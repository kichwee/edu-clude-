# EduCloud — Implementation Plan and Monday Delivery Checklist

> Created: 17 July 2026 (EAT)  
> Target: OpenAI Build Week 2026 challenge  
> Internal feature-complete deadline: Monday, 20 July 2026, 18:00 EAT  
> Submission deadline: Tuesday, 21 July 2026, 17:00 PT / Wednesday, 22 July 2026, 03:00 EAT

## 1. Scope lock

### Monday demo

Build **EduCloud Proof-Carrying Tutor**: an English-only Grade 3 Mathematics tutor for low-end Android phones. It must teach from a small teacher-reviewed, provenance-labelled content pack, save local progress, and visibly remain useful when the device cannot load a local model.

The demo has three connected surfaces:

1. **Android offline tutor (primary).** A learner takes a short diagnostic, receives an explanation grounded in the local pack, answers a check-for-understanding question, and gets a next-review recommendation.
2. **Cloud-enhanced comparison (secondary).** With connectivity, the same lesson can request a more natural explanation from a cloud model through a backend. The response must still contain the retrieved source and must never be required for the core learning flow.
3. **USSD/SMS companion (secondary).** A Django service provides a real menu/state machine and SMS command parser. A built-in phone simulator is the guaranteed presentation path; Africa's Talking Sandbox is enabled only when credentials and a public HTTPS callback are available.

### Explicit Monday non-goals

- Kiswahili, other local languages, voice/IVR, parent reports, certificates, School Hub, P2P, federated learning, and production rollout.
- A live production shortcode, bulk SMS, a production API key, real children, or real learner analytics.
- Claiming calibrated IRT, curriculum-wide coverage, clinical learning efficacy, or nationwide readiness.
- Training on whole KEC documents or publishing KEC-derived model weights.

### Acceptance criteria

- [ ] Android app builds and has no Kiswahili-facing experience.
- [ ] One complete Grade 3 Maths lesson path works without network access.
- [ ] Every displayed lesson/explanation names its source pack, grade, topic, and content version.
- [ ] A deliberate “low memory” toggle demonstrates the retrieval-first fallback.
- [ ] Cloud mode is opt-in, resilient to failure, and clearly labelled.
- [ ] USSD simulator completes menu → question → answer → explanation; SMS simulator completes `START`, `MATH`, `QUIZ`, `HELP`, and `STOP`.
- [ ] README, test instructions, sample data, demo script, and a <3-minute video are ready.

## 2. Decisions and alternatives

| Decision | Alternatives considered | Chosen | Why |
|---|---|---|---|
| Demo surface | Web-only; Android-only; Android plus cloud comparison | Android plus small cloud comparison | It proves the low-end-device claim while showing the quality ceiling when connectivity exists. |
| Teaching language | English + Kiswahili; English only | English only | Removes translation and voice-quality risk before Monday. |
| Content scope | Grades 3–4/all subjects; Grade 3 Maths | Grade 3 Maths | A small reviewed pack is demonstrable and credible. |
| Edge model | Gemma 3 270M; Qwen2.5 0.5B; no local model | Benchmark Gemma 3 270M and Qwen2.5 0.5B; ship retrieval fallback regardless | The fallback is reliable on 2 GB devices; no untested local model becomes a blocker. |
| Telephony | Live production USSD/SMS; sandbox only; simulator only | Simulator-first, Sandbox optional | It guarantees a working demo without waiting for telecom provisioning. |
| Adaptation | Per-learner 2PL calibration; fixed mastery bands | Fixed mastery bands + 1/3/7/14/30 review schedule | 2PL item calibration cannot be justified from a few learner responses. |

## 3. Research and PRD corrections

The source review was systematic against primary/current sources where available; it is not a claim to have verified the entire internet. Each changed factual statement should carry a date and source URL in the PRD.

### Mandatory PRD edits

- [ ] **Correct the CBC structure** from `2-6-6-3` to `2-6-3-3-3`; cite the current official KICD curriculum source in the revised PRD.
- [ ] **Replace the problem-statement statistics.** Remove the unqualified “64% have no internet,” “87% own a phone,” and “most are feature phones” claims. Use the CA/KNBS 2023–24 framing: 61.6% of households nationally had at least one internet user; the urban/rural gap was 81.9% versus 48.8%; access and affordability remain material barriers. [CA/KNBS ICT analytical report](https://www.ca.go.ke/sites/default/files/2025-08/ICT%20Analytical%20Report%20Based%20on%202023-2024%20Kenya%20Housing%20Survey_0.pdf)
- [ ] **Replace retired Gemini defaults.** `gemini-1.5-flash` must not be the default. Pin a currently supported model selected at implementation time, retain a provider abstraction, and document its date, model ID, pricing assumptions, and fallback behavior. [Gemini deprecations](https://ai.google.dev/gemini-api/docs/deprecations)
- [ ] **Make content provenance a release gate.** KEC identifies KICD as its operator but displays “all rights reserved.” Written approval must explicitly cover collection, storage, transformation/chunking, embedding, any model tuning, and public hackathon demonstration; approvals must be mapped per source/asset. [Kenya Education Cloud](https://kec.ac.ke/)
- [ ] **Replace the IRT promise.** “2PL calibration after 10 interactions” is invalid. Use teacher-reviewed difficulty bands and deterministic spaced review in the MVP. Later 2PL must use a pretested item bank and a field-trial plan with hundreds of responses per item; sources describe stable 2PL estimates as requiring large samples. [IRT sample-size review](https://pmc.ncbi.nlm.nih.gov/articles/PMC4096146/)
- [ ] **Make child-data safeguards specific.** No reports, account linking, or free-form cloud tutoring without a verified consent design, minimal data collection, deletion path, and DPIA review. Use the ODPC children's-data guidance as the baseline. [ODPC guidance](https://www.odpc.go.ke/wp-content/uploads/2025/11/ODPC-%E2%80%93-Guidance-Note-for-Processing-Childrens-Data.pdf)
- [ ] **Reframe the model decision.** `multilingual-e5-small` is a 384-dimension multilingual retrieval candidate, not a proven Swahili solution; benchmark it before a Swahili claim. For Monday, evaluate English-only edge candidates. Gemma 3 270M requires accepting the Gemma licence; Qwen2.5 0.5B has Apache-2.0 GGUF variants, including Q4_K_M around 491 MB. [Gemma 3 270M](https://huggingface.co/google/gemma-3-270m-it), [Qwen2.5 0.5B GGUF](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF), [multilingual-e5-small](https://huggingface.co/intfloat/multilingual-e5-small)
- [ ] **Demote unverified voice/TTS claims.** Do not state that a Swahili Kokoro/Piper model is commercially ready until model-weight licence and quality tests are recorded. Voice is post-hackathon.
- [ ] **Replace all cost tables with sourced ranges.** Retain no AI, telephony, hardware, or cloud number without a dated supplier quote, exchange-rate date, usage assumptions, and token mix. Resolve the conflict between the Track B model unit cost and the pilot-derived cost.
- [ ] **Resolve timeline conflicts.** Track B cannot both precede the Week 8 pilot and launch in months 3–6. Monday’s MVP is Track A plus a simulated Track B companion; production Track B is a separately approved experiment.

### Research document clean-up

- [ ] `PHD_RESEARCH_PAPERS.md`: preserve foundational papers, but label unattributed claims as hypotheses until a stable DOI, publisher, or author-hosted paper is linked. Remove exact outcome/latency/cost claims that cannot be reproduced from the cited paper. Correct the implication that Bloom’s 2σ result means an LLM equals a human tutor.
- [ ] `RESEARCH_FOUNDATION.md`: change claims of “proven on 2GB phones” to a device-test hypothesis; replace the contradictory 2-6-6-3 structure; remove unvalidated product scale and model-memory claims; state that RAG reduces but does not eliminate hallucination.
- [ ] `DIFFERENTIATION_BRAINSTORM.md`: change “no competitor has done” statements to attributable, time-bounded competitive observations; remove the unsupported 97% WhatsApp claim; move the parent loop, voice, hub, mesh, certificates, and federation to post-MVP.
- [ ] `PRD.md`: reconcile its React Native/`llama.rn` architecture with the actual Kotlin/Jetpack Compose Android project before any future milestone is committed.

## 4. Verified codebase state and required recovery work

### Current state

The repository has two partial mobile implementations:

- `android-app/` is a Kotlin/Jetpack Compose prototype with Room persistence, subject/quiz/chat screens, and tests. Its RAG and LLM classes are explicitly keyword/canned-response stubs; it contains no model, model runner, bundled content, or real vector search.
- `edu-cloud/apps/mobile/` has only `package.json` and three TypeScript modules. It is not a React Native application scaffold.

The Django service is not runnable as checked in:

- [ ] `settings.py` and `urls.py` reference missing modules/files such as `wsgi.py`, middleware, sync authentication, i18n, exceptions, Prometheus view, all route modules, `apps.voice`, and `apps.certificates`.
- [ ] There are no Django app configurations, migrations, tests, CI workflow, backend container, or environment template.
- [ ] The RAG database schema script exists, but no approved content pack is populated.
- [ ] `manage.py check` cannot run in the current environment because Django is not installed; Python syntax compilation is not runtime verification.

### High-priority defects to correct after the Monday demo

- [ ] **RAG threshold defect:** RRF returns a maximum score of roughly 0.0328 for this top-3 implementation, while the filtering arithmetic effectively requires >=0.06. This drops every result. Replace the threshold with a score model validated by retrieval tests, or do not threshold the RRF score.
- [ ] **Sync security:** endpoint lacks device authentication and accepts client-supplied `theta`. Authenticate device tokens, validate server-owned progress events, and make sync idempotent.
- [ ] **Shared-phone identity:** USSD/SMS use the most recently active student for a phone, SMS does not create a student, and there is no profile choice. Implement a non-identifying profile selector before persisting progress.
- [ ] **Parent privacy:** `JOIN` accepts a guessable/non-unique alias and `STOP` does not opt a phone out of messaging. Do not enable parent reports until the consent and opt-out workflow is redesigned and tested.
- [ ] **Telecom webhook security:** signature validation must fail closed outside local simulation; rate-limit and replay-protect requests; enqueue SMS delivery rather than send in the webhook request.
- [ ] **I18n mismatch:** hard-coded Swahili/English strings contradict the declared i18n requirement. Monday removes Kiswahili; later externalize all UI and message strings.

## 5. Five-day execution checklist

### Friday, 17 July — scope, evidence, and runnable foundation

- [ ] Add this plan and an ADR recording scope decisions.
- [ ] Add `CONTENT_PROVENANCE.md` with an empty approval register and the one permitted Grade 3 Maths source pack.
- [ ] Choose one local model candidate only after confirming licence acceptance and download size.
- [ ] Make the Android project the source of truth; mark the incomplete React Native directory as archived/experimental.
- [ ] Make Django minimally runnable: missing package files, environment template, local SQLite development mode, health route, route modules, migrations, and test command.
- [ ] Implement a static USSD/SMS simulator API and fixed Grade 3 question bank; do not wait for provider access.

### Saturday, 18 July — Android learning loop

- [ ] Remove Kiswahili selection, labels, subjects, and content from the Android MVP.
- [ ] Restrict the app to Grade 3 Mathematics.
- [ ] Add content-pack loading, content-version display, source cards, and deterministic retrieval over a small bundled JSON/SQLite pack.
- [ ] Replace canned chat behavior with the fixed teaching sequence: retrieve → explain in steps → ask one check question → record mastery band → schedule review.
- [ ] Add a visible “low memory / retrieval-only” demo switch; it must deliver the same lesson without generation.
- [ ] Persist interactions and the 1/3/7/14/30 review schedule locally.

### Sunday, 19 July — cloud comparison and telephony companion

- [ ] Add a backend provider interface and one cloud tutor endpoint, with server-side secret only, retrieval context, low temperature, source citation, timeout, rate limit, and deterministic fallback.
- [ ] Build an Android compare view: “Offline lesson” versus “Cloud-enhanced explanation”; label the latter as optional.
- [ ] Complete USSD menu flow and SMS keyword flow using the same question/content data.
- [ ] Add a browser-visible simulator screen or test fixture showing exact requests/responses.
- [ ] If credentials are supplied: configure Africa’s Talking Sandbox only, using a public HTTPS tunnel/deployment and request validation. If not, keep it simulated and state that plainly.

### Monday, 20 July — quality, packaging, and evidence

- [ ] Unit-test content retrieval, review scheduling, USSD state transitions, SMS commands, and cloud-failure fallback.
- [ ] Build the Android debug/release APK; smoke-test onboarding, diagnostic, lesson, low-memory mode, and compare mode.
- [ ] Run Django checks/migrations/tests and a manual API smoke test.
- [ ] Create `README.md`, architecture diagram, setup guide, sample data disclaimer, licence notices, threat-model note, and a known-limitations section.
- [ ] Capture the 2:40–2:55 demo video: problem → offline learner flow → source proof → low-memory fallback → cloud comparison → USSD/SMS simulator → impact/limits.
- [ ] Capture Codex session/feedback ID and document where Codex/GPT-5.6 accelerated the work, as required by the challenge. [OpenAI Build Week requirements](https://openai.devpost.com/)

### Tuesday buffer — submit

- [ ] Upload the public video, repository URL, description, category, and Codex feedback session ID.
- [ ] Verify the repository can run from a clean checkout and contains no API key, phone number, child data, KEC content without its recorded authorisation, or misleading claims.
- [ ] Submit before 03:00 EAT on 22 July.

## 6. What the agent can build versus what needs the team

### I can build in this workspace

- Android Compose screens, English-only content flow, local persistence, retrieval-first fallback, deterministic review scheduler, and model capability checks.
- A minimal runnable Django API, USSD/SMS state machines, simulator, provider abstraction, safe cloud endpoint, migrations, tests, Docker/CI files, and developer documentation.
- Content schema, provenance register/template, teacher-review checklist, evaluation dataset format, retrieval tests, threat-model documentation, demo script, README, and submission checklist.
- Static analysis, local builds/tests when dependencies and toolchains are available, and integration tests using fixtures.

### Requires you or an authorised external party

- [ ] Provide a redacted written KEC/KICD permission record with the permitted assets and exact reuse rights.
- [ ] Provide teacher-reviewed Grade 3 Maths material/questions or authorise a named, clearly licensed OER source.
- [ ] Create/provide Africa’s Talking Sandbox credentials and approve a public HTTPS callback; production shortcodes/sender IDs require provider arrangements.
- [ ] Create/provide a cloud-model API key and budget, then approve the chosen provider/model. Secrets must be added locally, never pasted into chat or committed.
- [ ] Test on at least one actual target Android device; emulator success is not a low-memory-device validation.
- [ ] Own legal/compliance decisions, parental-consent process, DPIA/DPO review, provider contracts, content licensing, and final Devpost submission/account actions.
- [ ] If LoRA is pursued, supply/approve a GPU account and teacher-authored training/evaluation examples. I can prepare and run the reproducible training recipe; I cannot assert it is pedagogically safe without teacher review.

## 7. Fine-tuning gate

Do not let adapter training delay the demonstrable product.

- [ ] Create 75–150 teacher-authored English tutoring-style examples; do not use whole KEC passages as training targets.
- [ ] Hold out at least 25 unseen Grade 3 Maths prompts, including refusal/off-topic cases.
- [ ] Benchmark base model and adapter on helpfulness, factual grounding, grade appropriateness, and source adherence.
- [ ] Promote the adapter only when it improves the held-out evaluation and stays within device memory/latency limits.
- [ ] Otherwise present the local-model experiment as an optional research branch and ship retrieval-only plus cloud comparison.

## 8. Estimated usage, tokens, and hard-cost control

These are planning ranges, not a bill. Codex subscription/event credits are not publicly convertible to a fixed token price, so confirm actual remaining credits in the OpenAI/Devpost interface before any paid call. Do not assume a credit amount from this document.

| Workstream | Estimated agent tokens | Estimated cloud-model tokens | Spend control |
|---|---:|---:|---|
| Audit, PRD/doc corrections, plan, provenance | 150k–350k | 0 | Local work only |
| Android and Django implementation | 600k–1.4m | 0–100k | Use tests/fixtures first |
| Debugging, builds, tests, README, video assets | 400k–900k | 0–100k | Reuse a fixed prompt/evaluation set |
| Optional cloud comparison/evaluation | 100k–250k | 100k–500k | Set a hard server-side quota and rate limit |
| **Monday total** | **1.25m–2.9m** | **100k–700k** | Stop optional model calls at the agreed cap |

Suggested operational cap: reserve all hackathon-provided Codex credits for development; cap cloud-demo testing at **US$25–50** until the provider, model, and exact pricing are approved. Local inference and LoRA use GPU time rather than API tokens; budget that separately only after the evaluation gate passes.

## 9. Risks that block a truthful demo

- No explicit content-use approval or no teacher-reviewed source pack → use original sample questions only and disclose that the content pack is illustrative.
- No Android physical test device → demonstrate the low-memory switch, but do not claim 2 GB production performance.
- No telephony credentials/public callback → use the simulator, not a claim of live USSD/SMS integration.
- Local model does not fit or produces unreliable results → ship retrieval-first; do not fake local inference.
- Cloud key/provider unavailable → hide compare mode and retain the full offline flow.

## 10. Definition of done for Monday

The project is ready to submit only if a judge can clone the repository, run the documented demo, watch the Android learner complete one lesson offline, observe the source-backed fallback, see the optional cloud comparison and telephony simulator, and understand exactly what is prototype versus production. The project must make a credible, narrow impact claim rather than promise every device, every language, and every channel at once.
