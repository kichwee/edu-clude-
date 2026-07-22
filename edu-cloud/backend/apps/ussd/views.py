"""
USSD Webhook Handler — Track B1
Implements the full USSD session state machine for Kenya CBC tutoring.

Architecture: Stateless handler + Redis session (180s TTL per AT sessionId)
PRD FR-02: *384# service code, 182 char limit, Redis session state.
Research: Yiya/CMU study — DTMF menus first, AI second.
"""
import json
import structlog
from django.conf import settings
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST
from django.http import HttpResponse
from django_redis import get_redis_connection

from apps.students.models import Student
from apps.irt.engine import IRTEngine
from apps.core.gemini import GeminiClient
from apps.core.rag import RAGPipeline
from apps.core.i18n import get_string
from apps.core.decorators import validate_at_signature

logger = structlog.get_logger('educloud.ussd')

# ─── USSD Session State Machine ───────────────────────────────────────────────
# States: MAIN → SUBJECT → TOPIC → QUIZ → RESULT → ASK
# Each state knows how to render and what to expect next.

SUBJECTS = {
    '1': {'key': 'math',      'sw': 'Hesabu',    'en': 'Mathematics'},
    '2': {'key': 'science',   'sw': 'Sayansi',    'en': 'Science'},
    '3': {'key': 'kiswahili', 'sw': 'Kiswahili',  'en': 'Kiswahili'},
    '4': {'key': 'english',   'sw': 'English',    'en': 'English'},
}

GRADES = {
    '1': 3, '2': 4, '3': 5, '4': 6, '5': 7, '6': 8,
}


def get_session(redis, session_id: str) -> dict:
    raw = redis.get(f'ussd:{session_id}')
    return json.loads(raw) if raw else {}


def save_session(redis, session_id: str, data: dict):
    redis.setex(
        f'ussd:{session_id}',
        settings.USSD_SESSION_TTL,
        json.dumps(data),
    )


def clear_session(redis, session_id: str):
    redis.delete(f'ussd:{session_id}')


def con(text: str) -> HttpResponse:
    """CON = continue session"""
    return HttpResponse(f'CON {text}', content_type='text/plain')


def end(text: str) -> HttpResponse:
    """END = close session"""
    return HttpResponse(f'END {text}', content_type='text/plain')


def truncate(text: str, max_len: int = 176) -> str:
    """Ensure response fits within AT 182 char limit (CON + space = 4 chars)"""
    return text[:max_len] + ('...' if len(text) > max_len else '')


@csrf_exempt
@require_POST
@validate_at_signature
def ussd_callback(request):
    """
    POST /api/v1/ussd/callback
    Receives AT USSD callback, routes to state machine.
    """
    session_id = request.POST.get('sessionId', '')
    service_code = request.POST.get('serviceCode', '')
    phone = request.POST.get('phoneNumber', '')
    text = request.POST.get('text', '')
    network_code = request.POST.get('networkCode', '')

    log = logger.bind(
        session_id=session_id,
        phone=phone[-4:],  # Log only last 4 digits (privacy)
        text_depth=len(text.split('*')),
    )
    log.info('ussd_request')

    redis = get_redis_connection('default')
    session = get_session(redis, session_id)

    try:
        response = route_ussd(session_id, phone, text, session, redis, log)
        save_session(redis, session_id, session)
        return response
    except Exception as e:
        log.error('ussd_error', error=str(e))
        clear_session(redis, session_id)
        return end('Samahani, hitilafu imetokea. Jaribu tena.\n(Sorry, an error occurred. Try again.)')


def route_ussd(session_id: str, phone: str, text: str, session: dict, redis, log) -> HttpResponse:
    """Main USSD routing logic — declarative state machine"""
    latest_input = text.split('*')[-1] if text else ""
    
    current_state = session.get('state', 'INIT')
    if not text:
        current_state = 'INIT'
        session.clear()
        session['phone'] = phone
        session['lang'] = detect_language_for_phone(phone)
        session['state'] = 'INIT'

    STATE_HANDLERS = {
        'INIT': handle_init,
        'MAIN': handle_main,
        'GRADE': handle_grade,
        'MODE': handle_mode,
        'QUIZ_ANSWER': handle_quiz_answer,
        'QUIZ_NEXT': handle_quiz_next,
        'ASK_PROMPT': handle_ask_prompt,
    }

    handler = STATE_HANDLERS.get(current_state, handle_init)
    return handler(latest_input, session, phone, log)


def handle_init(latest_input: str, session: dict, phone: str, log) -> HttpResponse:
    lang = session.get('lang', 'sw')
    session['state'] = 'MAIN'
    return con(
        'Karibu EduCloud! Chagua somo:\n'
        '1. Hesabu\n'
        '2. Sayansi\n'
        '3. Kiswahili\n'
        '4. English\n'
        '5. Ripoti yangu'
        if lang == 'sw' else
        'Welcome! Choose a subject:\n'
        '1. Mathematics\n'
        '2. Science\n'
        '3. Kiswahili\n'
        '4. English\n'
        '5. My Report'
    )


def handle_main(latest_input: str, session: dict, phone: str, log) -> HttpResponse:
    if latest_input == '5':
        session['state'] = 'END'
        return handle_report(phone, session)

    if latest_input not in SUBJECTS:
        return con('Chaguo si sahihi. Chagua 1-5:\n1.Hesabu 2.Sayansi 3.Kiswahili 4.English 5.Ripoti')

    subject_info = SUBJECTS[latest_input]
    session['subject'] = subject_info['key']
    session['state'] = 'GRADE'

    return con(
        f'{subject_info["sw"]} - Chagua darasa:\n'
        '1. Darasa la 3\n'
        '2. Darasa la 4\n'
        '3. Darasa la 5\n'
        '4. Darasa la 6\n'
        '5. Darasa la 7\n'
        '6. Darasa la 8'
    )


def handle_grade(latest_input: str, session: dict, phone: str, log) -> HttpResponse:
    if latest_input not in GRADES:
        return con('Chagua darasa (1-6):\n1=D3 2=D4 3=D5 4=D6 5=D7 6=D8')

    session['grade'] = GRADES[latest_input]
    session['state'] = 'MODE'
    return con(
        'Chagua:\n'
        '1. Swali (Quiz)\n'
        '2. Uliza swali\n'
        '3. Somo la leo'
    )


def handle_mode(latest_input: str, session: dict, phone: str, log) -> HttpResponse:
    session['mode'] = latest_input

    if latest_input == '1':
        return render_quiz_question(session, phone, log)
    elif latest_input == '2':
        session['state'] = 'ASK_PROMPT'
        return con('Andika swali lako:\n(Swali fupi, maneno 20 au chini)')
    elif latest_input == '3':
        session['state'] = 'END'
        return render_lesson(session, phone, log)
    else:
        return con('Chagua 1, 2 au 3.')


def render_quiz_question(session: dict, phone: str, log) -> HttpResponse:
    subject = session.get('subject', 'math')
    grade = session.get('grade', 5)
    lang = session.get('lang', 'sw')

    student = Student.objects.filter(phone_number=phone, track='B').order_by('-last_active').first()
    theta = IRTEngine.get_theta(student, subject, 'general') if student else 0.0

    rag = RAGPipeline()
    question_data = rag.get_quiz_question(subject=subject, grade=grade, theta=theta, lang=lang)
    session['quiz_q'] = question_data
    session['state'] = 'QUIZ_ANSWER'

    q = question_data.get('question', 'Swali halikupatikana.')
    a = question_data.get('a', 'A')
    b = question_data.get('b', 'B')
    c = question_data.get('c', 'C')
    d = question_data.get('d', 'D')

    text = f'{truncate(q, 80)}\n1.{a}\n2.{b}\n3.{c}\n4.{d}'
    return con(text)


def handle_quiz_answer(latest_input: str, session: dict, phone: str, log) -> HttpResponse:
    lang = session.get('lang', 'sw')
    subject = session.get('subject', 'math')
    
    question_data = session.get('quiz_q', {})
    correct_idx = str(question_data.get('correct', '1'))
    is_correct = (latest_input == correct_idx)
    explanation = question_data.get('explanation', '')

    student = Student.objects.filter(phone_number=phone, track='B').order_by('-last_active').first()
    if student:
        IRTEngine.record_response(student, subject, 'general', is_correct)

    result = ('Vizuri sana!' if lang == 'sw' else 'Correct!') if is_correct else \
             ('Jibu sahihi ni ' + ['A','B','C','D'][int(correct_idx)-1] if lang == 'sw' else
              'Correct answer: ' + ['A','B','C','D'][int(correct_idx)-1])

    full = f'{result}\n{truncate(explanation, 100)}\n\n0.Swali jingine\n9.Menyu kuu'
    session['state'] = 'QUIZ_NEXT'
    return con(truncate(full, 176))


def handle_quiz_next(latest_input: str, session: dict, phone: str, log) -> HttpResponse:
    if latest_input == '0':
        return render_quiz_question(session, phone, log)
    return end('Asante! Endelea kusoma. Piga *384# tena.')


def handle_ask_prompt(latest_input: str, session: dict, phone: str, log) -> HttpResponse:
    question = latest_input.replace('_', ' ')  # AT encodes spaces as _
    subject = session.get('subject', 'math')
    grade = session.get('grade', 5)
    lang = session.get('lang', 'sw')

    log.info('ussd_ask', subject=subject, grade=grade)

    rag = RAGPipeline()
    context = rag.retrieve(query=question, subject=subject, grade=grade, lang=lang)
    gemini = GeminiClient()
    answer = gemini.tutor_response(
        question=question,
        context=context,
        subject=subject,
        grade=grade,
        lang=lang,
        channel='ussd',
    )
    session['state'] = 'END'
    return end(truncate(answer, 176))


def render_lesson(session: dict, phone: str, log) -> HttpResponse:
    """Brief lesson summary from RAG (daily tip)"""
    subject = session.get('subject', 'math')
    grade = session.get('grade', 5)
    lang = session.get('lang', 'sw')

    rag = RAGPipeline()
    lesson = rag.get_daily_lesson(subject=subject, grade=grade, lang=lang)
    return end(truncate(lesson, 176))


def handle_report(phone: str, session: dict) -> HttpResponse:
    """Show student's progress summary"""
    student = Student.objects.filter(phone_number=phone, track='B').order_by('-last_active').first()
    if student:
        from apps.streaks.utils import get_streak_summary
        summary = get_streak_summary(student)
        return end(truncate(summary, 176))
    else:
        return end('Umechukua somo? Tuma JOIN [nambari] kwa 40384 kwanza.')


def detect_language_for_phone(phone: str) -> str:
    """Default to Swahili for Kenya numbers (future: per-student pref)"""
    return 'sw'
