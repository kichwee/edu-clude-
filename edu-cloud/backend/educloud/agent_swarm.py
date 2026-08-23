"""Schema-constrained orchestration for the demo autonomous teaching loop.

The stages are intentionally narrow and sequential: assess a bounded set of
anonymous errors, author one original micro-lesson, then compile a strict
offline pack.  Every hand-off is a Pydantic model; an invalid model response
never becomes a downloadable Android lesson.
"""

from __future__ import annotations

import json
import logging
from hashlib import sha256
from typing import Literal, Protocol, TypeVar
from uuid import UUID, uuid4

from django.conf import settings
from pydantic import BaseModel, ConfigDict, Field, ValidationError, model_validator


logger = logging.getLogger(__name__)

SkillId = Literal["two_digit_subtraction_regrouping"]
T = TypeVar("T", bound=BaseModel)


class AgentSwarmError(RuntimeError):
    """An orchestration failure that is safe to show as an API error."""


class FailedAttempt(BaseModel):
    model_config = ConfigDict(extra="forbid")

    attempt_id: UUID = Field(description="Random local event ID; never a learner identifier.")
    skill_id: SkillId = Field(description="Only the supported Grade 3 maths skill identifier.")
    minuend: int = Field(ge=10, le=99, description="The first two-digit number in a subtraction problem.")
    subtrahend: int = Field(ge=1, le=98, description="The smaller number subtracted from the minuend.")
    learner_answer: int = Field(ge=-99, le=99, description="The incorrect numeric answer selected locally.")

    @model_validator(mode="after")
    def has_a_valid_subtraction_problem(self) -> "FailedAttempt":
        if self.subtrahend >= self.minuend:
            raise ValueError("subtrahend must be smaller than minuend")
        if self.learner_answer == self.minuend - self.subtrahend:
            raise ValueError("telemetry includes a correct answer, not a failed attempt")
        return self


class TelemetryRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    learner_id: UUID = Field(description="Random, device-generated identifier. Never a name, phone number, or account ID.")
    request_id: UUID = Field(description="Random idempotency key for this explicit demo-sync request.")
    demo_consent: bool = Field(description="True only after the learner or guardian explicitly starts this demo sync.")
    attempts: list[FailedAttempt] = Field(min_length=3, max_length=10, description="Three to ten incorrect attempts for one bounded skill.")

    @model_validator(mode="after")
    def is_consented_and_single_skill(self) -> "TelemetryRequest":
        if not self.demo_consent:
            raise ValueError("Explicit demo consent is required before telemetry can sync")
        if len({attempt.skill_id for attempt in self.attempts}) != 1:
            raise ValueError("A demo remediation request can cover one skill only")
        return self


class Assessment(BaseModel):
    model_config = ConfigDict(extra="forbid")

    target_skill: SkillId = Field(description="The single assessed Grade 3 maths skill.")
    conceptual_gap: Literal["regrouping_from_tens"] = Field(description="The specific misconception supported by the failed attempts.")
    evidence_summary: str = Field(min_length=20, max_length=240, description="Brief, non-identifying explanation of the observed error pattern.")
    confidence: float = Field(ge=0.0, le=1.0, description="Confidence based only on the supplied attempts.")


class DifferentiatedLesson(BaseModel):
    model_config = ConfigDict(extra="forbid")

    title: str = Field(min_length=8, max_length=80, description="Original child-friendly Grade 3 micro-lesson title.")
    micro_lesson: str = Field(min_length=80, max_length=500, description="Original, short explanation of regrouping; do not quote a curriculum source.")
    teaching_steps: list[str] = Field(min_length=3, max_length=3, description="Exactly three short, actionable regrouping steps.")
    definition: str = Field(min_length=20, max_length=180, description="A simple original definition of regrouping.")


class PracticeQuestion(BaseModel):
    model_config = ConfigDict(extra="forbid")

    minuend: int = Field(ge=10, le=99, description="First number in a two-digit subtraction practice problem.")
    subtrahend: int = Field(ge=1, le=98, description="Second number in a two-digit subtraction practice problem.")
    answer: int = Field(ge=0, le=98, description="Correct result of minuend minus subtrahend.")

    @model_validator(mode="after")
    def is_arithmetically_correct(self) -> "PracticeQuestion":
        if self.subtrahend >= self.minuend:
            raise ValueError("practice subtraction must have a positive answer")
        if self.answer != self.minuend - self.subtrahend:
            raise ValueError("practice answer does not match its subtraction problem")
        return self


class CompiledRemediationDraft(BaseModel):
    model_config = ConfigDict(extra="forbid")

    title: str = Field(min_length=8, max_length=80, description="Use the differentiated lesson title unchanged.")
    micro_lesson: str = Field(min_length=80, max_length=500, description="Use the original differentiated micro-lesson unchanged.")
    teaching_steps: list[str] = Field(min_length=3, max_length=3, description="Use exactly the three differentiated teaching steps unchanged.")
    definition: str = Field(min_length=20, max_length=180, description="Use the original differentiated definition unchanged.")
    practice_questions: list[PracticeQuestion] = Field(min_length=3, max_length=3, description="Exactly three valid, regrouping subtraction questions with checked answers.")


class RemediationLesson(BaseModel):
    model_config = ConfigDict(extra="forbid")

    id: str
    topic: str
    source: str
    keywords: list[str]
    micro_lesson: str
    teaching_steps: list[str]
    definition: str
    practice_questions: list[PracticeQuestion]


class RemediationPackPayload(BaseModel):
    model_config = ConfigDict(extra="forbid")

    schema_version: Literal["1"]
    pack_id: UUID
    content_version: str
    label: str
    target_skill: SkillId
    validation_status: Literal["automatic_validation_passed"]
    provenance: str
    lessons: list[RemediationLesson] = Field(min_length=1, max_length=1)


class TeachingLoopClient(Protocol):
    def assess(self, telemetry: TelemetryRequest) -> Assessment: ...

    def differentiate(self, assessment: Assessment) -> DifferentiatedLesson: ...

    def compile(self, assessment: Assessment, lesson: DifferentiatedLesson, telemetry: TelemetryRequest) -> CompiledRemediationDraft: ...


class FixtureTeachingLoopClient:
    """Deterministic three-stage stand-in for the no-key hackathon demonstration."""

    def assess(self, telemetry: TelemetryRequest) -> Assessment:
        return Assessment(
            target_skill="two_digit_subtraction_regrouping",
            conceptual_gap="regrouping_from_tens",
            evidence_summary=(
                f"{len(telemetry.attempts)} incorrect two-digit subtraction attempts show that the learner "
                "needs a visible exchange from one ten into ten ones before subtracting the ones column."
            ),
            confidence=0.92,
        )

    def differentiate(self, _: Assessment) -> DifferentiatedLesson:
        return DifferentiatedLesson(
            title="Trade one ten, then subtract",
            micro_lesson=(
                "When the top ones digit is too small, trade one ten for ten ones. "
                "For 45 - 29, trade 1 ten from 4 tens. Now you have 3 tens and 15 ones. "
                "Subtract the ones first, then subtract the tens."
            ),
            teaching_steps=[
                "Check whether the top ones digit is smaller than the bottom ones digit.",
                "Trade one top ten for ten ones and write the new tens and ones.",
                "Subtract ones, then subtract tens, and check your answer by adding.",
            ],
            definition="Regrouping means trading one ten for ten ones so that you can subtract the ones safely.",
        )

    def compile(self, _: Assessment, lesson: DifferentiatedLesson, telemetry: TelemetryRequest) -> CompiledRemediationDraft:
        return CompiledRemediationDraft(
            title=lesson.title,
            micro_lesson=lesson.micro_lesson,
            teaching_steps=lesson.teaching_steps,
            definition=lesson.definition,
            practice_questions=[
                PracticeQuestion(minuend=attempt.minuend, subtrahend=attempt.subtrahend, answer=attempt.minuend - attempt.subtrahend)
                for attempt in telemetry.attempts[:3]
            ],
        )


class OpenAIStructuredTeachingLoopClient:
    """OpenAI implementation with one strict Pydantic response per stage."""

    def __init__(self) -> None:
        from openai import OpenAI

        self._client = OpenAI(api_key=settings.OPENAI_API_KEY, timeout=15.0, max_retries=1)

    def _parse(self, response_model: type[T], system_prompt: str, payload: dict[str, object]) -> T:
        response = self._client.beta.chat.completions.parse(
            model=settings.OPENAI_MODEL,
            temperature=0,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": json.dumps(payload, separators=(",", ":"))},
            ],
            response_format=response_model,
        )
        message = response.choices[0].message
        if message.refusal:
            raise AgentSwarmError("The teaching-loop model declined this bounded curriculum task")
        if message.parsed is None:
            raise AgentSwarmError("The teaching-loop model returned no structured output")
        return message.parsed

    def assess(self, telemetry: TelemetryRequest) -> Assessment:
        return self._parse(
            Assessment,
            "You are the Assessor Agent. Analyze only the anonymous failed Grade 3 maths attempts. "
            "Identify the one supported conceptual gap. Do not author a lesson and do not infer identity, ability, or diagnosis.",
            {"attempts": [attempt.model_dump(mode="json") for attempt in telemetry.attempts]},
        )

    def differentiate(self, assessment: Assessment) -> DifferentiatedLesson:
        return self._parse(
            DifferentiatedLesson,
            "You are the Differentiator Agent. Write an original, concise Grade 3 maths micro-lesson for the supplied gap. "
            "Do not quote, claim, or reproduce external curriculum content. Do not include a learner name or personal data.",
            assessment.model_dump(mode="json"),
        )

    def compile(self, assessment: Assessment, lesson: DifferentiatedLesson, telemetry: TelemetryRequest) -> CompiledRemediationDraft:
        return self._parse(
            CompiledRemediationDraft,
            "You are the Compiler Agent. Convert the approved lesson into exactly three offline practice records. "
            "Keep the lesson text unchanged, use two-digit subtraction that requires regrouping, and calculate every answer exactly. "
            "Return only the requested structured object.",
            {
                "assessment": assessment.model_dump(mode="json"),
                "lesson": lesson.model_dump(mode="json"),
                "failed_attempts": [attempt.model_dump(mode="json") for attempt in telemetry.attempts],
            },
        )


def build_teaching_loop_client() -> TeachingLoopClient:
    if settings.AGENT_SWARM_MODE == "fixture":
        return FixtureTeachingLoopClient()
    if not settings.OPENAI_API_KEY:
        raise AgentSwarmError("OPENAI_API_KEY is required when AGENT_SWARM_MODE=openai")
    return OpenAIStructuredTeachingLoopClient()


def telemetry_digest(telemetry: TelemetryRequest) -> str:
    """Create a non-reversible trace of a request without persisting attempts."""
    canonical = json.dumps(telemetry.model_dump(mode="json"), sort_keys=True, separators=(",", ":"))
    return sha256(canonical.encode("utf-8")).hexdigest()


def run_teaching_loop(telemetry: TelemetryRequest) -> RemediationPackPayload:
    """Run assess → differentiate → compile and return a validated offline payload."""
    client = build_teaching_loop_client()
    assessment = client.assess(telemetry)
    if assessment.target_skill != telemetry.attempts[0].skill_id:
        raise AgentSwarmError("Assessor returned a skill outside the supplied telemetry")
    lesson = client.differentiate(assessment)
    draft = client.compile(assessment, lesson, telemetry)

    pack_id = uuid4()
    pack = RemediationPackPayload(
        schema_version="1",
        pack_id=pack_id,
        content_version=f"remediation-{pack_id.hex[:12]}",
        label="Personalised Grade 3 Maths remediation",
        target_skill=assessment.target_skill,
        validation_status="automatic_validation_passed",
        provenance=(
            "AI-generated original Grade 3 Maths remediation from anonymised error patterns; "
            "automatically validated for schema, supported scope, provenance label, and arithmetic. "
            "Not KICD/KEC curriculum content."
        ),
        lessons=[
            RemediationLesson(
                id=f"remediation-{pack_id.hex[:12]}-regrouping",
                topic=draft.title,
                source="Personalised Grade 3 Maths practice · automatic checks passed",
                keywords=["subtraction", "regrouping", "borrow", "tens", "ones"],
                micro_lesson=draft.micro_lesson,
                teaching_steps=draft.teaching_steps,
                definition=draft.definition,
                practice_questions=draft.practice_questions,
            )
        ],
    )
    logger.info("teaching_loop_completed", extra={"pack_id": str(pack.pack_id), "skill": pack.target_skill})
    return pack
