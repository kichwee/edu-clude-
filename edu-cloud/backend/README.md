# EduCloud demo API

This is a self-contained **demo-only** API. It provides health checks and
stateless USSD/SMS simulators; it never invokes a telecom provider or stores
learner/phone data. A separate inbound-only USSD sandbox callback is built but
disabled by default. It has no outbound SMS implementation.

## Local verification

From `edu-cloud/backend` in PowerShell:

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements-mvp.txt
$env:DJANGO_SECRET_KEY = 'replace-with-a-new-local-secret'
.\.venv\Scripts\python.exe manage.py migrate
.\.venv\Scripts\python.exe manage.py check
.\.venv\Scripts\python.exe manage.py test educloud.tests -v 2
```

To run the local simulator API, retain `DJANGO_SECRET_KEY` in that shell and
run `.\.venv\Scripts\python.exe manage.py runserver`. The API is local-only;
do not expose it publicly or configure a telecom provider.

## Enabling an approved USSD sandbox callback

Only do this after Africa's Talking Sandbox credentials, a service/channel,
consent wording, and a public HTTPS callback have been approved. Set
`TELEPHONY_MODE=sandbox` and a unique 32+ character
`USSD_SANDBOX_CALLBACK_TOKEN` in the deployment's secret manager, then
configure this exact callback URL at the provider:

```
https://your-approved-host/api/v1/sandbox/ussd/<USSD_SANDBOX_CALLBACK_TOKEN>
```

The callback reads only the provider's `text` field. It intentionally ignores
phone number, session ID, and service code, returns a stateless `CON`/`END`
menu, and has a short gateway throttle. The current learning tree is:
`Learn → Grade (1–12) → Subject → Topic → Lesson → Check → feedback`. Only the
small, original Grade 3 Maths demonstration branch is available; every other
grade and subject returns an explicit unpublished state. The examples are
automatically validated original content, not KICD/KEC content. It must be presented as
**Sandbox**, not a live service. Keep the token out of source control,
screenshots, and demo recordings.

Do not enable the unfinished modules in `apps/` for a public deployment. They
need separate authentication, consent, persistence, provider, and security work.

## Autonomous Edge-to-Cloud Teaching Loop

This is a separate, **disabled-by-default** hackathon-only route. It accepts
only a random local learner UUID, explicit demo consent, and three to ten
incorrect two-digit subtraction attempts; it rejects names, phones, chat text,
unbounded input, correct attempts, and unsupported skills. It persists an
automatically validated remediation JSON patch, not the raw attempts.

To show the deterministic local demonstration:

```powershell
$env:EDGE_SYNC_MODE = 'demo'
$env:AGENT_SWARM_MODE = 'fixture'
.\.venv\Scripts\python.exe manage.py runserver
```

`fixture` runs the same Assessor → Differentiator → Compiler contract without
calling an external provider. To invoke OpenAI’s structured-output stages,
use `AGENT_SWARM_MODE=openai`, set `OPENAI_API_KEY` in the server environment,
and keep `OPENAI_MODEL=gpt-4o-mini` (or an approved replacement) server-side.
There is deliberately no production mode: authentication, durable consent,
rate limits, audit logging, retention/deletion controls, and an operational
security review are required first.
