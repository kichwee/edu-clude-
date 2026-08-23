# EduCloud MVP

EduCloud is a hackathon prototype. It demonstrates an English Grade 3 Maths
rule-based Android tutor plus the same Grade 3 Maths cards through a safe
stateless USSD flow. Its optional, consented Autonomous Edge-to-Cloud Teaching
Loop turns three locally recorded incorrect regrouping attempts into a
review-required offline remediation patch. It is not a production learning,
telephony, or child-data service.

## What is included

- `android-app/` — Kotlin/Jetpack Compose tutor with an offline Grade 3 rule
  pack, quiz flow, and an on-device USSD simulator using the same cards.
- `edu-cloud/backend/` — demo-only Django health, readiness, USSD, SMS, and
  disabled-by-default remediation-sync routes. The service never dispatches a
  provider message or stores a phone number or learner name.
- `CONTENT_PROVENANCE.md` — internal source record for the Grade 3 rule pack.
- `content/grade3_rule_tutor_v1.json` — canonical Grade 3 rule content used by
  backend USSD and to generate Android's offline cards.
- `tools/generate_grade3_rule_tutor.py` — regenerates Android's offline cards;
  CI rejects a stale generated file.
- `GRADE3_MATH_RAG_MVP_PLAN.md` — the approved-source, retrieval, model-gate,
  and release plan for the Grade 3 Mathematics tutor.
- `tools/extract_permissioned_textbook.py` — private staging extractor for a
  textbook covered by written permission; it does not create a learner-ready
  content pack.
- `MVP_LAUNCH_CHECKLIST.md` — release evidence and remaining gates.

## Local verification

Prerequisites: Python 3.11+ and Android Studio (for the Android project).

### Backend demo API

Run these commands in PowerShell from `edu-cloud/backend`:

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements-mvp.txt
$env:DJANGO_SECRET_KEY = 'replace-with-a-new-local-secret'
.\.venv\Scripts\python.exe manage.py migrate
.\.venv\Scripts\python.exe manage.py check
.\.venv\Scripts\python.exe manage.py test educloud.tests -v 2
.\.venv\Scripts\python.exe manage.py runserver
```

The available local-only routes are:

- `GET /api/v1/health`
- `GET /api/v1/ready`
- `POST /api/v1/demo/ussd`
- `POST /api/v1/demo/sms`

### Autonomous Teaching Loop — local demo only

The loop is disabled by default. For a fully local, deterministic demo—no API
request or API key—set these variables before `runserver`:

```powershell
$env:EDGE_SYNC_MODE = 'demo'
$env:AGENT_SWARM_MODE = 'fixture'
.\.venv\Scripts\python.exe manage.py runserver
```

It accepts only an explicit-consent payload containing a random installation
UUID and three to ten failed two-digit-subtraction attempts. The backend runs
the separate Assessor → Differentiator → Compiler contracts, persists the
finished JSON patch, and returns it from:

- `POST /api/v1/sync/telemetry`
- `GET /api/v1/sync/remediation?learner_id=<random-uuid>`

For a real OpenAI demonstration, set `AGENT_SWARM_MODE=openai`,
`OPENAI_API_KEY`, and optionally `OPENAI_MODEL=gpt-4o-mini` in the server
environment. Do not put an API key in the APK, source control, or a screen
recording. The Android client only enables the manual sync button when built
with an HTTPS endpoint:

```powershell
.\gradlew.bat -PedgeSyncBaseUrl=https://your-approved-demo-host assembleDebug
```

After three incorrect regrouping answers, the learner must tap the explicit
consent dialog. The app sends only the selected maths answers, never the
learner alias, chat messages, phone number, or hardware identifier. Downloaded
content stays in Room after automatic schema, scope, provenance-label, and arithmetic
validation; a learner does not wait for a teacher to unlock it.

### Android tutor

Open `android-app` in Android Studio. To run its unit tests in PowerShell,
set `JAVA_HOME` to Android Studio's bundled JBR and run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest
```

For an interactive demo, build and launch the `app` configuration from Android
Studio on an emulator or device. Test the lesson, quiz, low-memory toggle, and
feature-phone simulator. A successful physical-device run remains a release
gate; do not claim low-end device support without that evidence.

After changing `content/grade3_rule_tutor_v1.json`, regenerate Android's offline cards:

```powershell
cd edu-cloud/backend
.\.venv\Scripts\python.exe ..\..\tools\generate_grade3_rule_tutor.py
```

The Android simulator and backend USSD both render the same Grade 3 content
pack. Their numeric path never encodes a phone number, session, profile,
score, or learner record.

### Permissioned textbook staging

Do not add a textbook PDF, private permission record, or extracted staging JSON
to source control. The `content-input/` and `private-content-staging/` folders
are ignored deliberately.

With `pypdf` installed, extract a privately permissioned source into a staging
manifest:

```powershell
python tools\extract_permissioned_textbook.py `
  'C:\path\to\Grade 3 Mathematics.pdf' `
  'private-content-staging\g3-math-staging.json' `
  --source-id g3-math-pupils-book `
  --version private-staging-1.0
```

The output is page-level staging data only. The current MVP build uses four
shared Term One rule cards (Weeks 1–2), each with a source reference. The
staging output is not compiled into the Android app or learner retrieval.

## Safety and scope

- The local working build contains unreviewed records derived from a privately
  supplied Grade 3 Mathematics book. Every tutor response names the Term,
  Week, Lesson, PDF page range, printed-book page range, and pack version.
- The source permission record is still incomplete in this repository. Do not
  publish this content pack, the APK containing it, or a public repository
  containing the extracted lesson text until the owner records the exact
  written-permission scope in `CONTENT_PROVENANCE.md`.
- The optional model route is disabled. It remains blocked until a model passes
  the factual-grounding, source, word-limit, runtime, and Android-device gates
  in `MODEL_EVALUATION.md`.
- The old cloud-comparison idea is deliberately not implemented. The teaching
  loop remains disabled unless a local demo opts in; its OpenAI mode also needs
  an approved key and budget. The fixture mode is the verified demo fallback.
- Generated remediation is original AI-assisted Grade 3 Maths practice, never
  KICD/KEC curriculum content. It is delivered only when automatic schema,
  supported-scope, provenance-label, and arithmetic checks pass.
- Africa's Talking or any other live telephony integration is intentionally
  disabled. The included SMS/USSD flows are simulations only.
- Do not expose the demo API publicly or enable the unfinished `apps/` modules.

See [MVP_LAUNCH_CHECKLIST.md](MVP_LAUNCH_CHECKLIST.md) for required release
evidence and [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the scope and
implementation decisions.
