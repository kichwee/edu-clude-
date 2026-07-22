"""Stateless MVP USSD router for the shared Grade 3 Maths rule pack."""

from __future__ import annotations

from .grade3_rule_tutor import reply as grade3_rule_reply, answer_math_rule


def learning_reply(text: str) -> str:
    """Return a conventional CON/END response without storing learner data."""
    steps = tuple(step for step in text.split("*") if step)
    if not steps:
        return "CON Welcome to EduCloud\n1. Grade 3 Maths tutor\n2. Quick quiz\n3. Exit"
    if steps == ("3",):
        return "END Thanks for trying EduCloud."
    if steps[0] == "1":
        return grade3_rule_reply(steps[1:])
    if steps[0] == "2":
        if len(steps) == 1:
            return "CON What is 7 + 5?\n1. 11\n2. 12\n3. 13"
        if len(steps) == 2:
            return "END Nice work! 7 + 5 = 12." if steps[1] == "2" else "END Almost. 7 + 5 = 12."
        return "END That quiz has ended. Dial again for another one."

    rule_answer = answer_math_rule(text)
    if rule_answer:
        return f"END {rule_answer}"

    return "END That option is unavailable. Dial again."
