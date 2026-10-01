# Devil’s Advocate Report: Athena Learn + EduCloud school–home join

> Skill: `devils-advocate` (multi-turn debate).  
> Date: 25 August 2026.  
> Scope: `_project` (not a `paper-*` directory; not stamped into a paper `reviews/INDEX.md`).  
> Inputs: Athena Learn PRD (Peter Kibet, Spidey Labs Ltd, 25 Aug 2026); [athenalearn.org](https://athenalearn.org) and prospectus (fetched 25 Aug 2026); verbal classroom / smart-board / computer-vision pitch; EduCloud repo (`PRD.md`, `docs/HOME_STUDY_COMPANION_V2_PRODUCT_PLAN.md`, `docs/LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md`, `MVP_LAUNCH_CHECKLIST.md`).  
> Audience for the surviving advice: mixed room (technical partner, grant/investor, school/TSC/county officer).

**Verdict:** `ISSUES FOUND` — 4 Critical, 5 Major, 2 Minor survive adjudication. 8 critiques raised in Round 1. Open issues: **6 / 8** if “surviving” means Critical + Major + Minor (11 listed below because Major/Minor split some Round-1 items); against the 8 numbered Round-1 critiques, **6 stand or are only partial**.

This report is designed to challenge, not to affirm.

---

## Competing hypotheses

**Research / product question:** Why would a joined school + home system help Kenyan CBC learners?

Each hypothesis is a mechanism, not a slogan.

| ID | Hypothesis | Mechanism | Testability | Falsifiability | Parsimony |
|---|---|---|---|---|---|
| **H1 Complementary spaced practice** | In-class USB practice plus tonight’s assigned revision raises later strand accuracy because the missing loop is after-class retrieval. | Spacing + retrieval practice on the same skill_id. | Strong (assignment completion × later item checks) | Strong (no gain on held-out items ⇒ reject) | Strong |
| **H2 Face-as-engagement** | Classroom cameras infer bored / sleeping / engaged, and those labels identify who needs help. | Affective state → attention → learning. | Weak in this fleet (lighting, angle, occlusion) | Weak (always an escape: “bad camera”) | Weak (adds biometrics without a learning construct) |
| **H3 Teacher-time bottleneck** | Value is saving planning minutes (lesson plans, worksheets), not learner modeling. | Teacher labour substitution. | Strong | Strong | Moderate (real need; crowded market) |
| **H4 Null / incumbent** | Observed “need” is distribution. Zeraki already owns the school relationship; USB tutors die without it. | Institutional CAC, not pedagogy. | Moderate | Moderate | Strong |
| **H5 Consent collapse** | Any named child graph that joins classroom identity + home phone + video is rejected by parents / ODPC, so the product only survives as a tokenised, local-school / pseudonymous-home split. | Legal and social constraint, not a learning mechanism. | Strong (will the room accept cameras?) | Strong | Strong |

**Distinguishing predictions**

- If **H1** and not **H2**: teacher-assigned practice plus home re-explain moves later item accuracy; face labels add nothing once the practice log is known.
- If **H2** and not **H1**: face labels predict later items *better than* the practice log. Do not bet a pitch on this.
- If **H3** and not **H1**: teachers adopt lesson-plan generation and ignore assignment completion. Somo AI / CBC Edu Kenya already sell that wedge.
- If **H4**: schools install only when a Zeraki-like SIS relationship exists; a clever tutor does not move procurement.
- If **H5**: cameras or a named cross-context ID kill the meeting regardless of learning gains.

**Critical experiment (do not run H2 on children):** assignment pack + home quiz, no cameras. Measure later skill checks. That is the only ethical near-term test.

---

## Round 1 — Adversarial critic

Hostile but competent reviewer. Numbered critiques:

1. **Classroom computer vision of children’s faces** to score engaged / bored / sleeping is child biometric / sensitive-data processing. EduCloud’s long-term PRD already forbids camera emotion analysis. Accuracy is poor; harm (stigma, punishment, lighting and race bias) is high. One slide of this can sink the partnership in a mixed room.

2. **“Brain contacts,” TRIBE, or a phantom brain** is scientifically empty here. Meta’s TRIBE v2 needs fMRI. Practice logs are not neural data. Grant reviewers and county officers who hear “brain” will not trust the rest of the honest Athena PRD.

3. **The two apps are not one live system.** Athena is a separate Tauri/USB runtime (Grades 6–9, local named roster). EduCloud is Grade 3 Android + Django. There is no shared student, no assignment API, and no grade overlap. A week-one “joined product” demo that implies otherwise is a false demo.

4. **The school product in the verbal pitch is unbuilt on Athena.** Athena PRD §6: the teacher layer is read-only analytics. Teachers cannot post coverage, set assignments, or steer revision. The website’s “teachers see the whole classroom” is observation, not direction.

5. **Public site vs inventory.** athenalearn.org / prospectus (four JSS areas, Grades 7–9, “no accounts, no internet, no cloud,” English *and* Kiswahili tutor) disagrees with PRD Part I (six areas including Grade 6 and Agriculture; `students` with name and email; production AI via Anthropic proxy; Grade 6 Kiswahili banks at zero). A mixed room will open the site next to the PDF.

6. **KICD / TSC vendor climate.** Athena PRD already flags unconfirmed digital-materials approval and a June 2026 directive to report vendors selling unapproved curriculum-support materials. Cameras make approval harder. EduCloud already forbids “official KICD/KEC curriculum content” claims.

7. **“Smart board integration” is unspecified hardware.** Without a vendor SDK, the only honest version is software on the Windows PC that already drives most Kenyan “smart boards.” Camera-and-light analysis is not an integration; it is a new surveillance product.

8. **A “secure merge” that links a named school child to a home phone** (and especially to video) is how you accidentally build a child surveillance graph. Kenya DPA 2019 + ODPC children’s-data guidance already sit in EduCloud’s long-term PRD as a DPIA gate.

---

## Round 2 — Defense (author)

Strongest honest defense of each critique.

1. **CV.** There is no adequate defense for live facial analysis of minors in a Kenyan classroom in v1. Cut it from the PRD, pitch, and demo. Do not tease “we will add cameras later” in this room.

2. **Brain / TRIBE.** Athena Part II already replaced TRIBE with Bayesian knowledge tracing from *practice attempts* and a Learn-Your-Way-style prompting layer. The defense works **only if the pitch uses that language** and never says brain, fMRI, or phantom brain.

3. **Two products.** Complementary go-to-market is real: USB classroom (Athena: offline practice + KICD-traced banks) + phone home (EduCloud: habit shell + consented “Explain it my way”). That is a **strategy** defense, not a claim that a binary integration exists this week. The week-one artifact is a **homework-pack contract**, labelled as a prototype.

4. **Teacher direction.** Athena sequences this as Phase B, gated on Kuriot teacher feedback. Defense: show observation today if Peter brings the desktop app; show a **labelled EduCloud prototype** for “assign tonight’s revision”; never say Athena already assigns homework.

5. **Site vs PRD.** Put a “what exists / what does not” page in the deck. Ask Spidey Labs to align athenalearn.org with Part I before a school-officer room. Until the site is edited, the critique stands as presentation risk.

6. **KICD.** Athena’s current plan is a **free** founding pilot (Kuriot JSS), which may avoid the “selling materials” trigger. That is partial. It does not license overclaiming approval or shipping cameras.

7. **Smart boards.** Redefine as “Athena already runs on the classroom PC that drives the board.” Drop vendor SDKs and cameras. Then the hardware critique is resolved.

8. **Identity join.** Design the handshake as `assignment_id` + one-time class code. School SQLite never leaves the laptop. Home UUID never learns the child’s real name. Pack contains skill and item ids only. This is a **design** defense; it is not implemented in Athena, and it is only prototyped in EduCloud this week.

---

## Round 3 — Adjudication

| # | Ruling | Note |
|---|---|---|
| 1 CV | **Stands** | Defense failed. Hard non-goal. |
| 2 Brain language | **Stands if used**; **resolved if** the pitch uses BKT / practice-attempt wording only | Presenter PRD must ban the words. |
| 3 Live joined system | **Stands** as a shipped-product claim; **partial** as a strategy + labelled contract demo | |
| 4 Teacher-director-as-shipped | **Stands** if implied; **partial** if labelled “next / prototype” | |
| 5 Site vs PRD | **Stands** until the site is edited | Disclose in the room. |
| 6 KICD | **Partially addressed** | Free pilot helps; approval is still open. |
| 7 Smart-board SDK | **Resolved** if redefined as classroom Windows PC and cameras stay out | |
| 8 Identity graph | **Partially addressed** once the token contract is the demo; **stands** if anyone proposes named sync or video | |

---

## Devil’s Advocate Report (surviving critiques only)

### Critical (must not appear in the presenter PRD or demo)

1. **Classroom computer vision / facial engagement** — The defense failed. Suggested fix: written non-goal; no mock heatmaps of faces; no “cameras later” tease.

2. **Neural / TRIBE / brain-state claims** — Defense works only with Athena’s BKT wording. Suggested fix: “predicted struggle from **practice attempts**,” never brain.

3. **“The two apps are already one system”** — They are not. Suggested fix: demo a **bridge contract**; say Athena teacher-direction is unbuilt; never one child who lives in both live products.

4. **Named-child + home-device + video identity** — Suggested fix: local school DB; home UUID; assignment token; no photos, no national ID, no raw video.

### Major (reviewers will likely raise)

5. **Marketing honesty** — Site and PRD disagree. Residual: presenters will google athenalearn.org. Suggested fix: one-page inventory vs marketing; align the site after.

6. **Teacher is observer, not director** — Residual: the verbal pitch *is* Phase B. Suggested fix: Athena Phase B on their repo; this week only a labelled EduCloud prototype of assign-tonight.

7. **KICD / TSC vendor climate** — Residual: free pilot is not approval. Suggested fix: pilot-free framing; no “approved curriculum content” claim.

8. **Thin content + grade split** — Athena banks are quiz-shallow vs a KLB-style revision book; EduCloud is a Grade 3 rule pack. Suggested fix: one persona per demo beat (JSS school **or** Grade 3 home).

9. **Offline AI** — Practice can be offline; the interesting tutor often cannot (Anthropic proxy; DLP tablets unlikely to run Ollama). Suggested fix: say it like Athena §7.

### Minor (worth acknowledging)

10. **Night computer-lab access** — Residual: it is “run the same client on a lab PC,” not a third product. Preempt with one sentence.

11. **Full lesson-plan generation** — Residual: crowded (Somo AI, downloadable CBC packs). Do not lead with it.

### Dismissed

- Smart boards as a special integration **if** defined as the existing classroom Windows PC and cameras stay out.
- “Home vs school is a confused strategy” **if** EduCloud does not grow a competing SIS and Athena remains the school surface (honours EduCloud D9; thin-bridge exception to D17 only).
- Knowledge tracing as a research program (standard BKT; just not a week-one live model).

---

## Site vs PRD mismatch (Athena Learn)

Use this table in the room if challenged. Site/prospectus fetched 25 August 2026.

| Claim | Public site / prospectus | Athena PRD Part I (25 Aug 2026) |
|---|---|---|
| Grades | Junior Secondary 7–9 | Grades 6–9 (including Grade 6 Science & Technology) |
| Learning areas at launch | Four: Maths, Integrated Science, English, Pre-Technical | Six counted in-repo: those four plus Agriculture & Nutrition and Grade 6 Science & Technology (75 banks / 1,344 questions) |
| Accounts | “No accounts, no internet, no cloud” | `students` table includes name, email, grade |
| AI offline | Implied full tutor offline | Practice works offline; production AI for the real fleet is the Anthropic proxy; Ollama is unlikely on DLP tablets |
| Kiswahili | Tutor “speaks English and Kiswahili” | Grade 6 Kiswahili question banks are **zero** (KPSEA gap). UI language ≠ curriculum coverage |
| Teacher role | “Teachers see the whole classroom” | Read-only analytics; cannot assign or steer revision |
| Pilot | Kuriot JSS, Cherangany; Teacher Isaac Toili; free pilot year; two slots open | Compatible; do not inflate to “N schools live” |

---

## What this means for the join

Hold **H1**. Kill **H2**. Do not lead with **H3**. Treat **H4** as a GTM warning (Athena USB + optional later institutional CAC). Design for **H5** (tokenised pack, no faces).

The only week-one demo that survives this report is a labelled Grade 3 **homework pack** on EduCloud, plus optional Athena desktop as a separate Beat 0 if Peter brings it.
