# EduCloud MVP Launch Checklist

**Target:** English-only Grade 3 Maths Android demo with a consented, demo-only Autonomous Edge-to-Cloud Teaching Loop, plus safe USSD/SMS simulation.

**How to use this document:** This is the source of truth for release readiness. A checked box means it has been completed and verified as stated. An unchecked **release gate** blocks an external demo or deployment. I will update this checklist whenever I complete a milestone. Do not mark a claim, supplier quote, approval, or production integration complete without supporting evidence.

> **Assumption:** “ABC” below means **Africa's Talking (AT)**. If you meant another service, replace this section before using the checklist.

## 1. Built so far

- [x] Defined an English-only, Grade 3 Maths MVP scope in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md).
- [x] Created [CONTENT_PROVENANCE.md](CONTENT_PROVENANCE.md) and recorded the restriction on the privately supplied textbook source.
- [x] Added a locally bundled Grade 3 Maths rule pack: four shared Term One cards power Android chat, the Android USSD simulator, and backend USSD. The 142 page-derived staging records remain excluded from learner retrieval and Android compilation.
- [x] Switched Android MVP screens from bilingual/Kiswahili UI to English-only UI.
- [x] Implemented a retrieval-first Android teaching flow and a visible low-memory retrieval-only demonstration mode.
- [x] Made the active Android tutor deterministic: it uses verified local lesson facts, corrects known wrong answers, and returns a clear boundary for unmatched/off-topic questions rather than selecting an arbitrary lesson.
- [x] Rebuilt the active Android Grade 3 Maths home and learning-path screens around the approved storybook reference direction: warm cream surfaces, high-contrast navy type, rounded outlined cards, offline status, an interactive equal-shares visual, and a staged Maths journey. Verified on `SM-N986U1` on 18 July 2026; the supplied reference's multi-subject, cloud, guardian, and live-download screens were not represented as available MVP features.
- [x] Added a three-question English Maths quiz demonstration.
- [x] Replaced the misleading Android `LlamaInference` label with a clearly named retrieval-first tutor response engine; no local model claim remains in the MVP code.
- [x] Hardened Android chat state: bounded input, stable message identity, lifecycle-aware state collection, and recovery from tutor/storage errors.
- [x] Added an offline Android retrieval regression test.
- [x] Replaced fixed Android lesson selection with a local Room FTS4 lesson index seeded from the bundled content pack; learner questions remain on-device.
- [x] Added a visible, on-device USSD/SMS feature-phone simulator with explicit no-dispatch/no-phone-data messaging and regression tests.
- [x] Verified the feature-phone simulator build and unit-test suite (`BUILD SUCCESSFUL`); the simulator's USSD correct-answer and SMS STOP safeguards pass.
- [x] Fixed the FTS query-builder edge case where empty punctuation fragments could become invalid wildcard search terms; added regression coverage.
- [x] Restored the missing Navigation Compose dependency and fixed Android Kotlin compilation blockers in navigation, chat UI, and typography.
- [x] Registered `EduCloudApp` in the Android manifest so the app's repository container is created at runtime instead of causing a navigation startup crash.
- [x] Configured Room schema export for future migration review.
- [x] Corrected the Android KSP version reference that could not be resolved.
- [x] Replaced backend settings that imported missing production modules with a minimal, self-contained demo configuration.
- [x] Added a WSGI entry point and SQLite-by-default backend configuration.
- [x] Added a backend health endpoint: `GET /api/v1/health`.
- [x] Added a backend readiness endpoint: `GET /api/v1/ready`.
- [x] Added stateless simulated USSD endpoint: `POST /api/v1/demo/ussd`.
- [x] Added stateless simulated SMS endpoint: `POST /api/v1/demo/sms`.
- [x] Ensured simulated SMS/USSD never dispatches a provider message, stores a phone number, or creates a learner account.
- [x] Fixed the RAG reciprocal-rank-fusion threshold defect that could suppress every search result.
- [x] Added backend regression tests for health, the shared Grade 3 USSD menu, invalid choices, quiz, pack-contract fields, sandbox callback safety, SMS STOP, and JSON simulator requests. Verified 21 July 2026: twelve tests passed.
- [x] Added a checked-in GitHub Actions workflow that verifies the backend, regenerates Android's shared Grade 3 rule cards from its canonical JSON source, rejects generated-file drift, and runs Android unit tests. It must still be observed passing on the repository's remote CI before it is treated as hosted evidence.
- [x] Hardened demo simulator input parsing: bounded request/input sizes and malformed JSON now return HTTP 400 rather than raising an error.
- [x] Removed the known/default Django secret and made startup require `DJANGO_SECRET_KEY`.
- [x] Ran the Muqatta-Z code-auditor review on the demo code; production telecom routes remain intentionally unexposed.
- [x] Verified Python syntax using `compileall`.
- [x] Added a disabled-by-default Autonomous Edge-to-Cloud Teaching Loop: bounded anonymous subtraction telemetry passes through separate Assessor → Differentiator → Compiler contracts, which produce one versioned JSON remediation patch. The app delivers it after automatic schema, supported-scope, provenance-label, and arithmetic checks; a learner does not wait for teacher approval. The deterministic fixture path, persistence/download contract, structured-output OpenAI mock, and Android patch/telemetry contracts passed locally on 8 August 2026. It is not yet an end-to-end device or live-OpenAI verification.

## 2. Current blockers and facts to state accurately

- [x] **Release gate — install Django and run `manage.py check` plus tests.** Verified on 17 July 2026 with Django 5.0.6 in `edu-cloud/backend/.venv`: `manage.py check` passed and all five `educloud.tests` passed.
- [x] **Release gate — complete Android `testDebugUnitTest`.** `testDebugUnitTest --offline --no-daemon` completed with `BUILD SUCCESSFUL` on 21 July 2026. It verifies the four-card shared Grade 3 rule pack, deterministic tutor boundaries, generated-card versioning, and the Android USSD simulator's matching Grade 3 menu/answer path.
- [x] **Verification note — Android compilation completes on the local machine.** Automation can time out while Gradle finishes asynchronously, so JUnit result XML is used as the evidence of completion.
- [x] **Release gate — assemble a debug APK.** Verified on 19 July 2026: `android-app/.\\gradlew.bat assembleDebug --offline --no-daemon --console=plain` completed with `BUILD SUCCESSFUL`; the refreshed deterministic retrieval-only [app-debug.apk](android-app/app/build/outputs/apk/debug/app-debug.apk) was generated (31,193,960 bytes; SHA-256 `DD748A9FE19385A3C5A8622247E57D6028C8CE6D8480A891B4F23D3B0E6B8273`). The disabled LiteRT runtime is not packaged. Low-spec-device performance remains a separate release gate.
- [ ] **Release gate — install and run the app on at least one low-spec Android device or emulator.** The current code is not proof of low-end-device performance.
- [ ] **Release gate — create a public repository and README.** The hackathon submission needs reproducible setup instructions and the actual source.
- [ ] **Release gate — record a public demo video under three minutes.** Show the working product, not mock screens only.
- [ ] **Release gate — retain the Codex feedback-session ID required by the hackathon.**

### Statements we can make now

- [x] “EduCloud is an English-only Grade 3 Maths hackathon prototype.”
- [x] “It demonstrates local retrieval-first tutoring, a low-memory comparison mode, and feature-phone interaction simulations.”
- [x] “The local MVP has four shared Grade 3 Maths rule cards with source references.”

### Statements we must not make yet

- [ ] “The app works offline on all low-end Android phones.” Requires physical-device testing and packaged content/model evidence.
- [ ] “The app is powered by a fine-tuned model.” Requires a documented dataset, training run, evaluation, model artifact, and licence verification.
- [ ] “The on-device instruction-model baseline gives safe, source-grounded explanations.” SmolLM2 and Gemma failed factual quality checks; Qwen3 retained verified answers but violated the brevity/runtime contract. No tested model is enabled. See [MODEL_EVALUATION.md](MODEL_EVALUATION.md).
- [x] “EduCloud has a sandbox-only inbound Africa's Talking USSD callback.” Verified 21 July 2026: the configured temporary HTTPS callback returned the Grade-selection menu after a public POST. It is stateless, ignores phone/session fields, stores no learner data, and has no outbound SMS/USSD capability. This is not evidence of production readiness or a persistent deployment.
- [ ] “EduCloud sends real parent reports/SMS/USSD.” Requires consent, identity and opt-out safeguards, provider integration, and test evidence.
- [ ] “Official KICD/KEC curriculum content is in the product.” Requires a provenance record that identifies the approved material and the written permission scope.
- [ ] Any price, adoption percentage, performance number, or quoted claim without a dated primary source. Mark it as an estimate or remove it.

## 3. Milestone A — make the demo reproducible

- [x] Install the minimum backend dependency.
  - Command: `python -m pip install -r edu-cloud/backend/requirements-mvp.txt`
  - Acceptance evidence: 17 July 2026 — Django 5.0.6 installed in `edu-cloud/backend/.venv`; `manage.py check` exited 0.
  - Owner: agent or you, depending on local network/package access.
- [x] Run backend tests.
  - Command: `python edu-cloud/backend/manage.py test educloud.tests -v 2`
  - Acceptance evidence: 17 July 2026 — five tests passed: health, JSON simulator request, malformed/oversized JSON rejection, SMS STOP, and USSD quiz answer.
  - Release gate: yes.
- [x] Complete Android unit-test/build run.
  - Command: from `android-app`, set `JAVA_HOME` to Android Studio JBR, then run `./gradlew testDebugUnitTest`.
  - Acceptance evidence: 19 July 2026 — `testDebugUnitTest --offline --no-daemon --console=plain` completed with `BUILD SUCCESSFUL`; all 26 tests passed, including deterministic tutor correction, numeric-question routing, the five retrieval regressions, content-index refresh/ranking, and generated all-term source-pack coverage.
  - Release gate: yes.
- [x] Install Android app on an Android test device.
  - Acceptance evidence: 17 July 2026 — installed `app-debug.apk` through ADB on an authorized Android 13 `SM-N986U1`; verified the Maths lesson menu, offline tutor response with source, low-memory toggle, and quick-check answer/score flow.
  - Acceptance evidence: 18 July 2026 — reinstalled the deterministic retrieval-only debug APK through authenticated Wi-Fi ADB on `SM-N986U1`; observed the Grade 3 Maths main path, the explicit no-send USSD/SMS simulator notice, and the tutor correctly reject “Is 7 mangoes correct?” with the verified answer 6 and local source.
  - Acceptance evidence: 19 July 2026 — installed the Term One source-pack APK via Wi-Fi ADB on `SM-N986U1`; verified the corrected Term One entry labels, retrieval-first tutor state, disabled model baseline, and the answer “3 tens and 6 ones” with `Term 1 · Week 2 · Lesson 2 · PDF page 22–24 · Book page 14–16` source proof.
  - Acceptance evidence: 19 July 2026 — enabled the on-device low-memory retrieval-only toggle on `SM-N986U1`; “How to count in twos” returned the local lesson, the 511/513/515 → 517/519 teaching steps, and `Term 1 · Week 2 · Lesson 1 · PDF page 21–22 · Book page 13–14` source proof.
  - Acceptance evidence: 22 July 2026 — assembled debug APK (`app-debug.apk`) and installed via stream ADB onto connected device `R83W501RHAH` (`Performing Streamed Install Success`); verified Duolingo-style 3D tactile buttons, progress bars, mascot coach bubbles, slide-up feedback sheets, and 139-lesson Grade 3 RAG explainer.
  - Limitation: this is a higher-capability device, not evidence of low-end-device performance.
- [x] Add one-command local demo instructions to the README.
  - Acceptance evidence: 17 July 2026 — the root `README.md` documents isolated backend verification/server commands and Android Studio/unit-test launch steps, with simulator and safety limits.

## 4. Milestone B — Autonomous Edge-to-Cloud Teaching Loop

- [x] Define and validate a versioned remediation-pack contract. The backend rejects invalid/correct arithmetic, unsupported skills, missing consent, and oversized telemetry; the Android client revalidates downloaded arithmetic before persisting it.
- [x] Implement a synchronous demo pipeline with independently structured Assessor, Differentiator, and Compiler stages. The fixture pipeline and a mocked three-call `gpt-4o-mini` SDK path passed backend tests on 22 July 2026.
- [x] Add manual Android sync after an explicit consent dialog. It sends only three recorded incorrect regrouping selections under a random installation UUID, downloads the patch over HTTPS, stores it in Room, and merges it into local retrieval.
- [ ] Provide a provider project, OpenAI API key, explicit budget cap, and approved model choice for a live OpenAI demo. Keep the key server-side and set `AGENT_SWARM_MODE=openai`; fixture mode remains the no-key fallback.
- [ ] Record an end-to-end device demonstration using an approved HTTPS endpoint, then verify the offline tutor retrieves the downloaded automatically validated lesson after the connection is removed.
- [ ] Complete device authentication, durable guardian consent, rate limiting, audit logging, retention/deletion controls, and a privacy/security review before any production use.

## 5. Milestone C — real Africa's Talking (AT) USSD/SMS

### Required from you before any live connection

- [ ] Confirm that **ABC means Africa's Talking** or provide the correct provider name.
- [ ] Provide an AT sandbox account, not production credentials.
- [ ] Provide the sandbox username, API key, callback/webhook configuration access, and approved sender/shortcode details.
- [ ] Confirm the intended audience, consent wording, and whether minors can use the channel.
- [ ] Approve a cost cap and the exact SMS/USSD flows to test.
- [ ] Keep credentials in a local `.env` file or secret manager; never paste them into the PRD, repository, screenshots, or chat.

### Build steps

- [ ] Replace the demo simulator adapter with a provider adapter behind a feature flag (`TELEPHONY_MODE=simulator|sandbox|production`).
- [ ] Verify provider webhook signatures using a documented secret/header mechanism; reject invalid or missing signatures in every environment.
- [ ] Implement idempotency/replay protection using the provider message/session ID.
- [ ] Implement a profile-selection flow for shared phones; never select the “latest learner” by phone number.
- [ ] Add parent/guardian consent before associating a phone number with a child profile.
- [ ] Generate a non-guessable parent-link token; do not use a public learner alias as authorisation.
- [ ] Persist and enforce STOP/opt-out before any outbound message is queued.
- [ ] Add provider sandbox tests for START, MATH, QUIZ, invalid input, STOP, retry, replay, and shared-phone flows.
- [ ] Obtain a dated supplier quote and confirm USSD/SMS billing before enabling live service.

**Release gate:** No real message, USSD session, parent report, or child data collection may be enabled until every item in this section is checked.

## 6. Milestone D — content and teacher quality

- [ ] Add every proposed content item to [CONTENT_PROVENANCE.md](CONTENT_PROVENANCE.md): source, licence/permission, permitted use, exact location, reviewer, and version.
- [ ] Attach the written KEC/KICD approval you received and record its scope. The public KEC site alone is not evidence of redistribution/embedding permission.
- [ ] Have a Kenyan Grade 3 Maths teacher review each lesson, answer, distractor, and explanation.
- [ ] Add a review date, reviewer name/role, and correction log for each published item.
- [ ] Build a teacher-reviewed question bank before fine-tuning.
- [ ] Keep Kiswahili out of the MVP; create a separate later evaluation plan before adding it.

## 7. Milestone E — small-model work (after MVP is demonstrable)

- [ ] Choose one candidate after licence and device testing: Gemma 3 270M, Qwen 2.5 0.5B, or another English-capable model.
- [ ] Record licence, model card, quantisation, file size, RAM use, Android runtime, and tested devices.
- [ ] Define a narrow training task: Grade 3 Maths explanation style, not unrestricted “AI teacher” knowledge.
- [ ] Create a licensed, teacher-reviewed training and evaluation set with no child personal data.
- [ ] Establish a baseline using retrieval-only answers before training.
- [ ] Fine-tune only if the evaluation beats the baseline on accuracy, age-appropriate language, groundedness, latency, and device memory.
- [ ] Run a human teacher safety review and document failure cases.
- [ ] Package the model only after its licence permits the intended distribution.

**Release gate:** Do not claim fine-tuning or deploy a model without the completed evidence above.

## 8. Milestone F — production foundations (not required for Monday demo)

- [ ] Restore production Django modules one at a time with migrations, tests, and authenticated routes.
- [ ] Implement device authentication without trusting client-supplied learner ability (`theta`).
- [ ] Persist interactions and mastery events; use fixed 1/3/7/14/30-day reviews before considering IRT calibration.
- [ ] Build a secure consent, child profile, guardian-link, and data-deletion process aligned with ODPC requirements.
- [ ] Implement actual opt-out, audit logs, rate limits, background job queue, monitoring, and backups.
- [ ] Replace default passwords/secrets, restrict Docker service exposure, and add CI.
- [ ] Obtain security and privacy review before a school pilot.

## 9. What I can do vs. what requires you or an external party

| Work item | Agent can build/check | You or external party must provide |
| --- | --- | --- |
| Android MVP and demo API | Yes | Device/emulator access for final demonstration |
| Local content/quiz flow | Yes | Teacher review and content approval |
| Autonomous teaching-loop implementation | Yes | Provider account, key, budget approval for a live model demo; teacher review and safety sign-off for any wider use |
| Small-model evaluation tooling | Yes | Model/distribution licence decision and compute funding |
| AT sandbox integration | Yes | AT account, sandbox credentials, webhook settings, cost approval |
| Real telephony launch | Partly | Provider contract, shortcode/sender approval, consent language, legal/privacy sign-off |
| KEC/KICD content use | Can record provenance | Written permission files and confirmation of allowed uses |
| Hackathon submission | Can prepare project/README/demo script | Your public repository access, video upload, submission action, and any required account login |

## 10. Immediate next actions

1. [ ] You confirm whether “ABC” means Africa's Talking.
2. [ ] You decide whether to give me sandbox-only telephony credentials after the local demo passes.
3. [ ] We complete Django installation, backend checks/tests, and the Android build.
4. [ ] You test the installed Android app and report any device-specific issue.
5. [ ] You provide an OpenAI API key and approved spend cap if you want a live-model recording; otherwise use the verified fixture loop in the demo.
6. [ ] We prepare the repository README, three-minute demo script, and submission evidence.
