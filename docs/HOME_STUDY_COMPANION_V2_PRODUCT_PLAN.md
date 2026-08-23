# EduCloud V2 — Home Study Companion: Product Plan & Proposal

> Version 0.1 | 22 August 2026 | Status: **PROPOSAL — Draft for stakeholder/grant review**
> Origin: Strategy session, 22 Aug 2026 (cofounder alignment + market deep dive).
> This document supersedes the *positioning* sections of `PRD.md` v1.2. It does not
> replace `MVP_LAUNCH_CHECKLIST.md` (still the readiness record) or
> `CONTENT_PROVENANCE.md` (still binding). The engineering MVP described there is
> complete and submitted; this plan defines what is built next.

---

## 1. What We Are Building (Executive Summary)

**A fun, AI-personalized home study companion** that learners download onto their
phones, laptops, and computers and use at home — not a school-administration or
classroom product.

- Learner opens the app at home, studies Grade-school material through an
  interactive tutor, earns streaks and milestones, and comes back tomorrow.
- When a concept does not land, one tap — **"Explain it my way"** — has a frontier
  cloud model re-explain the exact lesson through the learner's own declared
  interests (e.g., angles explained through football), while preserving verified
  answers and source citations.
- Over time the app learns each learner's struggle patterns and predicts which
  topic will trip them up next — before they hit it.
- Parents/caregivers can optionally link in via a consent code and receive a
  weekly digest. No teacher dashboard, no classroom rosters, no exam-paper
  marketplace in v1.

**One line:** *Duolingo's habit science + Zeraki's local-curriculum grounding +
an AI tutor that explains things your way — running at home, offline-tolerant,
pseudonymous by default.*

---

## 2. How This Compares With the Main PRD (`PRD.md` v1.2)

| PRD v1.2 said | V2 decision | Why |
|---|---|---|
| **Track A**: offline Android with on-device LLM (Qwen2-0.5B, 2GB phones) | Keep the offline Android spine (deterministic tutor + FTS retrieval); **drop on-device LLM permanently**; move intelligence cloud-side | All four evaluated candidates failed quality gates (`MODEL_EVALUATION.md`: SmolLM2 40%, Gemma 3 270M accepted wrong arithmetic, both Qwens failed contract/runtime). Frontier APIs deliver real tutoring quality |
| **Track B**: USSD/SMS for feature phones, live Africa's Talking | Dropped from core product; simulator remains a demo artifact | Feature-phone tutoring cannot carry LLM-quality explanation; effort concentrates on the home app |
| **Track C**: Raspberry Pi school hubs | Dropped; replaced by optional Athena Learn partnership as a distribution channel | Hub hardware is capital-intensive; Athena already runs offline JSS classrooms |
| Personas: students, parents, teachers, schools | **Learner-first**, caregiver optional (v1), teachers = future channel only | Positioning pivot: consumer home product, not institutional |
| Cloud model: Gemini Flash | **Claude primary + open-weight models (Gemma-class) behind a provider abstraction** | Empirical quality + cost control for grant-funded scale |
| Parent SMS weekly reports | In-app optional caregiver digest via consent-code link | No telephony dependency; matches home-first usage |
| School hub dashboards / teacher copilot | Deferred indefinitely (future channel feature at most) | Zeraki owns the institutional lane; competing there wastes our differentiation |
| Production stack: PostgreSQL + Redis + Celery | Continue minimal Django + SQLite until scale demands otherwise | YAGNI; demo backend patterns already hardened |
| Week-8 pilot of 10 schools | Phased consumer rollout (see §9) | Different go-to-market |
| English-only Grade 3 Maths scope | Kept as starting content; grows by demand toward CBC-primary/JSS breadth | Provenance discipline requires slow, reviewed growth |

**What survives unchanged:** the deterministic rule tutor, Room/FTS4 offline
retrieval, shared JSON content pack + CI drift gate, provenance labelling,
consent-gated sync pattern, claim discipline ("not KICD/KEC curriculum content"
labels), and every ethical guardrail (no IQ/EQ scoring, no diagnosis, no ranking).

---

## 3. Evidence Base (Why This Shape)

### 3.1 Zeraki (Litemore Ltd) — the incumbent to respect, not copy
- Nairobi, founded 2014/15; ~$2.5M revenue; $1.94M raised ($1.8M seed led by
  Acumen, Dec 2022); claims >50% of Kenyan high schools and 2.5M+ students via
  its Analytics product across 10 countries.
- Wedge = school administration (exams, fees, report cards), then cross-sell
  video subscriptions to parents (KES 150/day · 800/wk · 1,900/mo) plus Safaricom
  micro-bundles (KES 20/day).
- **Lesson:** institutional distribution wins in African K-12; direct-to-consumer
  models failed broadly 2023–25. We accept this warning and de-risk deliberately
  (§10 Risk R1): freemium retention-first design, grant-subsidized AI costs, and
  keeping school channels (Athena/Zeraki-style) available as *optional* CAC later.

### 3.2 Duolingo — habit mechanics worth copying, failure modes worth avoiding
- Streaks work: 7-day streak → 3.6× course completion; milestone animations
  lifted D7 retention +1.7%; streak "slack" (freezes) *increased* daily actives.
- Documented failure modes we must not import:
  - XP farming / leaderboard gaming → engagement ≠ learning.
  - Hearts punish mistakes (their most controversial feature).
  - A learner quit entirely after losing an 850-day streak to a few days without
    internet — **in Kenya that is a normal week**, so connectivity grace is mandatory.
  - Duolingo for Schools monitoring is engagement-thin (time-on-task, accuracy);
    nobody offers *prediction* or *interest personalization*. That is our whitespace.

### 3.3 Our differentiators (neither competitor has them)
1. **Interest-based re-explanation** (Layer 2) — zero cold start; works day one.
2. **Predictive struggle flags** (Layer 1, knowledge tracing) — before failure,
   not after grades.
3. **Longitudinal competency growth view** (Layer 3, much later) — feeds the
   Grade 9 pathway-choice moment under CBC.

---

## 4. Product Definition

### 4.1 Target user
Self-directed learners studying at home (starting from the existing Grade 3 maths
pack and growing toward upper-primary/JSS breadth). Caregivers join optionally
via consent code. Schools/teachers are a future distribution channel, not users.

### 4.2 Core loops
1. **Study loop:** pick topic → interactive chat tutor (deterministic, grounded,
   cited) → quick-check quiz → immediate kind feedback.
2. **Habit loop:** daily goal → streak (with automatic freeze on missed days due
   to connectivity/life) → mastery-based XP → milestone celebrations.
3. **Re-explanation loop:** stuck? → "Explain it my way" → cloud re-explains the
   retrieved lesson via declared interests → back to practice within seconds.
4. **Caregiver loop (optional):** link once → weekly digest of topics covered,
   wins, and next focus.

### 4.3 Explicit non-goals (v1)
Teacher dashboards · classroom rosters · school admin features · exam-paper
libraries · live telephony/USSD · on-device LLMs · leaderboards for children ·
any trait/diagnosis inference · curriculum-completeness claims.

---

## 5. Decision Log (authoritative record of this plan)

| # | Decision | Rationale / trade-off accepted |
|---|---|---|
| D1 | Cloud-side intelligence (frontier LLM APIs) | On-device models empirically failed gates; keys stay server-side |
| D2 | Connected-first UX with offline tolerance | Smarter answers justify connectivity requirement; graceful degradation retained |
| D3 | Build the main product by evolving the submitted MVP | Prototype served its purpose; reuse tested assets |
| D4 | Pseudonymous UUID accounts; consent-gated sync; no names/phones stored | Reuses working code; avoids child-PII surface; device-loss trade-off accepted |
| D5 | Layer order: **L2 → L1 → L3** | L2 has zero cold start; L1 needs accumulated data |
| D6 | Providers: Claude primary + open-weight fallback behind abstraction | Quality now, cost control later |
| D7 | Interests captured as **theme chips at onboarding** *(amended 23 Aug 2026)* | Resolves the contradiction with `docs/LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md` §5.2 ("free text off initially"): controlled chips need no moderation tooling or typed-child-text review. Moderated free text may return later behind safety tooling; every interest still maps to the bounded analogy-domain list server-side |
| D8 | Funding path: grants / government sponsorship | Favors open models, auditable data practices, public benefit |
| D9 | Athena Learn (Spidey Labs) = partner/channel, not dependency | Complementary offline JSS distribution; governance agreement required first |
| D10 | Streaks with automatic connectivity grace/freezes | Duolingo's 850-day-loss failure mode is a Kenyan Tuesday |
| D11 | XP awarded only for mastery events (lesson passed, review stabilized) — never raw activity | Prevents gamification misuse |
| D12 | No hearts/lives punishing errors; mistakes trigger hint + offer re-explanation | Errors are pedagogical fuel, feeding L2 |
| D13 | Personal milestones & collections instead of child leaderboards | Maladaptive competition research |
| D14 | Any future adult dashboard leads with predicted-risk per topic, not vanity time metrics | Engagement ≠ learning guardrail |
| D15 | Monitoring surfaces remain pseudonymous until guardian-consented linkage | Consistent with D4/D18 |
| D16 | **Home-first consumer positioning** (phones/laptops/computers) | Fun-first, self-directed study |
| D17 | Teacher/classroom integration demoted to future channel | Focus; avoid Zeraki's lane |
| D18 | Optional caregiver view: consent-code link, weekly digest | Matches real family supervision; foundation for future guardian consent |
| D19 | Build approach: **"Habit Shell Over Proven Core"** — evolve the Compose app; web/PWA companion follows | Weeks-to-ship vs months; protects tested offline spine |

---

## 6. Intelligence Layers Roadmap

### Layer 2 — Interest-Based Re-Explanation (**build first**, ships v1)
- Trigger: learner taps "Explain it my way" on any lesson response.
- Pipeline: retrieve canonical lesson from shared JSON pack → build guarded prompt
  (source ID, page range, verified answer embedded) → map the chosen interest chip
  to its analogy domain (amended D7: chips at launch) → Claude call → strict
  validation (≤55 words, `preserves_verified_answer`, source-ID echo, no new
  facts) → render; any failure falls back to the deterministic explanation.
  Never a retry loop.
- Contract inherited from `docs/GPT55_STORY_REWRITER_INTEGRATION.md`;
  enforcement reuses the shipped Android `ModelExplanationPolicy`.
- Cost control: responses cached per `(lesson_id, analogy_domain)`; free text is
  classified to a bounded domain list server-side before generation.

### Layer 1 — Knowledge Tracing / Struggle Prediction (**build second**)
- Approach: Bayesian Knowledge Tracing computed **on-device** over the Room
  `interactions` history (BKT updates are trivially light — no LSTM on phones).
- Output: per-topic mastery probability + "likely to struggle next" flag surfaced
  gently ("Warm-up suggested") and in the caregiver digest.
- Prerequisite: content-pack growth beyond today's 4 lessons so a topic graph
  exists; spaced-review schedule already implemented (SM-2 intervals).
- Governance: inherits the ban on calibrated-ability claims until pretested;
  UI language says "not enough evidence yet" rather than fake precision.
- Mastery ownership boundary (decided 23 Aug 2026): BKT owns the
  mastery-probability / struggle-flag number; the shipped `IrtEngine`/SM-2 keeps
  running review scheduling. XP, streaks, and the caregiver digest consume BKT
  transitions only — one number per concern, never two competing "mastery"
  figures downstream.

### Layer 3 — Competency & Pathway Decision Support (**build last, gated**)
- Behavioral-skill signals (consistency, follow-through, error-correction speed)
  framed as **CBC-style competencies**, never Big Five personality scores.
- Surfaces only at the senior-school pathway choice stage, only for learners who
  expressed interest, decision-support framing only.
- Hard gates before build: written ethics review, longitudinal-data threshold,
  guardian-consent infrastructure, and explicit sign-off against the prohibitions
  in `docs/LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md`.

---

## 7. System Architecture (Approach A)

```
Android app (Compose, evolves existing codebase)
├── Habit Layer (NEW)
│   ├── HabitEngine — daily goal, streak + auto-freeze, milestone animations
│   └── MasteryXP — XP emitted only on mastery transitions
├── Tutor Spine (KEPT, tested)
│   ├── ChatScreen → TutorResponseEngine → DeterministicTutor + Grade3MathRules
│   ├── OfflineLessonRepository (Room FTS4) ← shared JSON pack (CI drift gate)
│   └── QuizScreen → IrtEngine (SM-2 review scheduling)
├── NEW: ReexplainService
│   └── POST /api/v1/tutor/reexplain → validated ≤55-word response
│       → ModelExplanationPolicy check → deterministic fallback on failure
├── NEW: Onboarding step 3 — interest theme chips (stored locally, name-stripped;
│   moderated free text deferred per amended D7)
└── NEW: CaregiverLink — consent-code pairing, weekly digest opt-in

Django backend (educloud app, evolves)
├── Existing endpoints unchanged (health/ready/sync/sandbox)
├── POST /api/v1/tutor/reexplain
│   ├── bounds-checked input (existing _value pattern)
│   ├── provider protocol: Claude primary → open-weight fallback
│   │   (same interface shape as agent_swarm.TeachingLoopClient)
│   ├── strict response validation (contract above)
│   ├── per-(lesson,domain) response cache
│   └── per-UUID rate limiting
├── POST /api/v1/caregiver/link — consent-code pairing (pseudonymous)
├── Weekly digest job — reads synced pseudonymous signals, renders summary
└── Data persisted: UUID, interests-as-domains, mastery events. NO names,
    NO phone numbers, NO raw chat transcripts.

Web/PWA companion (Phase 4) — progress, streak, practice on laptop browsers.
```

**Data flow — re-explanation:** tap → `{lesson_id, question, consent_flag}` +
locally-stored interest domains → backend validates + caches/generates →
policy-validated response rendered with source citation; offline or error ⇒
deterministic explanation, never a dead end.

---

## 8. Gamification Design Rules (from D10–D15)

| Rule | Implementation |
|---|---|
| Connectivity grace | Missed days during no-connectivity auto-apply freezes; streak never breaks on network loss alone |
| Mastery-only XP | `lesson_passed`, `review_stabilized`, `first_try_correct_after_struggle` — nothing else pays |
| Errors welcome | Wrong answer → kind correction → hint chip → one-tap "Explain it my way" |
| No leaderboards | Personal bests, collection albums, milestone ceremonies (Duolingo-grade animation polish) |
| Honest stats | Progress screen shows *real* Room-derived data; the old hardcoded mock stats are removed |
| Caregiver honesty | Digest shows topics covered + mastery movement + "not enough evidence" where true |

---

## 9. Implementation Plan

| Phase | Scope | Exit criteria |
|---|---|---|
| **0 — Hygiene (wk 1)** | Remove/quarantine unreviewed bundled OCR lessons from public builds; commit or clean stray files; scaffold provider-abstraction module; decide content-pack growth source | Public-safe APK contents; CI green |
| **1 — Habit shell (wk 2–4)** | HabitEngine, auto-freeze streaks, MasteryXP, milestone animations; interests step in onboarding; delete fake Progress/Profile mocks | Unit tests for streak/XP logic; fun loop usable offline end-to-end |
| **2 — L2 re-explanation (wk 3–6)** | Backend `/tutor/reexplain` + Claude integration + validation + cache; client button + policy enforcement; adversarial prompt eval set | Contract tests pass; 25-case adversarial eval ≥ agreed bar; graceful offline fallback proven |
| **3 — Caregiver digest (wk 6–8)** | Consent-code linking; weekly digest job; digest rendering in-app/email | Link/unlink tested; digest contains zero PII beyond alias chosen by family |
| **4 — Web companion (wk 8–12)** | PWA: login-by-code, progress, streak, practice mode reading same backend | Parity smoke suite; laptop demo possible |
| **5 — L1 BKT (post-launch)** | On-device mastery model over interactions; gentle struggle flags; digest upgrade | Prediction-vs-outcome tracking instrumented from day 1 of this phase |
| **6 — L3 (gated)** | Per §6 gates | Not scheduled until gates pass |

Content growth runs continuously beside phases 1–4: expand the reviewed rule-card
pack (ordinals → number words → addition/subtraction → equal groups → fractions →
time/measurement, per `RULE_BASED_TUTOR_MVP.md` order), each card carrying full
provenance. **No public release ships content lacking a completed provenance row.**

---

## 10. Risks & Mitigations

| # | Risk | Mitigation |
|---|---|---|
| R1 | Consumer edtech failure wave (2023–25 precedent) | Freemium core loop free forever; grant-subsidized AI costs (D8); retention-over-acquisition design; school channels kept optional for later CAC |
| R2 | LLM API cost escalation | Response caching by (lesson, domain); ≤55-word outputs; open-weight fallback; monthly spend cap before enabling live calls |
| R3 | Content licensing (only 4 provenance-complete lessons today) | Growth only via completed provenance rows; quarantined OCR pack stays out of builds; original AI-generated cards labelled as such |
| R4 | L2 output quality/hallucination | Strict contract + adversarial evals + deterministic fallback; verified answers never alterable by the model |
| R5 | Cold-start emptiness (few topics at launch) | L2-first ordering masks it; habit loop carries value while pack grows |
| R6 | Child-privacy misstep | Pseudonymous UUIDs; name-stripped prompts; consent flags; caregiver linkage explicitly consented; Kenya DPA 2019 review before public launch |
| R7 | Athena/partner governance ambiguity | Written agreement required before any data or revenue sharing |

---

## 11. Verification Strategy

- Extend existing JVM suites: HabitEngine/streak-freeze/XP-rule tests;
  ReexplainService contract tests with mocked providers; caregiver-link tests.
- Keep the retrieval regression suite and content-pack CI drift gate green.
- New adversarial evaluation harness for L2 outputs (25 cases minimum, mirroring
  the `MODEL_EVALUATION.md` discipline) gating any live-provider enablement.
- Backend: `manage.py test educloud.tests`; Android:
  `./gradlew.bat testDebugUnitTest` — both already wired into `.github/workflows/verify-demo.yml`.

---

## 12. Open Questions

1. Grant pipeline status and whether funder requires specific outcome metrics
   (L1 instrumentation should align with whatever is chosen).
2. Source of content growth: additional owner-permitted book pages vs original
   AI-generated reviewed cards (provenance rows differ accordingly).
3. Final analogy-domain list for interest classification (bounded set needed for
   caching; proposal: sports, animals, music, transport, food, games, nature, money).
4. PWA stack choice for Phase 4 (Next.js assumed unless overridden).
5. Commercial/data-governance terms with Athena Learn (blocks D9 activation).

---

## 13. References

**Internal:** `PRD.md` v1.2 · `MVP_LAUNCH_CHECKLIST.md` · `MODEL_EVALUATION.md` ·
`RESEARCH_FOUNDATION.md` · `PHD_RESEARCH_PAPERS.md` ·
`docs/GPT55_STORY_REWRITER_INTEGRATION.md` ·
`docs/LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md` ·
`docs/TIER2_RELEASE_GATES.md` · `RULE_BASED_TUTOR_MVP.md` ·
`DIFFERENTIATION_BRAINSTORM.md` (pre-cleanup; treat competitor claims as unverified).

**External:** TechCrunch — Zeraki $1.8M seed (Dec 2022); Safaricom–Zeraki Learning
partnership release (Apr 2022); allbusiness.africa — *African K-12 EdTech 2026:
The Institutional Pivot*; Preqin/Tracxn Zeraki profiles; Duolingo Blog — *The
habit-building research behind your streak* (2022); WIRED — *How Duolingo uses
dirty gaming tricks…* (2017); TechCrunch — *The product-led growth behind edtech's
most downloaded app* (2021); Mogavi et al., *When Gamification Spoils Your
Learning* (arXiv:2203.16175); Piech et al., *Deep Knowledge Tracing*; Google
Research — *Learn Your Way*.
