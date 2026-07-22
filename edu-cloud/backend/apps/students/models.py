"""
Students data models — central student registry across all tracks.
Maps to PRD §2.3 database schema.
"""
import uuid
from django.db import models


class Student(models.Model):
    TRACK_CHOICES = [('A', 'On-Device'), ('B', 'Cloud-Backed'), ('C', 'School Hub')]
    LANGUAGE_CHOICES = [('sw', 'Swahili'), ('en', 'English')]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    device_id = models.TextField(db_index=True)
    alias = models.TextField()  # Pseudonym (Kenya DPA compliance)
    grade = models.PositiveSmallIntegerField()  # 1-12
    language_pref = models.CharField(max_length=2, choices=LANGUAGE_CHOICES, default='sw')
    created_at = models.DateTimeField(auto_now_add=True)
    last_active = models.DateTimeField(null=True, blank=True)
    consent_given = models.BooleanField(default=False)
    consent_date = models.DateTimeField(null=True, blank=True)
    track = models.CharField(max_length=1, choices=TRACK_CHOICES)
    phone_number = models.TextField(null=True, blank=True, db_index=True)  # Track B students
    school_code = models.TextField(null=True, blank=True)  # Track C hub assignment
    last_subject = models.CharField(max_length=20, default='math')

    class Meta:
        db_table = 'students'
        indexes = [
            models.Index(fields=['device_id']),
            models.Index(fields=['phone_number']),
            models.Index(fields=['school_code']),
        ]

    def __str__(self):
        return f'{self.alias} (Grade {self.grade}, Track {self.track})'


class Interaction(models.Model):
    CHANNEL_CHOICES = [
        ('app', 'App'), ('ussd', 'USSD'), ('sms', 'SMS'),
        ('voice', 'Voice'), ('stk', 'SIM Toolkit'),
        ('ivr', 'IVR'), ('hub', 'School Hub'),
    ]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    student = models.ForeignKey(Student, on_delete=models.CASCADE, related_name='interactions')
    question = models.TextField()
    ai_response = models.TextField()
    rag_sources = models.JSONField(null=True, blank=True)  # [{chunk_id, grade, subject, score}]
    is_correct = models.BooleanField(null=True, blank=True)
    time_taken_ms = models.IntegerField(null=True, blank=True)
    subject = models.TextField()
    strand = models.TextField(null=True, blank=True)
    difficulty = models.FloatField(default=0.5)  # IRT parameter b
    discrimination_a = models.FloatField(default=1.0)  # IRT parameter a
    channel = models.CharField(max_length=10, choices=CHANNEL_CHOICES)
    created_at = models.DateTimeField(auto_now_add=True)
    request_id = models.TextField(null=True, blank=True)  # Correlation ID (FR-16)

    class Meta:
        db_table = 'interactions'
        indexes = [
            models.Index(fields=['student', 'subject', 'created_at']),
            models.Index(fields=['channel', 'created_at']),
        ]


class IRTParameter(models.Model):
    """2-Parameter IRT model state per student per subject-strand (FR-06)"""
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    student = models.ForeignKey(Student, on_delete=models.CASCADE, related_name='irt_params')
    subject = models.TextField()
    strand = models.TextField()
    ability_theta = models.FloatField(default=0.0)  # Student ability (IRT theta)
    items_seen = models.IntegerField(default=0)
    last_calibrated = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = 'irt_parameters'
        unique_together = [('student', 'subject', 'strand')]


class Streak(models.Model):
    """Daily learning streak state (FR-11, Ebbinghaus + Duolingo research)"""
    student = models.OneToOneField(Student, on_delete=models.CASCADE, primary_key=True, related_name='streak')
    current_streak = models.IntegerField(default=0)
    longest_streak = models.IntegerField(default=0)
    last_activity = models.DateField(null=True, blank=True)
    freeze_available = models.BooleanField(default=True)
    freeze_used_this_week = models.BooleanField(default=False)

    class Meta:
        db_table = 'streaks'
        indexes = [models.Index(fields=['last_activity'])]


class ParentLink(models.Model):
    """Parent-student consent and notification channel (FR-07)"""
    CHANNEL_CHOICES = [('sms', 'SMS'), ('whatsapp', 'WhatsApp')]

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    student = models.ForeignKey(Student, on_delete=models.CASCADE, related_name='parent_links')
    parent_phone = models.TextField(db_index=True)
    channel = models.CharField(max_length=10, choices=CHANNEL_CHOICES, default='sms')
    consent_verified = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'parent_links'
