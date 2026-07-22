"""Deterministic Grade 3 Maths menu tutor used by the USSD simulator.

USSD is intentionally a short numeric state machine, not a free-text chatbot.
The source-backed lesson cards live in one versioned JSON pack and are safe to
render without a model, retrieval service, learner profile, or network call.
"""

from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class RuleLesson:
    id: str
    version: str
    topic: str
    source: str
    explanation: str
    hint: str
    question: str
    options: tuple[str, ...]
    correct_option: str
    correction: str


PACK_PATH = Path(__file__).resolve().parents[3] / "content" / "grade3_rule_tutor_v1.json"


def _load_lessons() -> tuple[RuleLesson, ...]:
    payload = json.loads(PACK_PATH.read_text(encoding="utf-8"))
    if payload.get("schema_version") != "1" or not payload.get("content_version") or not payload.get("label"):
        raise RuntimeError("Unsupported Grade 3 rule-tutor schema")
    lessons = tuple(
        RuleLesson(
            id=row["id"], version=payload["content_version"], topic=row["topic"], source=row["source"],
            explanation=row["explanation"], hint=row["hint"], question=row["question"],
            options=tuple(row["options"]), correct_option=row["correct_option"],
            correction=row["correction"],
        )
        for row in payload.get("lessons", [])
    )
    if len(lessons) < 1 or any(len(lesson.options) != 3 for lesson in lessons):
        raise RuntimeError("The Grade 3 rule tutor needs three-option lesson cards")
    if len({lesson.id for lesson in lessons}) != len(lessons):
        raise RuntimeError("Grade 3 rule-tutor lesson IDs must be unique")
    return lessons


LESSONS = _load_lessons()


def _con(title: str, *options: str) -> str:
    return "CON " + title + "\n" + "\n".join(options)


def reply(steps: tuple[str, ...]) -> str:
    """Return a bounded, stateless USSD reply for the Grade 3 Maths tutor."""
    if not steps:
        return _con(
            "Grade 3 Maths tutor",
            *(f"{index}. {lesson.topic}" for index, lesson in enumerate(LESSONS, start=1)),
        )

    topic_index = int(steps[0]) if steps[0].isdigit() else 0
    if not 1 <= topic_index <= len(LESSONS):
        return "END That topic is unavailable. Dial again."
    lesson = LESSONS[topic_index - 1]

    if len(steps) == 1:
        return _con(
            f"{lesson.topic}\n{lesson.explanation}\n{lesson.source}",
            "1. Hint",
            "2. Practice",
        )
    if len(steps) == 2 and steps[1] == "1":
        return _con(f"Hint: {lesson.hint}", "1. Practice")
    if (len(steps) == 2 and steps[1] == "2") or (len(steps) == 3 and steps[1:] == ("1", "1")):
        return _con(lesson.question, *lesson.options)
    if len(steps) == 3 and steps[1] == "2":
        return _feedback(lesson, steps[2])
    if len(steps) == 4 and steps[1:3] == ("1", "1"):
        return _feedback(lesson, steps[3])
    return "END That lesson has ended. Dial again for another topic."


def _feedback(lesson: RuleLesson, selected: str) -> str:
    if selected not in {"1", "2", "3"}:
        return "END That answer is unavailable. Dial again."
    if selected == lesson.correct_option:
        return f"END Nice work! {lesson.correction}"
    return f"END Almost. {lesson.correction}"


def answer_math_rule(query: str) -> str | None:
    """Solve Grade 3 Maths rule questions deterministically for USSD / backend queries."""
    import re
    q = query.lower()

    # 1. Simple Calculations (+ - * /)
    calc_match = re.search(r"(\d{1,5})\s*(\+|-|×|x|÷|/|plus|minus|add|subtract|times|divided by)\s*(\d{1,5})", q)
    if calc_match:
        a, op, b = int(calc_match.group(1)), calc_match.group(2), int(calc_match.group(3))
        if op in ("+", "plus", "add"):
            return f"Nice work! {a} + {b} = {a + b}."
        if op in ("-", "minus", "subtract"):
            return f"Nice work! {a} - {b} = {a - b}."
        if op in ("x", "×", "times"):
            return f"Nice work! {a} x {b} = {a * b}."
        if op in ("÷", "/", "divided by") and b != 0 and a % b == 0:
            return f"Nice work! {a} ÷ {b} = {a // b}."

    # 2. Even / Odd
    if "even" in q or "odd" in q:
        numbers = [int(n) for n in re.findall(r"\d+", q)]
        if numbers:
            n = numbers[0]
            if n % 2 == 0:
                return f"{n} is an EVEN number! Shared equally into 2 groups."
            else:
                return f"{n} is an ODD number! 1 is left over."

    # 3. Before / After / Next
    numbers = [int(n) for n in re.findall(r"\d+", q)]
    if numbers:
        n = numbers[0]
        if "after" in q or "next" in q:
            return f"The number after {n} is {n + 1}."
        if "before" in q:
            return f"The number before {n} is {n - 1}."

    # 4. Comparisons
    if len(numbers) >= 2:
        a, b = numbers[0], numbers[1]
        if "bigger" in q or "greater" in q or "larger" in q:
            return f"{max(a, b)} is bigger! ({a} vs {b})"
        if "smaller" in q or "less" in q:
            return f"{min(a, b)} is smaller! ({a} vs {b})"

    # 5. Fractions / Halving / Doubling
    if numbers:
        n = numbers[0]
        if "half" in q:
            return f"Half of {n} is {n // 2}."
        if "double" in q:
            return f"Double {n} is {n * 2}."
        if "quarter" in q and n % 4 == 0:
            return f"A quarter of {n} is {n // 4}."

    # 6. Curriculum Facts
    if "minute" in q and "hour" in q:
        return "There are 60 minutes in 1 hour."
    if "hour" in q and "day" in q:
        return "There are 24 hours in 1 day."
    if "cent" in q or "shilling" in q:
        return "100 cents equal 1 Kenyan shilling."
    if "triangle" in q:
        return "A triangle has 3 sides and 3 corners."
    if "square" in q:
        return "A square has 4 equal sides."
    if "rectangle" in q:
        return "A rectangle has 4 sides (2 long, 2 short)."
    if "circle" in q:
        return "A circle has 1 curved side and 0 corners."

    return None

