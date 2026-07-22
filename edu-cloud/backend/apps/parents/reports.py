"""
Parent Reporting System
Generates weekly SMS reports for parents (FR-07).
"""
import structlog
from django.utils import timezone
from datetime import timedelta
from apps.students.models import Student, Interaction

logger = structlog.get_logger('educloud.parents')

def generate_student_summary(student: Student, lang: str = 'sw') -> str:
    """Generate a textual summary of a student's recent progress."""
    now = timezone.now()
    one_week_ago = now - timedelta(days=7)
    
    # Get last week's interactions
    recent_interactions = Interaction.objects.filter(
        student=student, 
        created_at__gte=one_week_ago,
        is_correct__isnull=False
    )
    
    total = recent_interactions.count()
    correct = recent_interactions.filter(is_correct=True).count()
    
    accuracy = int((correct / total * 100)) if total > 0 else 0
    
    # Get streak info
    try:
        streak_days = student.streak.current_streak
    except Exception:
        streak_days = 0
        
    name = student.alias
    
    if lang == 'sw':
        msg = f"Ripoti ya {name} (Wiki Hii):\n"
        if total == 0:
            msg += "Hajasoma wiki hii. Mhimize kujifunza!\n"
        else:
            msg += f"Maswali aliyojibu: {total}\nUsahihi: {accuracy}%\n"
        msg += f"Siku mfululizo: {streak_days}"
    else:
        msg = f"{name}'s Report (This Week):\n"
        if total == 0:
            msg += "No activity this week. Please encourage them to study!\n"
        else:
            msg += f"Questions answered: {total}\nAccuracy: {accuracy}%\n"
        msg += f"Active streak: {streak_days} days"
        
    return msg

def send_weekly_reports():
    """Celery task entry point to send reports to all consented parents."""
    from apps.students.models import ParentLink
    from apps.sms_channel.views import send_sms
    
    links = ParentLink.objects.filter(consent_verified=True).select_related('student')
    
    sent = 0
    for link in links:
        lang = link.student.language_pref
        report = generate_student_summary(link.student, lang=lang)
        
        try:
            if link.channel == 'sms':
                send_sms(link.parent_phone, report)
                sent += 1
        except Exception as e:
            logger.error('weekly_report_error', phone=link.parent_phone, error=str(e))
            
    logger.info('weekly_reports_sent', count=sent)
    return sent
