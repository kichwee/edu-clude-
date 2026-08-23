# Adaptive-Learning Research Base — Knowledge Tracing, Interest Re-Explanation, Pathway Signals

> **Compiled:** 23 August 2026, from cofounder research for the three-layer
> adaptive-learning plan (V2 `HOME_STUDY_COMPANION_V2_PRODUCT_PLAN.md` §3.3/§6).
>
> **⚠️ READ THE BANNER BEFORE CITING ANYTHING HERE**
>
> The Layer 3 sources below argue **for** personality-trait and
> career-prediction modelling. On 23 August 2026 we decided **not to build
> that**: Layer 3 stays inside the guardrails (CBC-style behavioural
> competencies only, never Big Five or employability scores), gated behind the
> hard preconditions in V2 plan §6 and the prohibitions in
> `docs/LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md` (no IQ/EQ scoring,
> no diagnosis, no ranking, no high-stakes automated decisions).
>
> These papers therefore inform *design thinking* only. They are not a licence
> to relax product language, data collection, or consent flows. Any future
> attempt to revisit that decision requires a written ethics review first.

---

## Decision record this document supports (2026-08-23)

| # | Decision | Consequence for these papers |
|---|---|---|
| 1 | Layer 3 stays in guardrails (competencies, never traits) | §3 citations are background only |
| 2 | Interests = theme chips at launch, moderated free text later (amends V2 D7) | §2 interest-capture designs read as future option, not current plan |
| 3 | Mastery ownership: BKT predicts, SM-2 schedules | §1 model choice is settled: interpretable BKT, on-device |
| 4 | Layer 1 ships only after content-pack growth (≥8 reviewed skills) | §1 accuracy claims cannot be validated on today's single-skill pack |

---

## §1 — Knowledge Tracing (Layer 1)

| Source | What it supports | Build impact |
|---|---|---|
| Piech et al., *Deep Knowledge Tracing* (Stanford) | LSTM-based tracing beats BKT with enough data | Considered and rejected for v1: no dataset, no on-device runtime. Revisit only if synced anonymous attempts ever reach the scale where DKT outperforms tuned BKT |
| *Deep Learning vs. Bayesian Knowledge Tracing* (JEDM — Journal of Educational Data Mining) | BKT remains competitive for driving interventions; interpretability matters when triggering support | Directly justifies decision #3: an interpretable mastery probability a parent can be shown |
| Fairness-in-BKT study (reading ability as a confound) | Tracing parameters can absorb reading skill rather than target skill | Feeds the fairness rule: assessment items stay low-language-load and neutrally themed; themed contexts belong to re-explanation, never to measurement |

## §2 — Interest-Based Re-Explanation (Layer 2)

| Source | What it supports |
|---|---|
| Google Research, *Learn Your Way* (LearnLM/Gemini) — two-step generation: re-level text to grade, then re-frame through learner interests | Validates our two-stage contract shape: canonical verified lesson stays fixed; only the *framing* is personalised. Mirrors our rule: verified answers are never alterable by the model |

Note: interest personalisation alone is **not** a defensible moat (see Learner
Discovery PRD §2 — Khanmigo et al. already ship it). Our differentiation is the
guarded pipeline around it: bounded domains, ≤55-word validation, source-ID
echo, deterministic fallback.

## §3 — Trait & Career Signals (Layer 3 — GATED, NOT SCHEDULED)

> ⚠️ Everything below motivates an approach we have deliberately declined to
> build. Kept for grant-writing context and for the future ethics review that
> any reversal would require.

| Source | Claim | Why we still don't build it now |
|---|---|---|
| [Frontiers in Psychology (2026)](https://www.frontiersin.org/journals/psychology/articles/10.3389/fpsyg.2026.1743896/full) — integrating psychological factors into a career-guidance model | Combined psychometric + academic signals improve guidance fit | Psychometric instrumentation on Grade 3 children is exactly what the Learner PRD prohibits without ethics review |
| [*Predictive modeling of student career pathways using machine learning techniques*](https://www.academia.edu/145493850/Predictive_modeling_of_student_career_pathways_using_machine_learning_techniques) (JSSCI; [link truncated in source notes](https://jssci.org/) — verify before citing externally) | Random Forest over academics + skills + interests reached ~88% accuracy on 1,000 profiles; combined signals beat score-only | Retrospective classifier ≠ longitudinal child-data infrastructure; also a reminder that "88%" claims need our own eval discipline before believing |
| [AI-Driven Career Guidance for Graduates (IJSRED)](https://ijsred.com/volume8/issue4/IJSRED-V8I4P124.pdf) | Big Five + supervised ML competency assessment; proposes mobile-first offline-capable variant | Population is graduates, not 8-year-olds; the offline-first engineering pattern is the transferable part |
| [Cognitive Ability and Non-Ability Trait Predictors of Academic Achievement: A Four-Year Longitudinal Study (MDPI J. Intelligence)](https://www.mdpi.com/2079-3200/13/7/79) | Typical Intellectual Engagement, Need for Achievement, Openness, Desire to Learn predict choices/outcomes over 4 years | Useful *if ever* gated-in as the shortlist of observable behaviours worth logging — as CBC competencies, not traits |
| [Personality and Career Success: Concurrent and Longitudinal Research (PMC)](https://pmc.ncbi.nlm.nih.gov/articles/PMC2747784/) | Personality–career links change across adulthood; measure early and repeatedly | Directly conflicts with "a few app questions cannot responsibly produce a stable profile"; supports repeated observation, which our interactions log does anyway |

### Framing rule inherited from the research pass

If Layer 3 is ever built, output is a **decision-support report at the
senior-school pathway-choice stage**, showing performance-trend + behaviour-
trend evidence against field profiles — never an automated recommendation the
platform hands down, and only for learners who opted in.

---

## Origin note

This file preserves the cofounder research pass of 23 August 2026 (triggered by
Meta's TRIBE v2 brain-response predictor — inspirational, not applicable, since
it requires fMRI input). TRIBE's useful residue is the framing: simulate the
"phantom brain" from behavioural traces instead of scans — which is ordinary
Knowledge Tracing done honestly.
