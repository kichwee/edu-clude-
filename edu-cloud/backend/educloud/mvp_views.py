"""Stateless original Grade 1–12 demo simulators used in the hackathon demo.

These endpoints never call an SMS/USSD provider, store a phone number, create
an account, or send a message.  They let the Android/cloud comparison demo show
the feature-phone interaction safely before provider credentials and consent
flows exist.
"""

import hashlib
import hmac
import json
import logging
from uuid import UUID
from collections.abc import Mapping

from django.conf import settings
from django.core.cache import cache
from django.http import (
    HttpRequest,
    HttpResponse,
    HttpResponseBadRequest,
    HttpResponseNotFound,
    JsonResponse,
)
from django.db import IntegrityError
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_GET, require_POST
from pydantic import ValidationError

from .ussd_learning import learning_reply
from .agent_swarm import AgentSwarmError, TelemetryRequest, run_teaching_loop, telemetry_digest
from .models import RemediationPack


MAX_SIMULATOR_INPUT_CHARS = 64
SANDBOX_REQUESTS_PER_MINUTE = 30
logger = logging.getLogger(__name__)


def _value(request: HttpRequest, name: str) -> str | None:
    """Return one bounded string field, or ``None`` for an invalid request."""
    if len(request.body) > 1024:
        return None
    if request.content_type and request.content_type.startswith("application/json"):
        try:
            payload = json.loads(request.body or b"{}")
        except (UnicodeDecodeError, json.JSONDecodeError):
            return None
        if not isinstance(payload, Mapping):
            return None
        value = payload.get(name, "")
    else:
        value = request.POST.get(name, "")
    if not isinstance(value, str):
        return None
    value = value.strip()
    return value if len(value) <= MAX_SIMULATOR_INPUT_CHARS else None


@require_GET
def health(_: HttpRequest) -> JsonResponse:
    return JsonResponse({"status": "ok", "service": "educloud-demo"})


@require_GET
def ready(_: HttpRequest) -> JsonResponse:
    return JsonResponse({"status": "ready", "mode": "demo", "external_providers": False})


def _ussd_reply(text: str) -> str:
    return learning_reply(text)


def _sandbox_rate_limited(request: HttpRequest) -> bool:
    """Throttle a gateway without retaining a learner's phone number.

    A provider may submit a phone number and session ID in its form payload,
    but this callback intentionally does not read either field.  The bounded,
    transient cache key protects the public endpoint from trivial flooding.
    """
    gateway_ip = request.META.get("REMOTE_ADDR", "unknown")
    gateway_hash = hashlib.sha256(gateway_ip.encode("utf-8")).hexdigest()
    key = f"ussd-sandbox-rate:{gateway_hash}"
    if cache.add(key, 1, timeout=60):
        return False
    try:
        return cache.incr(key) > SANDBOX_REQUESTS_PER_MINUTE
    except ValueError:
        # A cache eviction is safe: treat this as a fresh, permitted request.
        cache.set(key, 1, timeout=60)
        return False


@require_POST
@csrf_exempt
def ussd_simulator(request: HttpRequest) -> HttpResponse:
    """Accept a simulator ``text`` field and return conventional CON/END text."""
    text = _value(request, "text")
    if text is None:
        return HttpResponseBadRequest("Invalid simulator input.", content_type="text/plain; charset=utf-8")
    return HttpResponse(_ussd_reply(text), content_type="text/plain; charset=utf-8")


@require_POST
@csrf_exempt
def ussd_sandbox_callback(request: HttpRequest, callback_token: str) -> HttpResponse:
    """Serve a configured provider sandbox callback without collecting PII.

    The callback understands the conventional ``text`` USSD field only.  It
    deliberately ignores provider fields such as ``phoneNumber``, ``sessionId``
    and ``serviceCode``; the state machine is deterministic and stateless.
    This route is unavailable until a deployment explicitly selects sandbox
    mode and supplies the URL capability configured at the provider.
    """
    if settings.TELEPHONY_MODE != "sandbox" or not hmac.compare_digest(
        callback_token, settings.USSD_SANDBOX_CALLBACK_TOKEN
    ):
        return HttpResponseNotFound("Not found.")
    if _sandbox_rate_limited(request):
        return HttpResponse(
            "END EduCloud is busy. Please try again shortly.",
            status=429,
            content_type="text/plain; charset=utf-8",
        )
    text = _value(request, "text")
    if text is None:
        return HttpResponseBadRequest("Invalid USSD input.", content_type="text/plain; charset=utf-8")
    return HttpResponse(_ussd_reply(text), content_type="text/plain; charset=utf-8")


@require_POST
@csrf_exempt
def sms_simulator(request: HttpRequest) -> JsonResponse:
    """Return a simulated reply only; no outbound SMS is dispatched."""
    text = _value(request, "text")
    if text is None:
        return JsonResponse({"error": "Invalid simulator input."}, status=400)
    command = " ".join(text.upper().split())
    replies = {
        "START": "Welcome to EduCloud demo. Reply MATH for a lesson or QUIZ for a question.",
        "MATH": "Maths: 4 groups of 3 make 12. Repeated addition: 3 + 3 + 3 + 3 = 12.",
        "QUIZ": "Quiz: What is 9 - 4? Reply ANSWER 5 in the simulator.",
        "ANSWER 5": "Correct! 9 - 4 = 5.",
        "STOP": "Demo preference recorded for this browser request only. No messages will be sent.",
        "HELP": "Demo commands: START, MATH, QUIZ, ANSWER 5, STOP.",
    }
    return JsonResponse({"reply": replies.get(command, replies["HELP"]), "simulated": True})


def _sync_is_enabled() -> bool:
    return settings.EDGE_SYNC_MODE == "demo"


def _sync_disabled_response() -> JsonResponse:
    return JsonResponse(
        {"error": "The autonomous teaching-loop sync is disabled. Enable only for a consented local demo."},
        status=404,
    )


@require_POST
@csrf_exempt
def sync_telemetry(request: HttpRequest) -> JsonResponse:
    """Create one review-required pack from explicit, pseudonymous demo telemetry."""
    if not _sync_is_enabled():
        return _sync_disabled_response()
    if len(request.body) > 8_192:
        return JsonResponse({"error": "Telemetry request is too large."}, status=400)
    try:
        raw_telemetry = json.loads(request.body)
    except (UnicodeDecodeError, json.JSONDecodeError):
        return JsonResponse({"error": "Telemetry must be a JSON object."}, status=400)
    if not isinstance(raw_telemetry, Mapping):
        return JsonResponse({"error": "Telemetry must be a JSON object."}, status=400)
    # Consent is checked before validating or processing any learning events.
    if raw_telemetry.get("demo_consent") is not True:
        return JsonResponse({"error": "Explicit demo consent is required before telemetry can sync."}, status=400)
    try:
        telemetry = TelemetryRequest.model_validate(raw_telemetry)
    except (ValidationError, ValueError) as exc:
        return JsonResponse({"error": f"Invalid telemetry: {exc}"}, status=400)

    existing = RemediationPack.objects.filter(request_id=telemetry.request_id).first()
    if existing:
        if existing.learner_id != telemetry.learner_id:
            return JsonResponse({"error": "request_id does not belong to this learner ID."}, status=409)
        return _pack_receipt(existing, status=200)

    try:
        pack = run_teaching_loop(telemetry)
    except AgentSwarmError as exc:
        return JsonResponse({"error": str(exc)}, status=503)
    except Exception:
        logger.exception("Teaching loop failed before a remediation pack was created")
        return JsonResponse({"error": "The teaching loop could not prepare a remediation pack."}, status=502)

    try:
        stored = RemediationPack.objects.create(
            learner_id=telemetry.learner_id,
            request_id=telemetry.request_id,
            content_version=pack.content_version,
            telemetry_digest=telemetry_digest(telemetry),
            payload=pack.model_dump(mode="json"),
        )
    except IntegrityError:
        stored = RemediationPack.objects.get(request_id=telemetry.request_id)
    return _pack_receipt(stored, status=201)


@require_GET
def sync_remediation(request: HttpRequest) -> JsonResponse:
    """Return the latest patch for a random local learner ID, or no content."""
    if not _sync_is_enabled():
        return _sync_disabled_response()
    try:
        learner_id = UUID(request.GET.get("learner_id", ""))
    except (TypeError, ValueError):
        return JsonResponse({"error": "learner_id must be a UUID."}, status=400)
    pack = RemediationPack.objects.filter(learner_id=learner_id).first()
    if pack is None:
        return JsonResponse({"status": "no_remediation"}, status=204)
    return JsonResponse({"status": "ready", "pack": pack.payload})


def _pack_receipt(pack: RemediationPack, status: int) -> JsonResponse:
    payload = pack.payload
    return JsonResponse(
        {
            "status": "ready",
            "processing_mode": "synchronous_demo",
            "pack_id": str(payload["pack_id"]),
            "content_version": pack.content_version,
            "review_status": payload["review_status"],
            "request_id": str(pack.request_id),
            "remediation_url": "/api/v1/sync/remediation",
        },
        status=status,
    )
