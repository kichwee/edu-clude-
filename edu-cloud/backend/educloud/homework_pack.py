"""Homework-pack contract for the labelled school-home join prototype.

The pack carries a skill and item ids only. It must never include a child's
name, photo, face, emotion score, or school roster field. The in-memory store
is process-local demo state, not a production database.
"""

from __future__ import annotations

from copy import deepcopy
from dataclasses import dataclass
from datetime import datetime, timezone
from threading import Lock
from typing import Any, Mapping
from uuid import UUID, uuid4

SCHEMA_VERSION = "1"
DEMO_CLASS_CODE = "G3-HOME"
ALLOWED_SKILL_ID = "two_digit_subtraction_regrouping"
ALLOWED_ITEM_IDS = (
    "g3-regroup-45-29",
    "g3-regroup-82-37",
    "g3-regroup-63-28",
)
FORBIDDEN_PACK_KEYS = frozenset(
    {
        "name",
        "names",
        "alias",
        "email",
        "phone",
        "photo",
        "photos",
        "face",
        "faces",
        "emotion",
        "emotions",
        "biometric",
        "national_id",
        "gps",
        "location",
        "roster",
        "student_name",
        "learner_name",
    }
)
MAX_PACK_BYTES = 2048
MAX_ATTEMPTS_PER_ASSIGNMENT = 40
MAX_ITEMS_PER_ATTEMPT_POST = 10


class HomeworkPackError(ValueError):
    """Raised when a pack or attempt payload violates the contract."""


def _require_mapping(payload: Any, label: str) -> Mapping[str, Any]:
    if not isinstance(payload, Mapping):
        raise HomeworkPackError(f"{label} must be a JSON object.")
    return payload


def _reject_forbidden_keys(payload: Mapping[str, Any]) -> None:
    lowered = {str(key).lower() for key in payload}
    hit = lowered & FORBIDDEN_PACK_KEYS
    if hit:
        raise HomeworkPackError(
            "Homework packs cannot include personal or biometric fields: " + ", ".join(sorted(hit)) + "."
        )


def _parse_class_code(raw: Any) -> str:
    if not isinstance(raw, str):
        raise HomeworkPackError("class_code must be a short token.")
    code = raw.strip().upper()
    if not 4 <= len(code) <= 16:
        raise HomeworkPackError("class_code must be 4–16 characters.")
    if any(ch not in "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-" for ch in code):
        raise HomeworkPackError("class_code may contain only letters, digits, and hyphens.")
    return code


def _parse_item_ids(raw: Any) -> tuple[str, ...]:
    if not isinstance(raw, list) or not raw:
        raise HomeworkPackError("item_ids must be a non-empty list.")
    if len(raw) > len(ALLOWED_ITEM_IDS):
        raise HomeworkPackError("item_ids is longer than the supported Grade 3 bank.")
    items: list[str] = []
    for item in raw:
        if not isinstance(item, str) or item not in ALLOWED_ITEM_IDS:
            raise HomeworkPackError("Unknown or unsupported item_id.")
        if item in items:
            raise HomeworkPackError("item_ids must be unique.")
        items.append(item)
    return tuple(items)


def demo_pack(*, assignment_id: str | None = None, issued_at: str | None = None) -> dict[str, Any]:
    """Return the presenter fixture. Stable class code; fresh ids unless supplied."""
    return {
        "schema_version": SCHEMA_VERSION,
        "assignment_id": assignment_id or str(uuid4()),
        "skill_id": ALLOWED_SKILL_ID,
        "item_ids": list(ALLOWED_ITEM_IDS),
        "issued_at": issued_at or datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z"),
        "class_code": DEMO_CLASS_CODE,
        "label": "Tonight from class · Grade 3 regrouping (prototype)",
        "prototype": True,
        "not_athena_production": True,
        "not_facial_analysis": True,
    }


def validate_pack(payload: Any) -> dict[str, Any]:
    """Return a normalised pack or raise HomeworkPackError."""
    body = _require_mapping(payload, "Homework pack")
    _reject_forbidden_keys(body)
    schema = body.get("schema_version")
    if schema != SCHEMA_VERSION:
        raise HomeworkPackError("Unsupported homework-pack schema_version.")
    skill_id = body.get("skill_id")
    if skill_id != ALLOWED_SKILL_ID:
        raise HomeworkPackError("Unknown or unsupported skill_id.")
    try:
        assignment_id = str(UUID(str(body.get("assignment_id", ""))))
    except (TypeError, ValueError) as exc:
        raise HomeworkPackError("assignment_id must be a UUID.") from exc
    issued_at = body.get("issued_at")
    if not isinstance(issued_at, str) or len(issued_at) > 40:
        raise HomeworkPackError("issued_at must be a short ISO-8601 timestamp.")
    pack = {
        "schema_version": SCHEMA_VERSION,
        "assignment_id": assignment_id,
        "skill_id": skill_id,
        "item_ids": list(_parse_item_ids(body.get("item_ids"))),
        "issued_at": issued_at,
        "class_code": _parse_class_code(body.get("class_code")),
        "label": "Tonight from class · Grade 3 regrouping (prototype)",
        "prototype": True,
        "not_athena_production": True,
        "not_facial_analysis": True,
    }
    return pack


def validate_attempt_payload(payload: Any, pack: Mapping[str, Any]) -> dict[str, Any]:
    """Validate one learner's item results. Learner token must be a UUID, not a name."""
    body = _require_mapping(payload, "Attempt payload")
    _reject_forbidden_keys(body)
    try:
        learner_token = str(UUID(str(body.get("learner_token", ""))))
    except (TypeError, ValueError) as exc:
        raise HomeworkPackError("learner_token must be a UUID, not a name.") from exc
    raw_attempts = body.get("attempts")
    if not isinstance(raw_attempts, list) or not raw_attempts:
        raise HomeworkPackError("attempts must be a non-empty list.")
    if len(raw_attempts) > MAX_ITEMS_PER_ATTEMPT_POST:
        raise HomeworkPackError("Too many attempt rows in one request.")
    allowed = set(pack["item_ids"])
    rows: list[dict[str, Any]] = []
    seen: set[str] = set()
    for row in raw_attempts:
        item = _require_mapping(row, "Attempt row")
        _reject_forbidden_keys(item)
        item_id = item.get("item_id")
        if item_id not in allowed or item_id in seen:
            raise HomeworkPackError("Attempt item_id is missing, duplicate, or not in this assignment.")
        correct = item.get("correct")
        if not isinstance(correct, bool):
            raise HomeworkPackError("Each attempt needs a boolean correct flag.")
        seen.add(item_id)
        rows.append({"item_id": item_id, "correct": correct})
    return {"learner_token": learner_token, "attempts": rows}


def aggregate_results(pack: Mapping[str, Any], submissions: list[Mapping[str, Any]]) -> dict[str, Any]:
    """Count learners and misses per item. No identities in the output."""
    per_item = {
        item_id: {"item_id": item_id, "attempts": 0, "missed": 0} for item_id in pack["item_ids"]
    }
    learners = {row["learner_token"] for row in submissions}
    for row in submissions:
        for attempt in row["attempts"]:
            bucket = per_item[attempt["item_id"]]
            bucket["attempts"] += 1
            if not attempt["correct"]:
                bucket["missed"] += 1
    return {
        "assignment_id": pack["assignment_id"],
        "skill_id": pack["skill_id"],
        "class_code": pack["class_code"],
        "learners_attempted": len(learners),
        "items": [per_item[item_id] for item_id in pack["item_ids"]],
        "prototype": True,
    }


@dataclass
class _AssignmentState:
    pack: dict[str, Any]
    submissions: list[dict[str, Any]]


class HomeworkStore:
    """Process-local assignments keyed by class code."""

    def __init__(self) -> None:
        self._lock = Lock()
        self._assignments: dict[str, _AssignmentState] = {}

    def reset(self) -> None:
        with self._lock:
            self._assignments.clear()

    def ensure_demo(self) -> dict[str, Any]:
        with self._lock:
            existing = self._assignments.get(DEMO_CLASS_CODE)
            if existing is not None:
                return deepcopy(existing.pack)
            pack = demo_pack()
            self._assignments[DEMO_CLASS_CODE] = _AssignmentState(pack=pack, submissions=[])
            return deepcopy(pack)

    def assign_demo(self) -> dict[str, Any]:
        pack = demo_pack()
        with self._lock:
            self._assignments[pack["class_code"]] = _AssignmentState(pack=pack, submissions=[])
            return deepcopy(pack)

    def get(self, class_code: str) -> dict[str, Any] | None:
        code = _parse_class_code(class_code)
        with self._lock:
            state = self._assignments.get(code)
            return deepcopy(state.pack) if state is not None else None

    def record_attempts(self, class_code: str, payload: Any) -> dict[str, Any]:
        code = _parse_class_code(class_code)
        with self._lock:
            state = self._assignments.get(code)
            if state is None:
                raise HomeworkPackError("Unknown class_code. Assign tonight's revision first.")
            parsed = validate_attempt_payload(payload, state.pack)
            if len(state.submissions) >= MAX_ATTEMPTS_PER_ASSIGNMENT:
                raise HomeworkPackError("This assignment has recorded enough demo attempts.")
            # Replace any earlier submission from the same token so a retry is idempotent.
            state.submissions = [row for row in state.submissions if row["learner_token"] != parsed["learner_token"]]
            state.submissions.append(parsed)
            return aggregate_results(state.pack, state.submissions)

    def results(self, class_code: str) -> dict[str, Any]:
        code = _parse_class_code(class_code)
        with self._lock:
            state = self._assignments.get(code)
            if state is None:
                raise HomeworkPackError("Unknown class_code. Assign tonight's revision first.")
            return aggregate_results(state.pack, state.submissions)


STORE = HomeworkStore()
