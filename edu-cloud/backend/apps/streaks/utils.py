"""
Streak System Utilities
Handles daily streak tracking, freezes, and milestones.
PRD FR-11: Daily streaks, 1 freeze per 7 days, milestones.
"""
from datetime import timedelta
import structlog
from django.utils import timezone
from apps.students.models import Student, Streak

logger = structlog.get_logger('educloud.streaks')

def update_streak(student: Student) -> dict:
    """
    Called when a student interacts with the system.
    Returns dict with streak info and any milestones hit.
    """
    today = timezone.localdate()
    streak, created = Streak.objects.get_or_create(student=student)
    
    result = {'streak': streak.current_streak, 'milestone': None, 'frozen': False}
    
    if streak.last_activity == today:
        # Already active today
        return result
        
    yesterday = today - timedelta(days=1)
    
    if streak.last_activity == yesterday:
        # Consecutive day
        streak.current_streak += 1
    elif streak.last_activity is not None and streak.last_activity < yesterday:
        # Missed day(s)
        if streak.freeze_available and streak.last_activity == today - timedelta(days=2):
            # Used a freeze for missing exactly one day
            streak.current_streak += 1
            streak.freeze_available = False
            streak.freeze_used_this_week = True
            result['frozen'] = True
            logger.info('streak_frozen', student=student.id)
        else:
            # Streak broken
            streak.current_streak = 1
            logger.info('streak_broken', student=student.id)
    else:
        # First activity ever
        streak.current_streak = 1

    if streak.current_streak > streak.longest_streak:
        streak.longest_streak = streak.current_streak
        
    streak.last_activity = today
    streak.save()
    
    # Check milestones (7, 14, 30, 90, 365)
    MILESTONES = [7, 14, 30, 90, 365]
    if streak.current_streak in MILESTONES:
        result['milestone'] = streak.current_streak
        logger.info('streak_milestone', student=student.id, days=streak.current_streak)
        
    result['streak'] = streak.current_streak
    return result


def get_streak_summary(student: Student, lang: str = 'sw') -> str:
    """Generate a text summary of the student's streak."""
    try:
        streak = student.streak
        days = streak.current_streak
        freeze = 'Ndiyo' if streak.freeze_available else 'Hapana'
        if lang == 'sw':
            return f"Siku mfululizo: {days}\nRekodi yako: {streak.longest_streak}\nKinga ipo: {freeze}"
        else:
            freeze_en = 'Yes' if streak.freeze_available else 'No'
            return f"Current streak: {days} days\nLongest streak: {streak.longest_streak}\nFreeze available: {freeze_en}"
    except Streak.DoesNotExist:
        return "Anza kujifunza leo kuweka rekodi!" if lang == 'sw' else "Start learning today to build a streak!"
