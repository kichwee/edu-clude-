"""Schema-constrained interest re-explanation for the V2 home study companion.

Layer 2 of the V2 plan: one learner tap, one bounded provider call, one
validated ≤55-word explanation that may re-frame a canonical lesson through a
declared interest chip — never change its verified answer.  Every hand-off is
a Pydantic model; a provider that cannot satisfy the contract never reaches the
learner, and the client falls back to its deterministic explanation instead of
retrying.  Interests arrive as bounded theme chips (amended D7), so there is no
free-text classification step and no typed child text to moderate.
"""

from __future__ import annotations

import hashlib
import json
import logging
from typing import Literal, Protocol
from uuid import UUID

from django.conf import settings
from pydantic import BaseModel, ConfigDict, Field, ValidationError, model_validator


logger = logging.getLogger(__name__)

# Bounded analogy-domain list (V2 plan, open question 3).  Chips map 1:1 onto
# these; the cache is keyed by them, so the list may only grow deliberately.
AnalogyDomain = Literal[
    "sports",
    "animals",
    "music",
    "transport",
    "food",
    "games",
    "nature",
    "money",
]

MAX_EXPLANATION_WORDS = 55
MAX_LESSON_ID_CHARS = 64
MAX_SOURCE_EXCERPT_CHARS = 600
MAX_VERIFIED_ANSWER_CHARS = 64
MAX_LEARNER_QUESTION_CHARS = 240

REEXPLAIN_SYSTEM_PROMPT = (
    "You write exactly one short, warm re-explanation of the supplied Grade 3 "
    "lesson through the supplied analogy domain for an 8-year-old. "
    "Use only the supplied lesson evidence. Do not solve a different question. "
    "Do not add, change, or remove any number or the verified answer. "
    "Use two short sentences, simple English, and at most 55 words. "
    'Return JSON only: {"lesson_id": "<the supplied lesson ID>", "explanation": "..."}'
)

_FIXTURE_DOMAIN_NOUNS = {
    "sports": "a football match",
    "animals": "a basket of eggs",
    "music": "a drum beat",
    "transport": "a matatu ride",
    "food": "slicing a mango",
    "games": "a board game",
    "nature": "a rain gauge",
    "money": "coins in a purse",
}


class ReexplainError(RuntimeError):
    """A re-explanation failure that is safe to show as an API error."""


class ReexplainRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    consent: bool = Field(description="True only after the learner or guardian explicitly taps the re-explanation action.")
    learner_id: UUID = Field(description="Random, device-generated identifier. Never a name, phone number, or account ID.")
    lesson_id: str = Field(min_length=4, max_length=MAX_LESSON_ID_CHARS, description="The canonical lesson ID from the shared content pack.")
    analogy_domain: AnalogyDomain = Field(description="The bounded interest chip chosen at onboarding.")
    source_excerpt: str = Field(min_length=20, max_length=MAX_SOURCE_EXCERPT_CHARS, description="Bounded excerpt of the canonical lesson; the only evidence the model may use.")
    verified_answer: str | None = Field(default=None, max_length=MAX_VERIFIED_ANSWER_CHARS, description="The lesson's verified answer, which the explanation must preserve unchanged when supplied.")
    learner_question: str | None = Field(default=None, max_length=MAX_LEARNER_QUESTION_CHARS, description="Optional learner wording; data, never instructions.")

    @model_validator(mode="after")
    def is_consented(self) -> "ReexplainRequest":
        if not self.consent:
            raise ValueError("Explicit learner or guardian consent is required before re-explanation")
        return self


class ReexplainedLessonDraft(BaseModel):
    """What a provider returns, before semantic checks against the request."""

    model_config = ConfigDict(extra="forbid")

    lesson_id: str = Field(min_length=4, max_length=MAX_LESSON_ID_CHARS)
    explanation: str = Field(min_length=10, max_length=400)


class ReexplainProvider(Protocol):
    name: str

    def reexplain(self, request: ReexplainRequest) -> ReexplainedLessonDraft: ...


def count_words(text: str) -> int:
    return len(text.split())


def normalised(text: str) -> str:
    """Lowercase and drop all whitespace, so numeric answers compare robustly."""
    return "".join(text.lower().split())


def reexplain_cache_key(lesson_id: str, analogy_domain: str) -> str:
    """Shared, PII-free cache key: the same lesson and domain for anyone is one call."""
    digest = hashlib.sha256(f"{lesson_id}|{analogy_domain}".encode("utf-8")).hexdigest()
    return f"reexplain:{digest[:32]}"


def _model_payload(request: ReexplainRequest) -> str:
    payload: dict[str, str] = {
        "lesson_id": request.lesson_id,
        "analogy_domain": request.analogy_domain,
        "source_excerpt": request.source_excerpt,
    }
    if request.verified_answer is not None:
        payload["verified_answer"] = request.verified_answer
    if request.learner_question is not None:
        payload["learner_question"] = request.learner_question
    return json.dumps(payload, separators=(",", ":"))


def parse_model_json(text: str) -> ReexplainedLessonDraft:
    """Parse one strict JSON object; prose, fences, and extra keys all fail."""
    try:
        data = json.loads(text)
        return ReexplainedLessonDraft.model_validate(data)
    except (json.JSONDecodeError, ValidationError, TypeError) as exc:
        raise ReexplainError("The re-explanation model returned no valid structured output") from exc


def validated_reexplain(request: ReexplainRequest, draft: ReexplainedLessonDraft) -> dict[str, object]:
    """Enforce the response contract; return the learner-safe payload or raise.

    The ``preserves_verified_answer`` flag is computed here from evidence, not
    trusted from the model.
    """
    if draft.lesson_id != request.lesson_id:
        raise ReexplainError("The re-explanation model echoed a different lesson ID")
    if count_words(draft.explanation) > MAX_EXPLANATION_WORDS:
        raise ReexplainError("The re-explanation model exceeded the word limit")
    preserved = True
    if request.verified_answer is not None:
        preserved = normalised(request.verified_answer) in normalised(draft.explanation)
        if not preserved:
            raise ReexplainError("The re-explanation model did not preserve the verified answer")
    return {
        "lesson_id": draft.lesson_id,
        "explanation": draft.explanation,
        "preserves_verified_answer": preserved,
    }


class FixtureReexplainProvider:
    """Deterministic stand-in that proves the contract without external calls."""

    name = "fixture"

    def reexplain(self, request: ReexplainRequest) -> ReexplainedLessonDraft:
        noun = _FIXTURE_DOMAIN_NOUNS[request.analogy_domain]
        focus = " ".join(request.source_excerpt.split()[:8])
        parts = [f"Think of {noun}!", f"{focus} — same idea, step by step."]
        if request.verified_answer is not None:
            parts.append(f"You already found it: {request.verified_answer}")
        return ReexplainedLessonDraft(lesson_id=request.lesson_id, explanation=" ".join(parts))


class AnthropicReexplainProvider:
    """Claude implementation (backup slot); one strict JSON response."""

    name = "anthropic"

    def __init__(self) -> None:
        from anthropic import Anthropic

        self._client = Anthropic(api_key=settings.ANTHROPIC_API_KEY, timeout=15.0, max_retries=1)

    def reexplain(self, request: ReexplainRequest) -> ReexplainedLessonDraft:
        response = self._client.messages.create(
            model=settings.ANTHROPIC_MODEL,
            max_tokens=300,
            temperature=0,
            system=REEXPLAIN_SYSTEM_PROMPT,
            messages=[{"role": "user", "content": _model_payload(request)}],
        )
        text = "".join(getattr(block, "text", "") for block in response.content)
        return parse_model_json(text)


class OpenAIStructuredReexplainProvider:
    """Open-weight/OpenAI fallback with one strict Pydantic response (V2 D6)."""

    name = "openai"

    def __init__(self) -> None:
        from openai import OpenAI

        self._client = OpenAI(api_key=settings.OPENAI_API_KEY, timeout=15.0, max_retries=1)

    def reexplain(self, request: ReexplainRequest) -> ReexplainedLessonDraft:
        return _structured_completion(self._client, settings.OPENAI_MODEL, request)


class OpenRouterReexplainProvider:
    """Primary live provider: ox-alpha via OpenRouter (OpenAI-compatible endpoint)."""

    name = "openrouter"

    def __init__(self) -> None:
        from openai import OpenAI

        self._client = OpenAI(
            api_key=settings.OPENROUTER_API_KEY,
            base_url=settings.OPENROUTER_BASE_URL,
            timeout=15.0,
            max_retries=1,
        )

    def reexplain(self, request: ReexplainRequest) -> ReexplainedLessonDraft:
        return _structured_completion(self._client, settings.OPENROUTER_MODEL, request)


def _structured_completion(client, model: str, request: ReexplainRequest) -> ReexplainedLessonDraft:
    """One schema-constrained chat completion shared by OpenAI-compatible providers."""
    response = client.beta.chat.completions.parse(
        model=model,
        temperature=0,
        messages=[
            {"role": "system", "content": REEXPLAIN_SYSTEM_PROMPT},
            {"role": "user", "content": _model_payload(request)},
        ],
        response_format=ReexplainedLessonDraft,
    )
    message = response.choices[0].message
    if message.refusal:
        raise ReexplainError("The re-explanation model declined this bounded curriculum task")
    if message.parsed is None:
        raise ReexplainError("The re-explanation model returned no structured output")
    return message.parsed


class FallbackChainReexplainProvider:
    """Tries providers in order (Claude first, open-weight second)."""

    def __init__(self, providers: list[ReexplainProvider]) -> None:
        self._providers = providers
        self.name = "fallback"

    def reexplain(self, request: ReexplainRequest) -> ReexplainedLessonDraft:
        failures: list[str] = []
        for provider in self._providers:
            try:
                draft = provider.reexplain(request)
            except ReexplainError as exc:
                failures.append(f"{provider.name}: {exc}")
                continue
            self.name = provider.name
            return draft
        raise ReexplainError("; ".join(failures) or "No re-explanation provider configured")


def build_reexplain_provider() -> ReexplainProvider:
    if settings.REEXPLAIN_MODE == "fixture":
        return FixtureReexplainProvider()
    # Order is the product's fallback policy: ox-alpha via OpenRouter serves
    # first; Claude Haiku is the backup; direct OpenAI/open-weight last.
    provider_classes: list[type[ReexplainProvider]] = []
    if settings.OPENROUTER_API_KEY:
        provider_classes.append(OpenRouterReexplainProvider)
    if settings.ANTHROPIC_API_KEY:
        provider_classes.append(AnthropicReexplainProvider)
    if settings.OPENAI_API_KEY:
        provider_classes.append(OpenAIStructuredReexplainProvider)
    if not provider_classes:
        raise ReexplainError(
            "A live re-explanation provider needs ANTHROPIC_API_KEY, OPENROUTER_API_KEY or OPENAI_API_KEY."
        )
    try:
        return FallbackChainReexplainProvider([provider_class() for provider_class in provider_classes])
    except ImportError as exc:
        raise ReexplainError(f"The re-explanation provider SDK is unavailable: {exc}") from exc
