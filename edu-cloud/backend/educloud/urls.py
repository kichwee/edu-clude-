"""URLs deliberately limited to the safe, self-contained hackathon demo."""

from django.urls import path

from . import mvp_views


urlpatterns = [
    path("api/v1/health", mvp_views.health, name="health"),
    path("api/v1/ready", mvp_views.ready, name="ready"),
    path("api/v1/demo/ussd", mvp_views.ussd_simulator, name="ussd-simulator"),
    path("api/v1/demo/sms", mvp_views.sms_simulator, name="sms-simulator"),
    path("api/v1/sync/telemetry", mvp_views.sync_telemetry, name="sync-telemetry"),
    path("api/v1/sync/remediation", mvp_views.sync_remediation, name="sync-remediation"),
    path("api/v1/tutor/reexplain", mvp_views.tutor_reexplain, name="tutor-reexplain"),
    path(
        "api/v1/sandbox/ussd/<str:callback_token>",
        mvp_views.ussd_sandbox_callback,
        name="ussd-sandbox-callback",
    ),
]
