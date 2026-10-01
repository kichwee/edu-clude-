# School–home join — second-pass grilling ("grill me" mode)

> Skill: `devils-advocate` (multi-turn debate protocol, with the `references/competing-hypotheses.md` step run in full).
> Date: 26 August 2026.
> Scope: `_project`. This repository is **not** a paper repo — there is no `paper-*/` directory and no `reviews/INDEX.md`. Per the skill's "Output Path & Stamping" section, stamping is **skipped** for non-paper use; the report lives here instead.
> Companion, **not** replacement: [ATHENA_EDUCLOUD_DEVILS_ADVOCATE.md](ATHENA_EDUCLOUD_DEVILS_ADVOCATE.md) (first pass, 25 Aug 2026, 8 critiques). This document deliberately attacks what that one did not, and says where it was too soft or wrong.
> Sources: Athena Learn PRD (Peter Kibet, Spidey Labs Ltd, 25 Aug 2026, "the PDF"); [athenalearn.org](https://athenalearn.org) and [/prospectus](https://athenalearn.org/prospectus) (fetched 26 Aug 2026, "the site"); the founder's verbal pitch (undocumented, "the pitch"); this repo's `PRD.md`, `docs/HOME_STUDY_COMPANION_V2_PRODUCT_PLAN.md`, `docs/LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md`, `MVP_LAUNCH_CHECKLIST.md`, and the week-one prototype (`edu-cloud/backend/educloud/homework_pack.py`, `homework_views.py`; `android-app/.../sync/HomeworkPack.kt`, `.../ui/screens/home/TonightFromClassScreen.kt`).

**Verdict:** `ISSUES FOUND` — **6 Critical, 4 Major, 1 Minor survive adjudication. 12 critiques raised in Round 1. Open issues: 11 / 12.**

Nothing here is written to be encouraging.

---

## 0. The three specifications problem (read this first)

Everything below is harder to assess than it should be because there are **three mutually inconsistent specifications** of the same product, and only one of them is written down under the author's name:

| Layer | Artefact | Status | Teacher's role |
|---|---|---|---|
| **A** | The site / prospectus | Public, marketing | "Teachers see the whole classroom" — oversight |
| **B** | The PDF | Confidential, self-audited, Part I verified / Part II explicitly unbuilt | "Read-only analytics… The teacher is an observer, not a director" (PDF §6, §7) |
| **C** | The verbal pitch | Undocumented | Teacher is surveilled, coached, appraised, and has assessment auto-marked for them |

The first-pass report compared **A against B** and found a mismatch. It never noticed that **C is a third specification, and it is the largest of the three by an order of magnitude.** Every feature this document grills — camera engagement scoring, lesson-quality feedback to the teacher, assign-as-test with AI marking, per-student re-theming of assessed items, "deterministic level" diagnosis, learning-habit analysis, subchats pushed to school, night lab logins — appears in **C only**. None of it is in the PDF. Layer 3 of the PDF's Part II is the nearest relative, and the PDF itself calls it unbuilt, cold-start-blocked, and "realistically 2028+" (PDF §9).

That asymmetry is the single most important fact in this review. A partner who pitches C in a room where B is the honest inventory is not exaggerating a roadmap; they are describing a different company.

---

## 1. Competing hypotheses

**Question:** why would a joined school + home system improve learning outcomes for Kenyan CBC learners?

### Where I disagree with the first pass

The first report's H1–H5 are not wrong, but three of them are not hypotheses in the sense the skill's checklist requires — they are positions.

- **H1 ("complementary spaced practice")** bundles at least three separable mechanisms (spacing, retrieval, teacher-targeting) into one claim. If it "wins," you still do not know which lever to build. I split it into **G1** and **G2** below, which make opposite predictions about an easy experiment.
- **H2 ("face-as-engagement")** is scored *Weak on testability* with the reason "weak **in this fleet** (lighting, angle, occlusion)." That framing is too soft and, I think, actively harmful: it implies better cameras, better lighting, or a better model would rescue the hypothesis. The failure is **construct-level, not equipment-level** (Critique 2), and separately the binding constraint is **lawfulness, not testability**. I have therefore dropped it as a hypothesis about learning and replaced it with **G5**, a rival that predicts the intervention makes outcomes *worse*.
- **H5 ("consent collapse")** is a constraint, not a mechanism, and the first report says so itself. It belongs in the risk register, not the hypothesis table; carrying it as a hypothesis pads the count.
- **H3 (teacher-time)** and **H4 (null/incumbent)** survive in sharpened form as **G3** and **G4**.

The competing-hypotheses checklist also requires at least one null. The first report has one (H4). It has **no harm rival** — no hypothesis under which the product actively reduces outcomes. Given that the pitch adds surveillance of both children and teachers, that omission is not neutral. **G5** fixes it.

### The five rivals

Each specifies a mechanism, not a slogan.

- **G1 — Assignment authority.** The active ingredient is not spacing; it is that a *named teacher the child answers to tomorrow* set the work. Home practice completes because it is homework, not because it is well-timed. Mechanism: social obligation and accountability, individual→group level.
- **G2 — Error-set targeting.** The active ingredient is content precision: tonight's items are the exact items *this class missed today*, so practice lands on the actual gap instead of a generic strand. Mechanism: instructional targeting; the teacher supplies information the home system cannot infer.
- **G3 — Marking-time reallocation.** The gain is on the teacher's side of the ledger. At a rural peak the site gives as 1:60, the binding constraint is not planning but *marking and feedback latency*. Automating the aggregate view returns minutes that are re-spent on instruction. Mechanism: labour substitution → reallocation. Note the second, load-bearing clause: reclaimed teacher time is usually absorbed by other duties, not returned to teaching. The hypothesis fails if reallocation does not occur, even if the minutes are genuinely saved.
- **G4 — Null: access and selection.** There is no system effect. Households and schools that adopt already have a device, electricity, a supportive guardian, and an improving trend. The apparent effect is selection on device access and school trajectory. Mechanism: structural/institutional, not pedagogical.
- **G5 — Harm rival: surveillance substitution.** Adding engagement scoring, teacher lesson-grading, logged subchats, and night-lab identity *reduces* outcomes, because (i) pupils suppress visible confusion once looking-engaged is scored, (ii) teachers optimise the lesson for the observer rather than the learner, (iii) children stop confiding in a tutor chat they know a teacher reads, and (iv) the labelled "problem" child is treated as a problem. Mechanism: measurement changes behaviour — Campbell/Goodhart, plus stigma.

### Scoring against the seven criteria

| Criterion | G1 Authority | G2 Error-set | G3 Marking-time | G4 Null: access | G5 Harm |
|---|---|---|---|---|---|
| **Testability** | Strong | Strong | Moderate | Strong | Moderate |
| **Falsifiability** | Strong | Strong | Moderate | Strong | Moderate |
| **Parsimony** | Strong | Strong | Moderate | Strong | Moderate |
| **Explanatory power** | Moderate | Strong | Weak | Moderate | Moderate |
| **Scope** | Strong | Moderate | Strong | Strong | Strong |
| **Consistency** | Strong | Strong | Moderate | Strong | Strong |
| **Novelty** | Moderate | Moderate | Weak | Weak | Strong |

None scores Weak on both Testability and Falsifiability, so none is dropped. **G3** is the weakest and should not be led with: its explanatory power is poor (saved minutes are a cost story, not a learning story) and it is the most crowded market position.

### Distinguishing tests

| Pair | Divergent prediction | Feasible test |
|---|---|---|
| G1 vs G2 | If **G1**, attribution matters and content does not: "your teacher assigned this" beats "recommended for you" even when the items are identical. If **G2**, content matters and attribution does not: the teacher-targeted item set beats a generic same-strand set even when both are unattributed. | 2×2 on the existing pack contract: {attributed, unattributed} × {targeted items, generic strand items}. Costs one extra pack variant. **This is the highest-value experiment available and it needs no new data category.** |
| G2 vs G4 | If **G2**, the effect appears *within* households that already have a device. If **G4**, it disappears once you condition on device access. | Stratify by device access; sibling comparison within household. |
| G1 vs G4 | If **G4**, completion tracks device and electricity availability, not who assigned. | Log completion against a household device/power question asked once at onboarding. |
| G3 vs all | If **G3**, teacher-reported minutes fall *and* observed instructional time rises. If minutes fall but instruction does not rise, G3 fails even though the tool "works". | Teacher time diary, two weeks, before/after. |
| **G5 vs everything** | If **G5**, the *rate of pupils volunteering that they do not understand* falls after any observation feature is switched on, even where scores rise. | Count hand-raises / help requests / tutor-chat questions per lesson, before and after. **Do not need cameras to run this — that is the point.** |

**Critical experiment:** the G1×G2 2×2, run on the existing homework-pack contract with no new data category, no camera, no named child, and no teacher scoring. It eliminates or confirms the two mechanisms the whole join depends on, and it is the only one of these that can start this term. Everything in the pitch that is not G1 or G2 is currently a bet placed before the cheap experiment was run.

---

## 2. Round 1 — Adversarial critic

Hostile, competent, and specifically targeting what the first pass left alone.

### 1. "AI feedback to the teacher on how the lesson went" is a teacher-evaluation instrument with no validity evidence, and it will be captured for appraisal within one term

The pitch: the AI tells the teacher whether the lesson was good, whether they were engaging, and what to improve. Four separate failures, any one of which is disqualifying.

**(a) There is no ground truth.** Teacher effectiveness is one of the hardest measurement problems in education. Established observation protocols require trained, calibrated human raters, multiple lessons, and still produce modest inter-rater reliability. An LLM reading a transcript, or worse a stream of inferred engagement scores, has no access to what makes the lesson good: whether the explanation matched the misconception in the room, whether the teacher knew that this pupil's silence is different from that pupil's silence, whether the pacing was right for the last twenty minutes of a Friday.

**(b) The construct is inverted.** A good lesson frequently looks bad. Productive struggle, wait time, deliberate confusion, a teacher refusing to give the answer — these register as disengagement to anything scoring attentiveness. The system will systematically reward performance and penalise pedagogy. It is a lecture-detector, not a teaching-detector.

**(c) It will be used for appraisal.** This is the one that ends careers. Kenyan teachers already sit under TSC's appraisal regime (TPAD), which is document-heavy and lesson-observation-based. The instant a headteacher has a tool that emits a per-lesson quality number, that number enters the appraisal file — not because the vendor intended it, but because it is the only cheap, comparable, dated artefact in the building. The vendor cannot prevent this with a disclaimer. "Not for appraisal" is what every ed-tech metric that ended up in an appraisal file said first. Add the union dimension: KNUT and KUPPET will read an unvalidated, vendor-supplied, camera-fed teacher score exactly as it deserves to be read.

**(d) Observer effect.** Under continuous automated observation, teachers teach the metric. The prospectus positions this for "crowded classrooms in rural and peri-urban Kenya," where a teacher with sixty pupils cannot both teach and manage an audience.

Nothing in the PDF proposes this. It is a Layer-3-adjacent idea applied to adults instead of children, and Layer 3 is gated to 2028+ in Athena's own §9.

### 2. The camera is not a bad implementation of a good idea; the inference chain is broken at every link, and in a Kenyan classroom it is probably unlawful

The first report killed the camera. It killed it for the wrong reasons and left a door open. Its H2 row says the weakness is "lighting, angle, occlusion" — equipment. That invites the reply "so we get better cameras." Here is the version that does not have that escape.

**The chain has four links and each loses information:**

pixels → facial-muscle configuration → inferred affective state → inferred attention → learning.

- **Link 2→3 is the fatal one.** The scientific consensus that emerged from the large 2019 review of the affect-recognition literature (Barrett and colleagues, *Psychological Science in the Public Interest*) is that facial movements are not reliable, specific, or generalisable indicators of emotional state. People do not reliably scowl when angry, and scowling is not diagnostic of anger. "Bored" and "sleeping" are not facial expressions; they are interpretations. The pitch's four-class scheme (engaged / bored / sleeping / not listening) is not a measurement scheme, it is a teacher's folk vocabulary handed to a classifier as if it were a label set. There is no gold standard to train against — the labels would come from human raters guessing at the same faces.
- **Link 3→4 is weak** even where affect is measured properly. Attention is not visible. The child staring out of the window may be the one who has understood.
- **Link 4→5 is weak.** Engagement measures correlate with learning inconsistently, and the pitch's plan to combine them with test scores makes it worse, not better: if the scores already carry the signal, the face adds noise; if they disagree, you have no principled arbitration rule.
- **Demographic performance.** Face-analysis error rates are well documented to be worst for darker-skinned subjects, which is the entire user population here. A system whose error is concentrated in exactly one group, and whose output triggers teacher attention or punishment, is a discrimination machine with a technical alibi.

**The Kenyan legal frame the first report did not supply.** This repo's own long-term PRD §11 already cites the two instruments that matter: the Data Protection Act 2019, which treats biometric data as sensitive personal data and separately defines profiling; and the Data Protection (General) Regulations 2021, which identify processing of children's data as a case requiring a **data protection impact assessment**. Read together with ODPC's children's-data expectations, live facial analysis of minors in a classroom is a textbook high-risk processing operation: sensitive category, vulnerable data subjects, systematic monitoring of a public-facing space, automated evaluation with an effect on the individual. That is a mandatory DPIA before a single frame is captured, plus a lawful basis, plus a purpose limitation, plus retention rules, plus a data-subject rights route for a nine-year-old.

**And consent cannot be the lawful basis.** School attendance is compulsory and a pupil cannot decline to have their face processed without declining to attend the lesson. Consent obtained where refusal is not a real option is not consent. That is not a Kenyan technicality; it is the general structure of consent law, and it means the *only* available route would be a different lawful basis the vendor would have to argue for, in front of the regulator, on behalf of children.

**Power dynamics.** Finally, and separately from law: this puts a surveillance instrument in the hands of the adult who already controls the child's grades, discipline, and progression, in a setting the child cannot leave. "The system says you were not listening" is unanswerable by a nine-year-old. The evidentiary asymmetry is total.

This repo has already written the rule. `docs/LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md` §3 forbids "inferring disability, emotion, family circumstances, religion, ethnicity, or socioeconomic status from behaviour"; §6.1 forbids inference "from camera data"; §11 forbids "covert emotion analysis, camera monitoring." The prototype enforces it in code — `FORBIDDEN_PACK_KEYS` in `homework_pack.py` rejects `face`, `emotion`, and `biometric` at the contract boundary. The camera is not an open question in this partnership. It is a written non-goal with a unit test.

### 3. "Assign it as a test and mark it by their rules" converts a practice tool into an assessment instrument, and moves liability onto the teacher

Three of the pitch's features compose into something none of them is individually: the AI drafts the lesson, the teacher assigns it *as a test*, and the AI marks it against the teacher's rules.

- **Auto-marking is only safe where the answer is closed.** Athena's PDF is honest here: "bank answer-checking is local and deterministic" (§4) — the existing engine marks *its own* questions with *known* answers. The pitch replaces that with marking a teacher's free-form rubric against pupils' free-form answers. That is a different problem, it is the problem LLMs are worst at in a high-stakes setting, and its failures are not random: they punish unconventional phrasing, non-standard English, and exactly the rural learners the prospectus targets.
- **CBC runs on continuous assessment.** Competency-based assessment means school-based assessment tasks contribute to a pupil's record, and the PDF itself notes KPSEA weighting into Grade 9 placement (§10). Marks that feed a placement pathway are high-stakes marks. An AI-generated score inside that pathway needs, at minimum: a documented rubric, a human moderator, an appeals route, and a record of who is accountable for the mark. The pitch has none of the four.
- **Liability lands on the teacher, not the vendor.** The teacher signs off the mark. When a parent disputes it — and in an exam-conscious system they will — the teacher must defend a score they did not compute, from a rubric interpreted by a model they cannot inspect, with no audit trail. That is a professionally unsafe position to put a TSC-registered teacher in, and it is a very good reason for a school to refuse the tool.
- **KNEC integrity.** Anything touching assessment that feeds national placement sits near KNEC's remit. Athena's own risk register already flags an unresolved KICD approval question and a June 2026 directive about vendors and unapproved curriculum-support materials (PDF §7, §10). Adding *assessment* to *unapproved materials* is not a marginal increase in regulatory exposure; it is a category change.
- **And the market is already there.** Lesson-plan generation for CBC is a crowded, partly free segment in Kenya — downloadable CBC scheme-of-work and lesson-plan packs, plus local AI planning tools. The first report noted this and rated it **Minor** on the grounds of "don't lead with it." That rating is wrong. The market-crowding point is minor; the *assessment* extension bolted onto it is Critical, and rating the parent feature Minor buried the child.

### 4. Re-theming an assessed item per pupil destroys score comparability — and this repo has already written the argument against it

The pitch: the teacher plans a maths lesson, the AI tweaks it per pupil's interest to raise engagement, and (per feature 3) it can then be assigned as a test.

Take the second half seriously and it is a psychometric error, not a personalisation feature. If Amina's item is about football scores and Brian's is about matatu fares, and both are scored and compared, you no longer have one measurement. You have two items of unknown relative difficulty, differing in reading load, vocabulary familiarity, cultural context and number size, being treated as interchangeable.

`docs/LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md` §2 states it in plain terms already: *"A football-themed maths item can unintentionally measure football knowledge or English reading ability as much as maths."* §9.3 gives the rule that follows: *"Keep interest themes detachable from scoring. The same scored skill needs neutral and equivalent themed forms."* §7.2 goes further — LLMs *"must not freely create scored assessment items."*

Note also that this contradicts **Athena's own PDF**, not just this repo's. PDF §8 Layer 2 scopes interest re-framing as *re-explanation* — "rewrite the **explanation**" — triggered by a Layer 1 struggle flag. The PDF never proposes re-theming an assessed item. The pitch merges an explanation layer with an assessment layer and inherits the failure modes of both. It also inherits Layer 2's stated constraint, which the pitch omits: it "needs connectivity, so it degrades to nothing on the offline fleet" — that is, it does not work in the schools the prospectus says the product is for.

There is a defensible version, and it is much smaller: theme the *worked example and the explanation*, never the scored item; keep one neutral canonical form for anything that produces a mark.

### 5. "Deterministic level" is a promise the evidence cannot keep, and the data to compute it does not exist

The word is the tell. "Deterministic" claims certainty about *where* a pupil's problem is. Three problems, in increasing order of severity.

- **The method is probabilistic by construction.** The PDF's Layer 1 is Bayesian Knowledge Tracing — a *probability* of mastery with a credible interval, which on ten-to-twenty-five questions per strand (PDF §7: banks are "10–25 questions per strand") is wide. Calling a posterior with a wide interval a "deterministic level" is not a naming choice; it is a misrepresentation of the uncertainty to the person who will act on it. This repo's rule: *"Make skill estimates probabilistic and explainable: display 'not enough evidence' rather than fabricating precision"* (§9.3).
- **The input does not exist.** This is the concrete one. The PDF §8 states that `practice_results` stores `subject_name TEXT` and `question TEXT` — the question's prose — with *no* `question_id`, `strand`, `sub_strand`, or `difficulty`. Athena cannot currently group its own attempt log by concept "without fuzzy-matching prompt strings." It also says backfill is impossible, so the longitudinal clock has not started. **The feature being pitched as a capability cannot be computed from today's schema, and the earliest honest date for the first term of usable data is one term after a Phase 0 that has not shipped.** Add the browser/desktop fork (§8, unresolved) and the pilot cohort splits in two.
- **Cold start, stated by the author.** PDF Part II, "What is weak about it," item 3: meaningful signal needs months; *"pitching it as a reason for a school to sign up would be dishonest about when it starts working."* The pitch does exactly what the PDF forbids.
- **And the output is a labelling act.** "This student has issues in this" is a sentence a teacher will repeat, to the pupil, in front of the class, in a country where streaming and public ranking are live cultural practices. A wide-interval estimate delivered in confident language becomes a durable identity. That is the stigma mechanism, and it is the one thing on this list the pupil cannot appeal.

The first report banned "brain," "TRIBE," and "phantom brain," and ruled critique 2 "resolved if the pitch uses BKT wording." **"Deterministic level" is proof that the ban did not hold.** The overclaim did not need the banned vocabulary; it just found a new word. A wording ban is not a fix for a certainty problem.

### 6. Subchats pushed into the school side is a child-safeguarding hazard, not a data-flow feature

The pitch wants "subchats" looped into the school side. The PDF §5 records that the desktop schema already holds `chat_messages` — *"full tutor conversation history per student per subject."* So the artefact exists and it is a complete transcript of a child talking to a machine they believe is private.

The unasked questions, in order of severity:

1. **What happens when a child discloses harm?** Children tell chatbots things they will not tell adults — that is the documented appeal of the format. A tutor chat that runs for a term *will* receive a disclosure of abuse, neglect, hunger, or self-harm. Who reads it, within what time, under what obligation, escalating to which named person? Athena has no moderation layer described anywhere in the PDF, no safeguarding escalation policy, and no on-call human. A child disclosing into an unmonitored transcript is arguably worse than no channel at all: the disclosure has been made, it is on record, and nobody came.
2. **What happens when the teacher reads it?** "Pushed into the school side" means a member of staff can read what a child said when they thought they were alone with a tutor. That is a surveillance relationship, and it is the one that reliably destroys the tool's value: pupils who know the log is read stop asking the questions that reveal they do not understand. That is **G5** operating on the product's own core loop.
3. **Was the child told?** If not, it is covert. If yes, see (2).
4. **What is in the log?** Free child text is unbounded. Names, addresses, family circumstances, a sibling's illness. The prototype's `FORBIDDEN_PACK_KEYS` guard cannot help — it filters *field names*, not free text. This is precisely why `HOME_STUDY_COMPANION_V2_PRODUCT_PLAN.md` D7 chose interest **chips over free text**: "controlled chips need no moderation tooling or typed-child-text review." The subchat feature reverses that decision without acknowledging it, and reverses it in the direction of *more* text, *more* readers, and *longer* retention.
5. **Retention and breach.** A term of child chat transcripts on a school laptop is a high-value, unencrypted, physically portable dataset in a building with no IT security function. The site's own selling point is that the install is a USB drive onto existing classroom laptops.

### 7. Learning-habit analysis is trait inference wearing a different noun, with a shared-device confound that invalidates it

The pitch's "learning habits" and the PDF's Layer 3 "behavioral trait signals (consistency, follow-through, error-correction patterns)" are the same thing. The PDF is honest about it — "Inferred, not measured, traits… behavioral proxies, not validated psychometric instruments" — and gates it to multi-year data. The pitch drops both the honesty and the gate.

Beyond what the PDF concedes:

- **Proxy validity.** "Consistency" is measured as regular attempt timestamps. In this population that variable is dominated by electricity, shared-device availability, market days, harvest, illness, school fees, and whether an older sibling has the phone. You are measuring household infrastructure and calling it character. That is a direct hit on the fairness principle in this repo's §3 non-goals: no "inferring… family circumstances… or socioeconomic status from behaviour."
- **Shared-device confound, and it is worse on the school side.** This repo's `MVP_LAUNCH_CHECKLIST.md` already carries an open item: *"Implement a profile-selection flow for shared phones; never select the 'latest learner' by phone number."* On a classroom or lab PC the same confound is guaranteed: PDF §2 says the desktop app has "multi-student profiles," which means profile selection is a manual act by a child in a queue. Every mis-selected session writes one pupil's attempts onto another pupil's habit record. A trait model built on that data is not noisy, it is wrong in a way that cannot be detected after the fact.
- **The output has no safe use.** "This pupil has poor follow-through" is a character judgement delivered to an adult with authority over the pupil. Even where true, it licenses nothing pedagogically useful that the mastery estimate did not already license.

### 8. Night computer-lab logins reintroduce the named-child graph the whole architecture was designed to avoid — and nobody has costed the safeguarding

The first report rated this **Minor**, with the reasoning: it is "'run the same client on a lab PC,' not a third product. Preempt with one sentence." **That is the single worst call in the first report.** It is a correct statement about the software and a wrong conclusion about the product.

- **It breaks the privacy architecture.** The entire join design — presenter PRD §4 and §9, and the prototype's UUID `learner_token` — rests on one property: *the school knows the name, the home knows the pseudonym, and nothing joins them.* A pupil logging into "their account" on a school lab PC at night is the join. The school-authenticated identity and the personalised home-style tutoring now sit in the same session, on school hardware, under school administration. Every argument the first report used to rule out named cross-context linkage applies here with equal force, and the first report did not notice because it was thinking about the binary rather than the identity.
- **Shared accounts destroy the model anyway.** Lab machines with a queue of pupils and one convenient login produce a merged attempt history. Feed that to the "deterministic level" of critique 5, and you get confident statements about a composite child. The failure is silent.
- **The safeguarding is not a sentence, it is a policy.** Children on school premises after dark: staff supervision ratios, who holds the keys, who walks a Grade 6 girl home in Cherangany at 8pm, lighting on the route, incident logging, parental permission, the school's insurance position, and the TSC/board approval for after-hours use. The gendered safety asymmetry is the one that will be raised first and loudest by any county officer in the room, and "it's the same client" is not an answer to it.
- **The connectivity claim reverses.** If the lab has internet, then the AI works there and it is the *best* surface in the product — which quietly contradicts the offline-first positioning that the site leads with. If it does not, the pitch's "personalized tutoring" in the lab is the same offline practice engine, and the feature adds nothing except the risks above.

### 9. The in-lesson teacher sidekick has no interaction budget, and it does not work in the schools the product is sold to

"A chatbot that helps the teacher while teaching" assumes a teacher who has hands, attention, and connectivity. In the target classroom:

- **Attention.** The site's own headline number is 1:60 at rural peak. A teacher managing sixty pupils in a forty-minute period does not have a spare cognitive channel for a chat interface. Anything requiring the teacher to read and evaluate a suggestion mid-lesson competes directly with the classroom management that is the actual constraint.
- **The display is singular.** The "smart board" is the only projection surface. A sidekick panel either occupies the surface the class is looking at, or it lives on a screen the teacher must turn away to read. Both are costs; neither is discussed.
- **Connectivity.** The site's own statistic is ~30% of schools with reliable internet, and the PDF §7 states plainly that the on-device LLM path is out for the real fleet — government DLP tablets and Windows-10-class school PCs are "unlikely to run Ollama," so "the production runtime for AI is the Anthropic proxy, which requires connectivity, the thing schools do not have." A conversational live assistant is the *most* latency- and connectivity-sensitive thing you could build, aimed at the *least* connected fleet. It is the one feature category that structurally cannot work at Kuriot.
- **It contradicts the differentiator.** PDF §1: "It works offline… Neither differentiator gets traded for coverage volume." A live in-lesson chatbot trades it.

### 10. The pitch is a third specification that neither the PDF nor the site supports, and the PDF is the only one with an audit trail

Set out plainly (§0 above). Concretely: the PDF's §6 says the teacher layer is *read-only analytics* and calls the teacher-directed flow "the largest gap between the product vision and the shipped product." The site says "teachers see the whole classroom" — oversight. The pitch says the teacher is planned-for, marked-for, coached, scored, and observed by camera.

The risk is not that the pitch is ambitious. It is that **the PDF is a good document** — Part I is counted, §11 gives reproduction commands, §7 is a genuinely unflattering honest-gaps table, §10 is a real risk register. That credibility is the partnership's main asset in a mixed room, and it is spent the moment the verbal pitch outruns it. A grant reviewer who has read the PDF and then hears the camera pitch will re-read Part I looking for what else was inflated — and Part I did not deserve that.

Note also a smaller mismatch the first report did not catch: the site's prospectus advertises "adaptive practice" and an AI tutor that "asks, explains, and re-explains… in English and Kiswahili," while the PDF §7 records Grade 6 Kiswahili banks at zero and the AI as an enrichment layer that degrades to nothing offline. The first report caught the Kiswahili point; it did not catch that "adaptive" is also doing work the PDF does not evidence — adaptivity is what Layer 1 would provide, and Layer 1 is unbuilt.

### 11. "Smart-board integration" is worse than unspecified — the box behind the panel is frequently the wrong computer

The first report **dismissed** this, conditional on redefining it as "software on the classroom Windows PC." That reframe is right and I keep it, but the dismissal is premature on the hardware facts.

Interactive panels sold into African school markets commonly run an embedded Android system, with a Windows OPS module as a paid optional slot-in. Where there is no OPS module, the "computer inside the board" cannot run the Tauri/Windows NSIS installer the PDF §2 describes; it runs Android apps. Athena's Windows desktop build with Rust/SQLite and local Ollama is then not installable on the board at all — you are back to a teacher's laptop plugged into the panel as a display, which is a fine product and a completely different sentence from "integrates with the smart board."

The honest claim is narrow and still worth saying: *Athena installs from USB onto the classroom Windows laptop that drives the display.* Anything more requires knowing which panel each pilot school has, and that is a per-school survey question, not a product capability.

### 12. The pitch adds roughly seven subsystems to a company whose own risk register is already red

From the PDF §7 and §10, dated nine days ago by its own author: KPSEA content gap open with three learning areas at zero and nine weeks to the exam; KICD approval unresolved and blocking confident school-facing sales; the "parents will pay" thesis flagged as possibly wrong; CI dead account-wide with `cargo check` as the only compile gate; no pricing model in the repo; content depth below the revision-book bar. Phase B (teacher direction) is gated on pilot feedback that cannot exist before the pilot starts in September.

The pitch adds: computer vision, a teacher-coaching model, a lesson planner, an assessment generator, an auto-marker, a per-pupil re-theming pipeline, a habit/trait model, a chat-sync channel, and a night-lab access mode. Against six open High/Medium risks and a dead CI.

---

## 3. Round 2 — Defense

Strongest honest defense of each. Where there is no defense, that is stated rather than manufactured.

**1. Lesson feedback.** No defense of the pitched feature. One narrower thing survives and is genuinely valuable: a **private, teacher-owned, teacher-deletable self-review** — a checklist the teacher runs on their *own* lesson plan before teaching ("your three examples all use the same representation"; "you have no check for understanding before the independent task"). It operates on the plan, not on the pupils; it emits no score; the headteacher cannot see it; there is no export; nothing is retained after the teacher closes it. Everything that makes it useful is what makes it non-appraisable. If the buyer asks for an exportable score, the answer is no — and losing that sale is the feature working correctly.

**2. Camera.** No defense. Not "not yet," not "with better hardware," not "with consent." The construct is invalid, the population-specific error is discriminatory, the lawful basis is unavailable in a compulsory setting, and both partners have already written it down as prohibited. The correct action is a written non-goal in Athena's PRD, matching the one already in this repo, and the removal of the idea from the pitch — including the "later" version, because "later" is how the room hears "we intend to."

**3. Assign-as-test + auto-mark.** Split it and most of it survives. **Assign-as-practice** with deterministic marking of closed-form bank items is exactly what Athena already ships and is safe (PDF §4: "bank answer-checking is local and deterministic"). **Assign-as-assessment** with AI marking of open responses does not survive: no rubric governance, no moderation, no appeal, no accountable party, and the mark can reach a placement pathway. Concrete fix: the product may *draft* items and *suggest* a mark with the teacher's rule shown alongside; the teacher's confirmation is the mark; every AI-suggested mark is flagged as such in the record; and anything contributing to a formal CBA record requires teacher confirmation item by item. On the planner itself, the defense is honest and small: it is a commodity, it is a reasonable retention feature, it is not a wedge, and it must never be the lead slide.

**4. Interest re-theming.** Fully defensible in the PDF's own scope and indefensible outside it. Rule: **theme the explanation, never the scored item.** One canonical neutral form per assessed item; themed variants are unscored practice and worked examples only. This is already both repos' position — this repo's §9.3, and Athena's Layer 2 which says "rewrite the *explanation*." State the connectivity constraint the PDF states: it degrades to nothing offline, so it is a connected-school and consumer-app feature.

**5. "Deterministic level."** The underlying research programme is legitimate — BKT is standard, interpretable, and appropriate. The defense is entirely about honesty of expression and sequencing: never "deterministic"; always "estimated, from N recent attempts, with this much confidence"; always with the attempts visible to the teacher; always with "not enough evidence" as an available output. And it cannot be pitched at all until Phase 0 has shipped and a term of instrumented data exists — Athena's PDF says this in two places and the fix is to obey the document that has already been written.

**6. Subchats.** The transport is not the problem; the content and the reader are. Defensible version: push **derived, bounded artefacts** — "three learners asked for a re-explanation of regrouping" — not transcripts. Free-text child chat never leaves the device it was typed on and is never shown to a teacher as text. If a genuine transcript channel is ever wanted, it requires, before any child uses it: a named safeguarding lead, a written escalation protocol with response times, moderation, child-facing disclosure that a teacher can read it, guardian consent, a retention schedule, and a deletion route. That is a safeguarding programme, not a sprint, and it should be declined rather than half-built.

**7. Learning habits.** Two survivable pieces. (a) **Self-facing, opt-in, non-comparative** routine feedback to the learner — "you have practised four days this week" — which is what this repo's habit shell already does under D10/D11, with streak grace precisely because missed days signal electricity, not character. (b) **Aggregate, unattributed** class-level timing information to a teacher. What does not survive: any per-pupil trait vocabulary, any adult-facing character claim, and any inference about the household. Also: fix profile selection before any of it, or the data is wrong regardless of the model.

**8. Night lab.** The learning case is real — for many pupils the lab is the only device and the only quiet hour. The defense is to make it a *school* surface end to end, not a home surface running on school hardware: the pupil uses the school's existing local install under the school's existing identity, the session stays on that machine, and **no pseudonymous home account is used, linked, or created in the lab.** One identity, one context, no join. Plus a written after-hours policy owned by the school — named supervising adult, permission slips, incident log, lighting/transport, closing time — which is the school's to own but the vendor's to require before enabling the mode. If the school will not write it, the mode stays off.

**9. In-lesson sidekick.** Only the asynchronous forms are defensible: a **pre-lesson** planning pass and a **post-lesson** aggregate of who missed which item, both offline-capable, neither requiring the teacher's attention during instruction. Live conversational assistance during a lesson should be dropped, and the connectivity reality in PDF §7 is the reason to say so out loud rather than the thing to hide.

**10. Third specification.** Straightforward and entirely within the founders' control: the PDF is the specification. Anything in the pitch that is not in the PDF is labelled "idea, not roadmap," or it goes into the PDF where §9's gates apply to it. The PDF's own §12 open questions were written for exactly this conversation — the pitch should be answering them, not bypassing them.

**11. Smart boards.** "Athena runs on the classroom Windows laptop that drives the display" is true, sufficient, and verifiable. Add one line to the pilot-school intake form asking what the panel is and whether it has a Windows OPS module. The residual is a survey, not a risk.

**12. Roadmap inflation.** This one has a real defense, and it is the PDF itself. §9 gates every phase; Phase 0 and Phase A are explicitly the near-term work; and the document states outright that "Part II should not be able to block Part I's revenue path." The mechanism the critique demands already exists and was written by the person being criticised. It only fails if the gates are not honoured — which is a discipline question about the pitch, already covered by critique 10, not a second structural defect.

---

## 4. Round 3 — Adjudication

Impartial senior reviewer.

| # | Critique | Ruling | Reasoning |
|---|---|---|---|
| 1 | Teacher lesson-quality feedback | **Partially addressed** | The private self-review is a real, defensible product. But the defense concedes the pitched feature entirely, and it has no answer to appraisal capture beyond a promise not to export — a promise the vendor cannot keep once a headteacher is the buyer. Severity stays **Critical** because the harm is to an adult's livelihood and the union/TSC exposure is a partnership-level risk. |
| 2 | Classroom computer vision | **Stands** | No defense offered or possible. Construct invalidity, population-concentrated error, and unavailable lawful basis are independent disqualifiers. |
| 3 | Assign-as-test + AI marking | **Stands** (assessment half); resolved for practice | The split defense is correct and useful, but the pitch's actual words were "assigns it to children as a test" and "marks by their rules." As pitched, it stands. |
| 4 | Per-pupil re-theming of scored items | **Stands** | The fix is known, written in both repos, and simply not in the pitch. Until the pitch distinguishes explanation from assessment, this is an active error, not a resolved one. |
| 5 | "Deterministic level" | **Stands** | The defense is a wording change plus a sequencing promise. The first report already tried a wording ban and this critique exists because it failed. Independently, the input columns do not exist in Athena's schema, so the feature is not merely overclaimed — it is currently uncomputable. |
| 6 | Subchats to the school side | **Stands** | The "derived artefacts only" defense is sound but is a *different feature* from the one pitched. The pitched feature — child chat looped to school — has no safeguarding layer, and the absence of a disclosure protocol is disqualifying on its own. |
| 7 | Learning-habit analysis | **Partially addressed** | Self-facing habit feedback genuinely survives and already exists here. Adult-facing trait inference does not, and the shared-device confound is unfixed in both codebases. **Major.** |
| 8 | Night lab logins | **Partially addressed** | The "one identity, one context" fix does close the graph problem cleanly, and it is a good fix. Safeguarding remains unwritten and is the school's to own but the vendor's to require. **Major** — and note this is an upgrade from the first report's Minor. |
| 9 | In-lesson sidekick | **Partially addressed** | Dropping to pre/post-lesson async is the right call and salvages the useful part. Residual: the pitch's headline feature is precisely the live one, and PDF §7's connectivity reality kills it in the target fleet. **Major.** |
| 10 | Three specifications | **Stands** | Wholly unaddressed in any artefact. The pitch is not written down anywhere, which is both the problem and the reason it keeps growing. **Major.** |
| 11 | Smart-board hardware | **Partially addressed** | The reframe works; the OPS/Android residual is real but is a one-line intake question. **Minor** — a downgrade in confidence from the first report's outright dismissal, not an escalation in severity. |
| 12 | Roadmap inflation | **Resolved** | The PDF's §9 gates and its explicit "Part II must not block Part I" clause already implement the fix the critique demands. Enforcement risk is real but is critique 10, not a separate defect. Do not double-count. |

---

## 5. Devil's Advocate Report

### Critical

1. **AI feedback to the teacher on lesson quality.** — The defense concedes the pitched feature and cannot prevent appraisal capture; there is no validity evidence, no ground truth, and the construct rewards performance over pedagogy. — **Fix:** cut it. If anything ships, it is a private pre-lesson self-review on the teacher's own plan: no score, no export, no headteacher visibility, teacher-deletable, and a contractual refusal to supply per-teacher metrics to school management. Raise the union/TSC dimension proactively rather than being asked about it.

2. **Classroom computer vision / facial engagement scoring.** — Undefended and undefendable: facial movement is not a reliable indicator of affective state, error concentrates in the darker-skinned population that *is* the user base, and consent is unavailable where attendance is compulsory. Kenya's DPA 2019 and the 2021 General Regulations make this mandatory-DPIA, high-risk processing of children's sensitive data. — **Fix:** a written non-goal in *Athena's* PRD mirroring this repo's §3/§6.1/§11 and the `FORBIDDEN_PACK_KEYS` guard in code. No mock heatmaps, no faces on slides, and no "cameras later."

3. **Assign-as-test with AI marking against the teacher's rules.** — Stands as pitched: open-response AI marking has no rubric governance, no moderation, no appeal, and no accountable party, while CBC continuous assessment can feed Grade 9 placement. Liability lands on the teacher. — **Fix:** assign-as-**practice** with the existing deterministic bank marking. If a mark is ever AI-suggested, the teacher confirms item by item, the record flags it as AI-suggested, and nothing auto-marked enters a formal CBA record.

4. **Per-pupil interest re-theming of assessed items.** — Two differently themed items are two different measurements; comparing them is not personalisation, it is a construct-validity error. Both repos already forbid it in writing and the pitch contradicts both. — **Fix:** theme the explanation and the worked example; keep one neutral canonical form for anything scored. State the offline degradation, as Athena's own Layer 2 does.

5. **"Deterministic level" of where a pupil has a problem.** — A wide-interval posterior described with the vocabulary of certainty, delivered to an adult who will repeat it aloud, about a child who cannot contest it. And it is currently uncomputable: `practice_results` has no `question_id`, `strand`, `sub_strand`, or `difficulty`, and backfill is impossible (PDF §8). — **Fix:** ban the word; ship Phase 0 first; express every estimate as "from N recent attempts, with this confidence," with "not enough evidence" as a first-class output and the underlying attempts always inspectable.

6. **Subchats pushed into the school side.** — No moderation layer, no safeguarding escalation, no named responsible adult, no child-facing disclosure, no retention schedule. A term-long child transcript will contain a disclosure, and a disclosure nobody reads is worse than a channel that never existed. Also silently reverses this repo's D7 (chips over free text). — **Fix:** push derived, bounded artefacts only ("three learners asked for a re-explanation of regrouping"). Free child text stays on the device it was typed on. A real transcript channel requires a safeguarding programme first, and declining to build it is the correct answer for now.

### Major

7. **Learning-habit analysis.** — Self-facing habit feedback survives; adult-facing trait inference does not. "Consistency" measured from timestamps is a measure of electricity and device queues, and the shared-device/shared-profile confound is unfixed in both codebases. — **Fix:** keep habits self-facing, opt-in, non-comparative, with connectivity grace (this repo's D10/D11 already do this). No trait vocabulary in any adult-facing surface. Close the profile-selection item in `MVP_LAUNCH_CHECKLIST.md` before any timing data is modelled.

8. **Night computer-lab logins.** — The "one identity, one context" fix genuinely closes the named-child graph, but it must be stated explicitly because the default implementation reintroduces exactly the linkage the join architecture exists to prevent. Safeguarding is entirely unwritten. — **Fix:** lab sessions use the school's local install and school identity only; no home pseudonymous account is used, linked, or created there. Require a written after-hours policy from the school — supervising adult, permission, incident log, lighting and transport, closing time — as a precondition for enabling the mode. **This is an upgrade from the first report's Minor rating; see §6.**

9. **Live in-lesson teacher chatbot.** — No interaction budget at 1:60, one projection surface, and the most connectivity-sensitive feature aimed at the least connected fleet (PDF §7 says the production AI runtime needs the internet the schools do not have). — **Fix:** pre-lesson planning pass and post-lesson item aggregate, both async and offline-capable. Drop live conversational assistance and say why.

10. **The pitch is a third specification.** — It exceeds both the PDF and the site, it is written down nowhere, and it puts the PDF's hard-won credibility at risk. — **Fix:** the PDF is the specification. Anything else is labelled "idea, not roadmap," or it enters the PDF and inherits §9's gates. Answer PDF §12's open questions rather than routing around them.

### Minor

11. **Smart-board hardware.** — The reframe to "the classroom Windows laptop that drives the display" is correct and sufficient, but the residual is real: many interactive panels run embedded Android and cannot host the Tauri/Windows/SQLite build without an OPS module. — **Preempt:** one line on the pilot intake form asking the panel make and whether a Windows OPS module is fitted. Never say "integrates with smart boards" unqualified.

### Dismissed

- **Roadmap inflation as a structural defect (critique 12).** The PDF's §9 phase gates and its explicit "Part II should not be able to block Part I's revenue path" already implement the requested fix. Enforcement is critique 10; do not count it twice.
- **Knowledge tracing as a research programme.** BKT is standard, interpretable, appropriately chosen over DKT for the data volume, and correctly sequenced behind Phase 0. The objection is to the word "deterministic" and to the timing of the pitch, not to the method.
- **AI assistance with lesson planning per se.** Helping a teacher draft *their own* plan is legitimate and useful. It is a commodity and a retention feature, not a wedge, and it is only dangerous when it grows an auto-marker (critique 3).
- **"The night lab is a third product."** Correct as the first report said: it is the same client on a different machine. The problems are identity and safeguarding, not scope — which is exactly why the Minor rating on the wrong axis was misleading.
- **Interest-based personalisation in general.** Themed *explanation* is fine, cheap, already in both roadmaps, and not a differentiator (this repo's §2 says so plainly). Only the scored-item version is an error.

---

## 6. Where this pass contradicts the first pass

Stated explicitly, as requested.

| First report's position | This report's position | Why |
|---|---|---|
| Night computer-lab access — **Minor**, "preempt with one sentence" | **Major**, and the identity half is architectural | Correct about the software, wrong about the product. A school-authenticated login to a personalised tutor on school hardware *is* the named-child join the same report ruled out in its own Critical #4. Safeguarding after dark is a policy, not a sentence. |
| Full lesson-plan generation — **Minor**, "crowded market, do not lead with it" | Planning is Minor; **assign-as-test with AI marking is Critical** | The market point is right and the severity is misassigned to the parent feature. Rating the planner Minor buried the assessment extension that carries teacher liability and CBC/KNEC exposure. |
| Camera framed as **H2**, weak "in this fleet (lighting, angle, occlusion)" | Construct-invalid and lawfulness-blocked, not equipment-blocked | The equipment framing leaves the reply "so we buy better cameras." Facial movement does not reliably indicate affective state at all, and no camera fixes an unavailable lawful basis in a compulsory-attendance setting. |
| Smart boards — **Dismissed** conditional on the Windows-PC reframe | Reframe accepted; **kept as Minor** | The reframe is right but the hardware claim was dismissed without checking what the box behind the panel is. Embedded-Android panels cannot run the Tauri/Windows build. |
| Brain/TRIBE — "**resolved if** the pitch uses BKT wording" | A wording ban is not a fix for a certainty problem | "Deterministic level" carries the same overclaim without using a single banned word. Ban the certainty, not the vocabulary. |
| **H5 "consent collapse"** carried as a hypothesis | It is a constraint; moved to the risk frame | The report says so itself. Carrying it inflates the hypothesis count without adding a mechanism. |
| Five hypotheses, **no harm rival** | **G5** added | With surveillance of both children and teachers on the table, a rival under which the product reduces outcomes is not optional. |

**Where the first pass was right and this pass reinforces it:** kill the camera; the two apps are not one system; the named-child + home-device + video graph must not be built; the site/PRD mismatch is live presentation risk; KICD approval is unresolved; teacher direction is unbuilt on Athena today.

---

## 7. Salvageable, conditionally salvageable, unsalvageable

| Feature (founder's words) | Judgement | Condition |
|---|---|---|
| Camera/CV engagement scoring | **Unsalvageable** | None. Written non-goal, both sides, including the "later" version. |
| AI verdict on lesson quality / teacher engagingness | **Unsalvageable as pitched** | Only a private, non-exported, teacher-owned pre-lesson self-review survives, and only if management can never see it. |
| "Deterministic level" of a pupil's problem | **Unsalvageable as named** | The word goes. A calibrated, attempt-backed, inspectable mastery estimate after Phase 0 is fine. |
| Subchats pushed to the school side | **Unsalvageable as pitched** | Derived counts only. Transcripts require a safeguarding programme that should be declined for now. |
| Assign-as-test with AI marking | **Conditional** | Practice, not assessment. Deterministic marking of closed items. Teacher confirms any AI-suggested mark item by item; nothing auto-marked enters a CBA record. |
| Per-pupil interest tweaking | **Conditional** | Explanations and worked examples only. One neutral canonical form for anything scored. |
| Lesson planning assistant | **Conditional** | Teacher-owned draft, offline-capable, async. Commodity — never the lead slide. |
| Learning-habit analysis | **Conditional** | Self-facing, opt-in, non-comparative, connectivity-graced. No adult-facing trait language. Fix profile selection first. |
| Night computer-lab access | **Conditional** | School identity only, no home-account link, written after-hours safeguarding policy from the school before the mode is enabled. |
| Smart-board integration | **Conditional** | Say "the classroom Windows laptop that drives the display." Survey the panel per school. |
| In-lesson sidekick | **Conditional** | Pre-lesson and post-lesson only. Nothing live, nothing connectivity-dependent, in the target fleet. |
| The join itself (homework pack) | **Salvageable — and it is the only thing here with a cheap decisive experiment** | Run the G1×G2 2×2 before building anything else on this list. |

---

## 8. What survives

One sentence: **the only part of the pitch that is both buildable now and defensible in a mixed room is the assignment loop already prototyped in this repo — a teacher-set, item-targeted, token-only homework pack with no names, no faces, and no scores about people — and the honest next step is to run the attribution-versus-targeting experiment on it rather than to add a ninth subsystem to a roadmap whose own risk register is red on six items.**

Everything else in the pitch is either a research programme that Athena's PDF has already correctly gated to 2027–2028, or a feature that should not be built at all.
