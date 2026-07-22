"""
Gemini Flash Client — Cloud LLM for Track B tutoring.
Implements Socratic pedagogy (avoids direct answers, guides students).

Research basis:
- SocraticAI paper (arXiv, 2024): +5.5pp improvement with scaffolding vs direct answers
- VanLehn (2011): step-level feedback outperforms final-answer checking
- PRD: Gemini Flash primary, Claude Haiku fallback on outage (R4 risk mitigation)
"""
import structlog
import google.generativeai as genai
from django.conf import settings

logger = structlog.get_logger('educloud.gemini')

# System prompts implementing Socratic pedagogy (research-validated)
SOCRATIC_SYSTEM_SW = """Wewe ni mshauri wa elimu wa EduCloud, ukifundisha wanafunzi wa Kenya CBC.

KANUNI MUHIMU:
1. USITOE jibu moja kwa moja — uliza maswali yanayoongoza
2. Tumia mfano wa maisha halisi wa Kenya (kuna, ng'ombe, shilingi)
3. Jibu kwa Kiswahili safi, lugha rahisi kwa darasa la msingi
4. Kila jibu liwe na chanzo: "Kutoka vitabu vya Darasa [X]..."
5. Kama hujui — sema ukweli: "Hili silijui vizuri. Uliza mwalimu wako."
6. Jibu fupi: maneno 50-80 tu (kwa SMS/USSD: chini ya 150 maneno)

MFANO WA JIBU ZURI:
Swali: "Tisa mara nane ni ngapi?"
Jibu: "Hebu fikiria! Unapanga safu tisa za ng'ombe, kila safu ina ng'ombe nane. Je, unajumlisha vipi? Kwanza hesabu 9×8..."

USISEME KAMWE: "Jibu ni..." au "Hesabu hii ni..."
"""

SOCRATIC_SYSTEM_EN = """You are an EduCloud education assistant, teaching Kenya CBC students.

CORE RULES:
1. NEVER give a direct answer — ask guiding questions (Socratic method)
2. Use real Kenyan examples (cows, maize, shillings, football)
3. Write in clear, simple English for primary school level
4. Always cite source: "From Grade [X] curriculum..."
5. If unsure — be honest: "I'm not certain. Please ask your teacher."
6. Keep responses SHORT: 50-80 words (for SMS/USSD: under 150 words)

EXAMPLE GOOD RESPONSE:
Question: "What is 9 times 8?"
Response: "Let's think together! If you have 9 rows of maize, each with 8 plants, how would you count them? Try adding 9 groups of 8..."

NEVER SAY: "The answer is..." or "The result is..."
"""

CHANNEL_MAX_TOKENS = {
    'app': 512,
    'ussd': 150,
    'sms': 300,
    'voice': 200,  # ~20s TTS at average speed
    'hub': 400,
    'default': 300,
}


class GeminiClient:
    def __init__(self):
        genai.configure(api_key=settings.GEMINI_API_KEY)
        self.model = genai.GenerativeModel(settings.GEMINI_MODEL)

    def tutor_response(
        self,
        question: str,
        context: list[dict],
        subject: str,
        grade: int,
        lang: str = 'sw',
        channel: str = 'app',
        max_tokens: int = None,
    ) -> str:
        """
        Generate a Socratic tutoring response grounded in RAG context.
        Implements the RAG architecture: context → constrained LLM generation.
        """
        system = SOCRATIC_SYSTEM_SW if lang == 'sw' else SOCRATIC_SYSTEM_EN
        max_tok = max_tokens or CHANNEL_MAX_TOKENS.get(channel, 300)

        # Format context from RAG chunks
        context_text = ''
        if context:
            parts = []
            for c in context[:3]:  # Top 3 chunks
                source = f"Grade {c['grade']} {c['subject'].title()}"
                if c.get('topic'):
                    source += f" — {c['topic']}"
                parts.append(f"[{source}]\n{c['text']}")
            context_text = '\n\n'.join(parts)

        # Construct the grounded prompt
        if lang == 'sw':
            prompt = (
                f"Somo: {subject} | Darasa: {grade}\n\n"
                f"Maudhui ya mtaala (CBC):\n{context_text}\n\n"
                f"Swali la mwanafunzi: {question}\n\n"
                f"Jibu kwa Kisokrasia (usitoe jibu moja kwa moja, ongoza):"
            ) if context_text else (
                f"Somo: {subject} | Darasa: {grade}\n\n"
                f"Swali la mwanafunzi: {question}\n\n"
                f"Jibu kwa uaminifu kwa Kisokrasia:"
            )
        else:
            prompt = (
                f"Subject: {subject} | Grade: {grade}\n\n"
                f"CBC Curriculum Context:\n{context_text}\n\n"
                f"Student Question: {question}\n\n"
                f"Respond with Socratic guidance (do NOT give direct answer):"
            ) if context_text else (
                f"Subject: {subject} | Grade: {grade}\n\n"
                f"Student Question: {question}\n\n"
                f"Respond honestly using Socratic method:"
            )

        try:
            response = self.model.generate_content(
                [system, prompt],
                generation_config=genai.GenerationConfig(
                    max_output_tokens=max_tok,
                    temperature=settings.GEMINI_TEMPERATURE,
                    top_p=0.85,
                ),
            )
            text = response.text.strip()
            logger.info('gemini_response', channel=channel, tokens=len(text.split()), has_context=bool(context_text))
            return text
        except Exception as e:
            logger.error('gemini_error', error=str(e))
            # Fallback: return top RAG chunk directly (retrieval-only mode)
            if context:
                chunk = context[0]['text'][:300]
                prefix = 'Kutoka kwa mtaala:' if lang == 'sw' else 'From curriculum:'
                return f'{prefix}\n{chunk}'
            return ('Samahani, hitilafu imetokea. Jaribu tena.' if lang == 'sw' else
                    'Sorry, an error occurred. Please try again.')

    def generate_lesson_plan(
        self,
        grade: int,
        subject: str,
        strand: str,
        lang: str = 'sw',
    ) -> str:
        """Teacher Copilot: Generate a 40-minute lesson plan (FR-08)"""
        system = (
            'Wewe ni mshauri wa walimu wa CBC Kenya. '
            'Tengeneza mpango wa somo wa dakika 40 kwa muundo wa CBC: '
            'Malengo ya Ujifunzaji → Shughuli → Tathmini.'
        ) if lang == 'sw' else (
            'You are a Kenya CBC teacher advisor. '
            'Create a 40-minute lesson plan in CBC format: '
            'Learning Objectives → Activities → Assessment.'
        )

        prompt = (
            f'Darasa: {grade} | Somo: {subject} | Mada: {strand}\n'
            'Tengeneza mpango wa somo wa dakika 40.'
        ) if lang == 'sw' else (
            f'Grade: {grade} | Subject: {subject} | Strand: {strand}\n'
            'Generate a 40-minute lesson plan.'
        )

        try:
            response = self.model.generate_content(
                [system, prompt],
                generation_config=genai.GenerationConfig(max_output_tokens=800, temperature=0.3),
            )
            return response.text.strip()
        except Exception as e:
            logger.error('gemini_lesson_plan_error', error=str(e))
            return 'Hitilafu — jaribu tena.' if lang == 'sw' else 'Error — please try again.'
