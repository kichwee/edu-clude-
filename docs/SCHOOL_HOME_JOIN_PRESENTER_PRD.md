# School–home join — Presenter PRD

> Version 0.1 | 25 August 2026  
> Status: **Draft for mixed-room presentation** (technical partner, grant/investor, school / TSC / county officer).  
> Companion critique: [ATHENA_EDUCLOUD_DEVILS_ADVOCATE.md](ATHENA_EDUCLOUD_DEVILS_ADVOCATE.md).  
> Demo script: [SCHOOL_HOME_JOIN_DEMO_SCRIPT.md](SCHOOL_HOME_JOIN_DEMO_SCRIPT.md).  
> This document does **not** replace [HOME_STUDY_COMPANION_V2_PRODUCT_PLAN.md](HOME_STUDY_COMPANION_V2_PRODUCT_PLAN.md) or [MVP_LAUNCH_CHECKLIST.md](../MVP_LAUNCH_CHECKLIST.md). It defines the join story and the week-one prototype only.

---

## 1. One line

Athena Learn is the USB classroom practice system for Junior Secondary. EduCloud is the phone/home revision companion. They meet through a homework pack, not through cameras.

---

## 2. Problem (what the mixed room already believes)

Rural peak pupil–teacher ratios make individual attention mathematically scarce (Athena cites MoE Basic Education Statistical Booklet 2022/23 sub-county tables; rural peaks around 1:60). Roughly 30% of Kenyan schools have reliable internet (Communications Authority of Kenya, Q4 2023/24). After class, revision happens on shared phones and, where they exist, school-lab PCs — not on a second copy of the classroom roster in the cloud.

The missing loop is **the same skill practised tonight**, not a camera that guesses who looks bored.

---

## 3. Product split (do not blur)

| Surface | Owner | Who | Device | What is true today | What is not true today |
|---|---|---|---|---|---|
| **School** | Athena Learn (Spidey Labs) | Teacher + learners in class | Classroom Windows PC / “smart board” PC, USB install | Practice banks; local teacher **analytics**; Kuriot JSS onboarding partner | Teacher **assignment**; lesson-plan generator; facial analysis; live join to EduCloud |
| **Home / revision** | EduCloud (this repo) | Learner (caregiver optional later) | Android phone; later PWA / lab PC | Grade 3 Maths tutor, quiz, habit shell, “Explain it my way” | Grade 6–9 banks; classroom roster; KICD-approved content |
| **Bridge** | Contract (prototype here) | Token, not a child | JSON pack + class code | Week-one labelled prototype | Shared database, names, photos, video |

EduCloud decisions D9 (Athena as partner, not dependency) and D17 (no teacher copilot inside EduCloud) stand, with **one thin exception**: this prototype emits and consumes a homework pack. EduCloud does not become a school information system.

---

## 4. Architecture

```
Athena classroom PC (local SQLite, named roster stays here)
        |
        |  export HomeworkPack  (skill_id, item_ids, class_code — no names, no faces)
        v
   class code  --------->  EduCloud Android (pseudonymous UUID)
        ^
        |  optional attempt events (item_id, correct, learner_token UUID)
        |
Teacher Sidekick prototype (Django, this repo, labelled "not Athena production")
```

**Pack fields (v1):** `schema_version`, `assignment_id`, `skill_id`, `item_ids`, `issued_at`, `class_code`.

**Forbidden in the pack and in the pitch:** names, emails, photos, national IDs, GPS, emotion scores, face embeddings, “brain” or trait labels.

**Smart board:** the classroom Windows PC that already drives the board. No Promethean/SMART SDK in this PRD.

---

## 5. Non-goals (v1 and this room)

- Computer vision, classroom cameras, “engagement heatmaps,” sleeping/bored detectors.
- TRIBE, fMRI, phantom brain, IQ/EQ, diagnosis, ranking, streaming, punishment.
- A live binary integration of Athena’s monorepo and EduCloud.
- Grade 7 content inside EduCloud, or Grade 3 presented as JSS.
- KICD/KEC “approved curriculum content” claims.
- Offline frontier AI on government DLP tablets.
- Zeraki-style fees, exams, report cards, or a second SIS.
- Full KICD-format lesson-plan generation as the lead feature.
- AI verdicts on lesson quality, teacher “engagingness,” or any per-teacher score, export, or coaching output.
- Assign-as-test with AI marking of open responses; anything auto-marked entering a CBC continuous-assessment record.
- Per-pupil re-theming of **scored** items (themed explanations and worked examples only).
- Child chat transcripts or “subchats” pushed, looped, or made visible to the school side.
- Adult-facing learning-habit or trait claims about a named pupil.
- A live in-lesson teacher chatbot.
- Night-lab sessions that use, link, or create a home pseudonymous account on school hardware.
- Vendor smart-board SDKs or “runs on the panel” hardware claims.

---

## 6. Do-not-say list (slide 0 — keep visible)

Print this. If a word below is spoken, stop and correct.

**Two reviews exist.** The second pass — [SCHOOL_HOME_JOIN_GRILL.md](SCHOOL_HOME_JOIN_GRILL.md), 26 Aug 2026, 6 Critical / 4 Major — is harder and is **authoritative wherever it differs from the first pass**. §5 and §6 reflect it.

**Three specifications exist; only one counts.** Athena’s confidential PDF is the specification. The public site overstates it and the verbal pitch exceeds both. Anything not in the PDF is said as “idea, not roadmap” — or not said.

- Facial recognition, computer vision, cameras, “we can see who is sleeping.”
- Brain, TRIBE, phantom brain, neural, “the AI reads their mind.”
- “KICD-approved” / “official curriculum content.”
- “Already used in N schools” beyond **Kuriot JSS as an onboarding partner**.
- “Works fully offline including the AI tutor on every government tablet.”
- “We diagnose who is lazy / not engaging.”
- “One app, all grades, live today.”
- “Athena already assigns homework to phones.”
- “EduCloud is the smart-board OS.”
- “The AI tells the teacher how the lesson went” / “we score teaching quality” / “we coach the teacher.” **Highest-severity item in the room: TSC/TPAD appraisal capture and KNUT/KUPPET exposure. Do not wander near it.**
- “Assign it as a test and the AI marks it against the teacher’s rules.” Auto-marked work can reach Grade 9 placement through CBC continuous assessment, and the liability lands on the teacher.
- “We tweak the test to each student’s interest.” Two themed items are two different measurements. Themed **explanations** are fine; themed **scored items** are not.
- “Subchats / the child’s tutor chat get looped to the school side.” No moderation, no safeguarding escalation, no named responsible adult exists.
- “Integrates with smart boards” unqualified. Many panels run embedded Android and cannot host the Tauri/Windows/SQLite build without an OPS module. Say: *the classroom Windows laptop that drives the display.*

**“Deterministic” — one word, two meanings. Check which one you are about to say.**

| Applied to… | Verdict |
|---|---|
| a **child** — “we deterministically know where this student’s problem is” | **BANNED.** It is a wide-interval BKT estimate, and Athena’s `practice_results` cannot compute it today (no `question_id`, `strand`, `difficulty`). Say “estimated, from N recent attempts,” with “not enough evidence” as a real answer. |
| a **system fallback** — the rule-based content path with no LLM in it | **PERMITTED.** “Deterministic fallback” stays legal; see below. |

**What you may say**

- USB install onto existing classroom laptops; practice works without internet.
- Athena’s in-repo inventory (disclose that the public site currently lists four JSS areas while the PRD counts six — see the devil’s advocate table).
- Teacher dashboard today = **analytics**, not direction.
- EduCloud can re-explain a **verified Grade 3** lesson through a declared interest chip, with deterministic fallback (rule-based, no LLM in the path — the permitted sense above).
- The join is a **contract we are prototyping**. Athena teacher-direction is **unbuilt** (their PRD §6).
- Predicted struggle, when it exists later, comes from **practice attempts**, not faces.
- **If library / night-lab access is raised, state the condition in the same breath:** lab sessions run the school’s local install under **school identity only** — no home pseudonymous account used, linked, or created there — and a **written after-hours policy from the school** (supervising adult, parental permission, incident log, lighting and transport, closing time) is a precondition before the mode is enabled. Without it, the mode stays off.

---

## 7. Eight-to-ten minute deck

| Min | Beat | Proof in the room |
|---|---|---|
| 0:00 | Slide 0: do-not-say list | Paper or first slide |
| 0:45 | Problem: 1:60, ~30% reliable internet, revision happens at home | Athena prospectus stats with sources |
| 2:00 | Two surfaces: school USB vs home phone | Architecture diagram above |
| 3:30 | **Optional Beat 0:** Athena desktop practice + read-only dashboard | Only if Peter brings the USB/app — do not fake it in EduCloud |
| 5:00 | Live home loop: Tonight from class → Grade 3 regrouping quiz → Explain it my way | This repo |
| 7:00 | Teacher Sidekick prototype: assign `G3-HOME`, aggregate **item** misses | Browser at `/demo/teacher/sidekick` |
| 8:30 | What is not built (direction on Athena, cameras, live join, KICD) | Honest list |
| 9:30 | Ask: cofounder alignment **or** grant next step **or** remaining pilot slot — one ask only | |

---

## 8. Week-one demo — acceptance

A presenter can complete this loop without lying.

1. Banner on the teacher page is visible: *Contract prototype. Not Athena production. Not facial analysis.*
2. Assign tonight’s revision for skill `two_digit_subtraction_regrouping`; class code `G3-HOME` is shown.
3. Android **Tonight from class** accepts that code **or** the bundled fixture and opens the existing Grade 3 maths quiz (and the tutor with a regrouping prompt).
4. Teacher page can show aggregate “n learners attempted, n missed item X” from **item results** (live POST or the page’s demo-attempt button). No names, no faces.
5. Backend and Android unit tests for pack validation (unknown skill, oversized body, forbidden PII keys) pass.

**Out of week one:** Athena monorepo edits, smart-board SDKs, OpenCV, live BKT, lesson-plan LLM, Kiswahili banks, Grade 7 in EduCloud, real child data.

---

## 9. Security rules for the “secure system”

- School roster never syncs to EduCloud.
- Home never stores the child’s school name.
- Learner tokens on the bridge are UUIDs, not aliases.
- Purpose limitation: homework completion on a named skill, nothing else.
- Cloud sync and re-explain remain consent-gated and disabled-by-default unless a demo opts in.
- Any future camera, microphone-for-the-class, or trait model requires a separate DPIA, Kenyan counsel, and an ethics review — they are **not** on the roadmap in this PRD.

---

## 10. Open questions this document does not pretend to close

1. Written commercial/data-governance terms with Athena Learn (EduCloud D9 / risk R7).
2. Whether Athena Phase B (teacher direction) lands before any real pack is emitted from *their* SQLite.
3. KICD confirmation (`info@kicd.ac.ke` in Athena’s risk register).
4. Aligning athenalearn.org with PRD Part I before a school-officer audience.
