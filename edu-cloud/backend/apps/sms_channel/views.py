"""
SMS Webhook Handler — Track B2
PRD FR-03: Shortcode 40384, keywords JIBU/SOMO/MSAADA/STOP/JOIN
Response: < 30s, max 3 SMS (480 chars)
"""
import structlog
from django.conf import settings
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST
from django.http import JsonResponse
from django.utils import timezone

import africastalking

from apps.students.models import Student, ParentLink
from apps.core.gemini import GeminiClient
from apps.core.rag import RAGPipeline
from apps.core.decorators import validate_at_signature
from apps.streaks.utils import update_streak

logger = structlog.get_logger('educloud.sms')

# Initialize Africa's Talking SDK
africastalking.initialize(settings.AFRICASTALKING_USERNAME, settings.AFRICASTALKING_API_KEY)
sms = africastalking.SMS

HELP_SW = (
    'EduCloud Msaada:\n'
    'JIBU [swali] - Uliza swali\n'
    'SOMO [somo] - Chagua somo\n'
    'STOP - Acha ujumbe\n'
    'Piga *384# kwa menyu kamili'
)
HELP_EN = (
    'EduCloud Help:\n'
    'JIBU [question] - Ask a question\n'
    'SOMO [subject] - Choose subject\n'
    'STOP - Unsubscribe\n'
    'Dial *384# for full menu'
)


@csrf_exempt
@require_POST
@validate_at_signature
def sms_callback(request):
    """
    POST /api/v1/sms/callback
    Receives Africa's Talking SMS callback.
    """
    phone = request.POST.get('from', '')
    text = request.POST.get('text', '').strip()
    shortcode = request.POST.get('to', settings.AFRICASTALKING_SHORTCODE)

    log = logger.bind(phone=phone[-4:], keyword=text.split()[0].upper() if text else '')
    log.info('sms_received')

    # Update student activity and streak
    student = get_or_create_student_b(phone)
    if student:
        update_streak(student)
        student.last_active = timezone.now()
        student.save(update_fields=['last_active'])

    response_text = route_sms(text, phone, student, log)

    # Send response via AT (async via Celery in production)
    try:
        send_sms(phone, response_text)
    except Exception as e:
        log.error('sms_send_error', error=str(e))

    return JsonResponse({'status': 'ok'})


def route_sms(text: str, phone: str, student, log) -> str:
    """Route SMS to appropriate handler based on keyword"""
    parts = text.split(maxsplit=1)
    keyword = parts[0].upper() if parts else ''
    body = parts[1].strip() if len(parts) > 1 else ''

    lang = student.language_pref if student else 'sw'

    if keyword == 'JIBU':
        return handle_jibu(body, student, lang, log)
    elif keyword == 'SOMO':
        return handle_somo(body, student, lang, log)
    elif keyword == 'MSAADA':
        return HELP_SW if lang == 'sw' else HELP_EN
    elif keyword == 'STOP':
        return handle_stop(phone, student, lang)
    elif keyword == 'JOIN':
        return handle_join(body, phone, lang, log)
    elif keyword == 'RIPOTI':
        return handle_report(student, lang)
    else:
        # Default: treat as free-form question
        if text:
            return handle_jibu(text, student, lang, log)
        return HELP_SW if lang == 'sw' else HELP_EN


def handle_jibu(question: str, student, lang: str, log) -> str:
    """
    JIBU [question] — RAG + Gemini answer via SMS.
    Max 480 chars (3 SMS parts).
    """
    if not question:
        hint = 'Andika: JIBU [swali lako]' if lang == 'sw' else 'Write: JIBU [your question]'
        return hint

    subject = student.last_subject if hasattr(student, 'last_subject') else 'math'
    grade = student.grade if student else 5

    log.info('sms_jibu', subject=subject, grade=grade, q_len=len(question))

    rag = RAGPipeline()
    context = rag.retrieve(query=question, subject=subject, grade=grade, lang=lang)

    gemini = GeminiClient()
    answer = gemini.tutor_response(
        question=question,
        context=context,
        subject=subject,
        grade=grade,
        lang=lang,
        channel='sms',
        max_tokens=300,  # ~480 chars
    )
    return truncate_sms(answer)


def handle_somo(subject_text: str, student, lang: str, log) -> str:
    """SOMO [subject] — set preferred subject"""
    SUBJECT_MAP = {
        'hesabu': 'math', 'math': 'math', 'mathematics': 'math',
        'sayansi': 'science', 'science': 'science',
        'kiswahili': 'kiswahili', 'swahili': 'kiswahili',
        'english': 'english',
    }
    key = subject_text.lower().strip()
    mapped = SUBJECT_MAP.get(key)

    if not mapped:
        opts = 'Hesabu, Sayansi, Kiswahili, English'
        return f'Somo halijulikani. Chagua: {opts}' if lang == 'sw' else f'Unknown subject. Choose: {opts}'

    if student:
        student.last_subject = mapped
        student.save(update_fields=['last_subject'])

    subjects_sw = {'math': 'Hesabu', 'science': 'Sayansi', 'kiswahili': 'Kiswahili', 'english': 'English'}
    name = subjects_sw.get(mapped, mapped)
    return f'Umechagua {name}! Tuma JIBU [swali] sasa.' if lang == 'sw' else f'Subject set to {name}! Send JIBU [question] now.'


def handle_stop(phone: str, student, lang: str) -> str:
    """STOP — unsubscribe from messages (Kenya DPA compliance)"""
    if student:
        # Deactivate parent links
        ParentLink.objects.filter(student=student).update(consent_verified=False)

    msg_sw = 'Umesimamisha ujumbe wa EduCloud. Tuma JOIN tena kuendelea.'
    msg_en = 'You have unsubscribed from EduCloud. Send JOIN to resubscribe.'
    return msg_sw if lang == 'sw' else msg_en


def handle_join(body: str, phone: str, lang: str, log) -> str:
    """
    JOIN [student_code] — parent consent registration (FR-07)
    Parent sends JOIN ABCD1234 → links to student → consent verified.
    """
    if not body:
        return 'Tuma: JOIN [nambari ya mtoto]' if lang == 'sw' else 'Send: JOIN [student code]'

    student_code = body.strip().upper()
    try:
        student = Student.objects.get(alias=student_code, track='B')
        ParentLink.objects.update_or_create(
            parent_phone=phone,
            student=student,
            defaults={'channel': 'sms', 'consent_verified': True},
        )
        name = student.alias
        grade = student.grade
        log.info('parent_joined', student_code=student_code)
        return (
            f'Asante! Umeunganishwa na {name} (Darasa {grade}). '
            f'Utapokea ripoti kila Ijumaa saa 12 usiku.'
            if lang == 'sw' else
            f'Thanks! Linked to {name} (Grade {grade}). '
            f'You will receive weekly reports every Friday at 6 PM.'
        )
    except Student.DoesNotExist:
        return 'Nambari ya mtoto haijulikani. Angalia nambari na jaribu tena.' if lang == 'sw' else \
               'Student code not found. Please check and try again.'


def handle_report(student, lang: str) -> str:
    """RIPOTI — inline progress report"""
    if not student:
        return 'Jiandikishe kwanza: SOMO [jina]' if lang == 'sw' else 'Register first: SOMO [name]'
    from apps.parents.reports import generate_student_summary
    return truncate_sms(generate_student_summary(student, lang=lang))


def get_or_create_student_b(phone: str):
    """Get existing Track B student by phone (most recently active), or return None"""
    return Student.objects.filter(phone_number=phone, track='B').order_by('-last_active').first()


def send_sms(phone: str, message: str):
    """Send SMS via Africa's Talking"""
    sms.send(message, [phone], sender_id=settings.AFRICASTALKING_SENDER_ID)


def truncate_sms(text: str, max_chars: int = 480) -> str:
    """Truncate to max 3 SMS parts (480 chars)"""
    if len(text) <= max_chars:
        return text
    return text[:477] + '...'
