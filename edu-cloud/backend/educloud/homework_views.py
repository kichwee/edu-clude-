"""Labelled teacher-sidekick prototype for the school-home homework pack.

Not Athena production. Not facial analysis. Process-local demo state only.
"""

from __future__ import annotations

import json
from collections.abc import Mapping

from html import escape
from pathlib import Path

from django.http import HttpRequest, HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_GET, require_http_methods, require_POST

from .homework_pack import (
    DEMO_CLASS_CODE,
    FORBIDDEN_PACK_KEYS,
    HomeworkPackError,
    MAX_PACK_BYTES,
    STORE,
    validate_pack,
)

BANNER = (
    "Contract prototype. Not Athena production. Not facial analysis. "
    "No names, photos, or classroom cameras."
)
_PAGE_PATH = Path(__file__).resolve().parent / "templates" / "educloud" / "teacher_sidekick.html"


def _teacher_page_html(pack: Mapping[str, object], results: Mapping[str, object]) -> str:
    """Fill the static HTML file without Django templates (Python 3.14 test-client copy crash)."""
    rows = []
    for item in results.get("items") or []:
        if not isinstance(item, Mapping):
            continue
        rows.append(
            "<tr><td><code>{}</code></td><td>{}</td><td>{}</td></tr>".format(
                escape(str(item.get("item_id", ""))),
                escape(str(item.get("attempts", 0))),
                escape(str(item.get("missed", 0))),
            )
        )
    item_ids = pack.get("item_ids") or []
    page = _PAGE_PATH.read_text(encoding="utf-8")
    return (
        page.replace("__BANNER__", escape(BANNER))
        .replace("__CLASS_CODE__", escape(str(pack.get("class_code", DEMO_CLASS_CODE))))
        .replace("__SKILL_ID__", escape(str(pack.get("skill_id", ""))))
        .replace("__ITEM_IDS__", escape(", ".join(str(item_id) for item_id in item_ids)))
        .replace("__LEARNERS__", escape(str(results.get("learners_attempted", 0))))
        .replace("__ITEM_ROWS__", "\n          ".join(rows))
    )


def _json_body(request: HttpRequest) -> Mapping[str, object] | None:
    if len(request.body) > MAX_PACK_BYTES:
        return None
    if not request.body:
        return {}
    try:
        payload = json.loads(request.body)
    except (UnicodeDecodeError, json.JSONDecodeError):
        return None
    if not isinstance(payload, Mapping):
        return None
    return payload


def _error(message: str, status: int = 400) -> JsonResponse:
    return JsonResponse({"error": message, "prototype": True}, status=status)


@require_GET
def teacher_sidekick(request: HttpRequest) -> HttpResponse:
    pack = STORE.ensure_demo()
    try:
        results = STORE.results(pack["class_code"])
    except HomeworkPackError:
        results = {
            "learners_attempted": 0,
            "items": [{"item_id": item_id, "attempts": 0, "missed": 0} for item_id in pack["item_ids"]],
        }
    return HttpResponse(_teacher_page_html(pack, results), content_type="text/html; charset=utf-8")


@require_GET
def homework_pack_get(request: HttpRequest, class_code: str) -> JsonResponse:
    try:
        if class_code.strip().upper() == DEMO_CLASS_CODE:
            pack = STORE.ensure_demo()
        else:
            pack = STORE.get(class_code)
    except HomeworkPackError as exc:
        return _error(str(exc))
    if pack is None:
        return _error("Unknown class_code. Assign tonight's revision first.", status=404)
    return JsonResponse(pack)


@require_http_methods(["GET", "POST"])
@csrf_exempt
def homework_assign(request: HttpRequest) -> JsonResponse:
    if request.method == "GET":
        return JsonResponse(STORE.ensure_demo())
    body = _json_body(request)
    if body is None:
        return _error("Assignment request is too large or is not a JSON object.")
    lowered = {str(key).lower() for key in body}
    hit = lowered & FORBIDDEN_PACK_KEYS
    if hit:
        return _error(
            "Homework packs cannot include personal or biometric fields: " + ", ".join(sorted(hit)) + "."
        )
    skill_id = body.get("skill_id")
    if skill_id not in (None, "", "two_digit_subtraction_regrouping"):
        return _error("Unknown or unsupported skill_id.")
    if "assignment_id" in body or "item_ids" in body:
        try:
            validate_pack({**STORE.ensure_demo(), **body, "class_code": body.get("class_code", DEMO_CLASS_CODE)})
        except HomeworkPackError as exc:
            return _error(str(exc))
        return _error("Custom packs are out of scope for this prototype. Use the demo assign button.")
    return JsonResponse(STORE.assign_demo(), status=201)


@require_POST
@csrf_exempt
def homework_attempts(request: HttpRequest, class_code: str) -> JsonResponse:
    body = _json_body(request)
    if body is None:
        return _error("Attempt request is too large or is not a JSON object.")
    try:
        if class_code.strip().upper() == DEMO_CLASS_CODE:
            STORE.ensure_demo()
        summary = STORE.record_attempts(class_code, body)
    except HomeworkPackError as exc:
        status = 404 if "Unknown class_code" in str(exc) else 400
        return _error(str(exc), status=status)
    return JsonResponse(summary, status=201)


@require_GET
def homework_results(request: HttpRequest, class_code: str) -> JsonResponse:
    try:
        if class_code.strip().upper() == DEMO_CLASS_CODE:
            STORE.ensure_demo()
        summary = STORE.results(class_code)
    except HomeworkPackError as exc:
        status = 404 if "Unknown class_code" in str(exc) else 400
        return _error(str(exc), status=status)
    return JsonResponse(summary)
