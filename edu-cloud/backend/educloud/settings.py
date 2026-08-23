"""Minimal, safe configuration for the Monday EduCloud demo API.

The incomplete production modules remain in ``apps/`` for future work, but are
not installed or routed by this configuration.  This keeps the demo runnable
without exposing unfinished authentication, parent-reporting, or telephony
integrations.
"""

import os
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent.parent

env_file = BASE_DIR / ".env"
if env_file.exists():
    for line in env_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            os.environ.setdefault(k.strip(), v.strip().strip("'\""))

SECRET_KEY = os.getenv("DJANGO_SECRET_KEY")
DEBUG = os.getenv("DEBUG", "false").lower() == "true"
ALLOWED_HOSTS = [host.strip() for host in os.getenv("ALLOWED_HOSTS", "localhost,127.0.0.1").split(",") if host.strip()]

# A public deployment must opt in to both its secret and debug mode.  Failing
# during startup is safer than accidentally serving a demo with a known key.
if not SECRET_KEY:
    raise RuntimeError("Set DJANGO_SECRET_KEY before starting EduCloud.")

INSTALLED_APPS = [
    "educloud.apps.EducloudConfig",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

ROOT_URLCONF = "educloud.urls"
TEMPLATES = [{
    "BACKEND": "django.template.backends.django.DjangoTemplates",
    "DIRS": [],
    "APP_DIRS": True,
    "OPTIONS": {"context_processors": [
        "django.template.context_processors.request",
        "django.contrib.auth.context_processors.auth",
        "django.contrib.messages.context_processors.messages",
    ]},
}]
WSGI_APPLICATION = "educloud.wsgi.application"

# SQLite makes the hackathon demo self-contained.  No learner data is collected
# by the demo API, and the telephony simulators are intentionally stateless.
DATABASES = {"default": {"ENGINE": "django.db.backends.sqlite3", "NAME": BASE_DIR / "db.sqlite3"}}

LANGUAGE_CODE = "en-gb"
TIME_ZONE = "Africa/Nairobi"
USE_I18N = True
USE_TZ = True
STATIC_URL = "static/"
DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"
DATA_UPLOAD_MAX_MEMORY_SIZE = 16 * 1024
DATA_UPLOAD_MAX_NUMBER_FIELDS = 10

# Python 3.14 + Django 5.0.6 incompatibility: the default ``mail_admins``
# handler renders a technical-500 template for every logged 5xx even when no
# ADMINS are configured, and that render crashes (template Context.__copy__).
# With an empty ADMINS list the handler could never deliver anything anyway,
# so request errors go to the console instead.  All other defaults stand.
from django.utils.log import DEFAULT_LOGGING

LOGGING = {
    **DEFAULT_LOGGING,
    "loggers": {
        **DEFAULT_LOGGING["loggers"],
        "django.request": {"handlers": ["console"], "level": "ERROR", "propagate": False},
    },
}

if not DEBUG:
    SECURE_CONTENT_TYPE_NOSNIFF = True
    X_FRAME_OPTIONS = "DENY"
    SESSION_COOKIE_SECURE = True
    CSRF_COOKIE_SECURE = True

# RRF scores are ranks, not cosine-similarity scores.  A default of zero keeps
# valid top-ranked matches from being silently removed.
RAG_MIN_RRF_SCORE = float(os.getenv("RAG_MIN_RRF_SCORE", "0"))
RAG_TOP_K = 3
RAG_EMBEDDING_MODEL = "intfloat/multilingual-e5-small"
RAG_DB_PATH = os.getenv("RAG_DB_PATH", str(BASE_DIR / "data" / "cbc_knowledge.db"))

# Telephony is opt-in.  The default is deliberately the local, stateless
# simulator.  ``sandbox`` only enables an inbound callback; it never grants
# this application permission to send an SMS or make another provider call.
TELEPHONY_MODE = os.getenv("TELEPHONY_MODE", "simulator").strip().lower()
if TELEPHONY_MODE not in {"simulator", "sandbox"}:
    raise RuntimeError("TELEPHONY_MODE must be 'simulator' or 'sandbox'.")

# This is a URL capability used solely for the provider's sandbox callback.
# It must be a long, randomly generated value kept in the deployment secret
# manager, never source control.  It is required before sandbox mode can start.
USSD_SANDBOX_CALLBACK_TOKEN = os.getenv("USSD_SANDBOX_CALLBACK_TOKEN", "")
if TELEPHONY_MODE == "sandbox" and len(USSD_SANDBOX_CALLBACK_TOKEN) < 32:
    raise RuntimeError(
        "Set a random USSD_SANDBOX_CALLBACK_TOKEN (32+ characters) before enabling sandbox mode."
    )

# The sandbox callback has no learner account or phone-number storage.  This
# process-local throttle retains only a hash of the gateway IP for one minute.
CACHES = {
    "default": {
        "BACKEND": "django.core.cache.backends.locmem.LocMemCache",
        "LOCATION": "educloud-demo-throttle",
    }
}

# The autonomous teaching loop is intentionally unavailable unless a local
# hackathon demonstration opts in.  This project has no device authentication
# or production child-data consent workflow, so there is no "production" mode.
EDGE_SYNC_MODE = os.getenv("EDGE_SYNC_MODE", "disabled").strip().lower()
if EDGE_SYNC_MODE not in {"disabled", "demo"}:
    raise RuntimeError("EDGE_SYNC_MODE must be 'disabled' or 'demo'.")

# ``fixture`` proves the whole edge-to-cloud contract without external calls.
# ``openai`` runs three schema-constrained stages only when a key is provided.
AGENT_SWARM_MODE = os.getenv("AGENT_SWARM_MODE", "openai").strip().lower()
if AGENT_SWARM_MODE not in {"fixture", "openai"}:
    raise RuntimeError("AGENT_SWARM_MODE must be 'fixture' or 'openai'.")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")

# Interest re-explanation (V2 Layer 2). Disabled unless a deployment opts in,
# mirroring the teaching loop's posture: no explicit mode, no route, no calls.
# ``fixture`` proves the re-explain contract without external calls; ``live``
# serves ox-alpha via OpenRouter first, with Claude Haiku and direct
# OpenAI/open-weight as backups.
# An empty value (e.g. a blank slot in .env) means the default: disabled.
REEXPLAIN_MODE = (os.getenv("REEXPLAIN_MODE", "disabled").strip().lower() or "disabled")
if REEXPLAIN_MODE not in {"disabled", "fixture", "live"}:
    raise RuntimeError("REEXPLAIN_MODE must be 'disabled', 'fixture' or 'live'.")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "")
ANTHROPIC_MODEL = os.getenv("ANTHROPIC_MODEL", "claude-sonnet-5")
# OpenRouter slot (OpenAI-compatible endpoint). Leave the key blank to skip it.
OPENROUTER_BASE_URL = os.getenv("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1")
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY", "")
OPENROUTER_MODEL = os.getenv("OPENROUTER_MODEL", "ox-alpha")
# Cost and abuse controls: one tap is one call, responses are cached per
# (lesson, domain), and each pseudonymous learner has an hourly tap cap.
REEXPLAIN_MAX_REQUESTS_PER_LEARNER_HOUR = int(os.getenv("REEXPLAIN_MAX_REQUESTS_PER_LEARNER_HOUR", "12"))
REEXPLAIN_CACHE_TTL_SECONDS = int(os.getenv("REEXPLAIN_CACHE_TTL_SECONDS", "86400"))
