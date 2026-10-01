# School–home assignment bridge — Capability manifest

> Version 0.1 | 26 August 2026
> Status: **Proposal** (engineering-facing). Not a release claim. Nothing here may be ticked in [MVP_LAUNCH_CHECKLIST.md](../MVP_LAUNCH_CHECKLIST.md) without separate verified evidence.
> Skill lane: `product-capability`. This repo has no `docs/examples/product-capability-template.md`; the skill's Output Format section order is used directly.
> Companion documents (this one does not replace them): [SCHOOL_HOME_JOIN_PRESENTER_PRD.md](SCHOOL_HOME_JOIN_PRESENTER_PRD.md) is the presenter-facing story; [ATHENA_EDUCLOUD_DEVILS_ADVOCATE.md](ATHENA_EDUCLOUD_DEVILS_ADVOCATE.md) supplies binding Critical findings; [HOME_STUDY_COMPANION_V2_PRODUCT_PLAN.md](HOME_STUDY_COMPANION_V2_PRODUCT_PLAN.md) supplies decisions D1–D19; [LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md](LONG_TERM_LEARNER_DISCOVERY_AND_ADAPTATION_PRD.md) supplies the ethical and Kenya DPA 2019 / ODPC gates.
>
> **Traceability of current-state claims.** Every "today the code does X" statement below is traceable to one of: `edu-cloud/backend/educloud/homework_pack.py`, `edu-cloud/backend/educloud/homework_views.py`, `edu-cloud/backend/educloud/urls.py`, `android-app/app/src/main/java/com/example/educloud/sync/HomeworkPack.kt`, `android-app/app/src/main/java/com/example/educloud/sync/HomeworkService.kt`, `android-app/app/src/main/java/com/example/educloud/ui/screens/home/TonightFromClassViewModel.kt`, `android-app/app/src/main/java/com/example/educloud/ui/screens/quiz/QuizSubjectCatalog.kt`. Statements about Athena Learn are traced to the Athena Learn PRD (Peter Kibet, Spidey Labs Ltd, 25 August 2026). No code was changed and no build or test was run while producing this document.

---

## CAPABILITY

A teacher working on the classroom Windows PC that drives the smart board can, at the end of a lesson, name the single skill the class just covered and issue it as tonight's revision, producing a short **class code** and a machine-readable **homework pack** that carries a skill identifier and item identifiers and nothing else; a learner at home on an EduCloud client (Android today; PWA or a night-time school-lab PC later) enters that class code, sees "Tonight from class," practises exactly those items in the existing verified content, and — if and only if the learner's household has opted in — returns per-item correctness under a pseudonymous UUID, so that the teacher's surface can show an **aggregate** of the form "*n* learners attempted, *m* missed item X" without ever learning which child did what. A caregiver surface is deliberately out of this first capability and enters later only through the existing D18 consent-code route. The outcome that changes is the after-class retrieval loop (hypothesis **H1** in the devil's-advocate report): the same skill is practised again the same evening, and the teacher gets item-level evidence of where the class is stuck the next morning, instead of guessing or — the explicitly rejected alternative — pointing a camera at children's faces.

**What this capability is not, in current reality.** Athena's teacher layer today is read-only analytics; the Athena PRD §6 states plainly that a teacher "cannot yet post what was covered in class, set an assignment, or steer what a student revises," and sequences that work as their Phase B, gated on Kuriot pilot feedback. So the *issuing* actor in the paragraph above does not exist in any shipped product yet. What exists in this repo is a labelled Django prototype that stands in for that actor. This manifest specifies the bridge contract so that whichever system eventually issues packs can do so without renegotiating the invariants.

---

## CONSTRAINTS

Constraints are marked **[POLICY]** (fixed; changing it requires a documented product, legal, or ethics decision), **[NARROW]** (a deliberate current-prototype narrowing that should widen later, with the widening condition stated), or **[GAP]** (something the contract asserts but the code does not yet enforce).

### C1. Content of the pack

| # | Constraint | Class | Enforced where |
|---|---|---|---|
| C1.1 | A pack carries only `schema_version`, `assignment_id`, `skill_id`, `item_ids`, `issued_at`, `class_code`, plus fixed provenance labels (`label`, `prototype`, `not_athena_production`, `not_facial_analysis`). No other caller-supplied field survives normalisation. | **[POLICY]** | `validate_pack` rebuilds the pack from scratch rather than merging caller keys (`homework_pack.py`). |
| C1.2 | Any key whose lowercased name is in the forbidden set — `name`, `names`, `alias`, `email`, `phone`, `photo`, `photos`, `face`, `faces`, `emotion`, `emotions`, `biometric`, `national_id`, `gps`, `location`, `roster`, `student_name`, `learner_name` — rejects the whole request. This applies to the pack, the attempt envelope, and every attempt row. | **[POLICY]** | `_reject_forbidden_keys` (Python) and the identical set in `HomeworkPack.kt`. Both sides duplicate the list deliberately: the home client does not trust the school side. |
| C1.3 | `schema_version` must equal `"1"` exactly. There is no version negotiation and no forward compatibility. | **[NARROW]** — widen when a second issuer exists; then define a minimum-supported-version rule rather than equality. | `validate_pack`, `validateHomeworkPack`. |
| C1.4 | `skill_id` must equal `two_digit_subtraction_regrouping`. Exactly one skill is assignable. | **[NARROW]** — widen to "any skill_id present in the receiving client's verified content catalogue," which today is Grade 3 Mathematics only (`QuizSubjectCatalog`). Widening is gated on content, not on code. | Both sides. |
| C1.5 | Every `item_id` must be drawn from the three-item allowlist `g3-regroup-45-29`, `g3-regroup-82-37`, `g3-regroup-63-28`; the list must be non-empty, unique, and no longer than the allowlist. | **[NARROW]** — same widening condition as C1.4. The *shape* (non-empty, unique, allowlisted against content the receiver actually has) is **[POLICY]**. | Both sides. |
| C1.6 | `assignment_id` must parse as a UUID. | **[POLICY]** | Both sides. |
| C1.7 | `issued_at` must be a string of at most 40 characters. It is length-bounded but **not parsed as a timestamp**, so a well-formed pack may carry a semantically meaningless `issued_at`. | **[GAP]** — a durable version must parse to an instant and reject future-dated or absurdly old values. | Both sides (length only). |
| C1.8 | `class_code` is normalised to upper case, must be 4–16 characters, and may contain only `A–Z`, `0–9`, and `-`. | **[POLICY]** for the charset and bounds; the specific value `G3-HOME` is **[NARROW]** demo fixture. | `_parse_class_code`, `normalizeClassCode` + `validateHomeworkPack`. |

### C2. Identity and trust boundaries

- **C2.1 [POLICY] The school roster never crosses the bridge.** Athena's `students` table (name, email, grade) stays in the classroom SQLite file. This is Critical finding 4 of the devil's-advocate report and is also what C1.2 mechanically enforces.
- **C2.2 [POLICY] The learner identifier on the bridge is a UUID and nothing else.** `validate_attempt_payload` rejects a `learner_token` that does not parse as a UUID, with the error text "learner_token must be a UUID, not a name."
- **C2.3 [POLICY] The token is random, never derived.** *Resolved 26 August 2026.* The earlier `HomeworkService.uuidOrDerived` fell back to `UUID.nameUUIDFromBytes(raw)` — a deterministic UUIDv3 — for any non-UUID input, so passing a child's name or admission number would have produced a stable, offline-reversible pseudonym for that identifier. That path is gone. `LearnerToken` now mints a random v4 UUID once per install and persists it in the `device` SharedPreferences; `HomeworkService.uuidOrNull` returns null for a non-UUID and the submission is `Rejected` without a network call. No token may ever be derived from a human-meaningful string.
- **C2.4 [POLICY] Home never stores the child's school name, and the pack is not an authentication artefact.** The class code is a *rendezvous* string, not a credential; it is short, guessable, and shared aloud in a classroom. Nothing behind it may ever be sensitive enough that guessing the code causes harm. This is the load-bearing reason the pack contains no personal data — the design assumes the code leaks.
- **C2.5 [GAP] There is no authentication, authorisation, rate limiting, or CSRF protection on any bridge route.** `homework_assign` and `homework_attempts` are `@csrf_exempt`, and any anonymous caller can issue an assignment or post attempts for any class code. This is acceptable only for a process-local, fixture-only demo. It is a hard blocker for any deployment reachable by a real learner.
- **C2.6 [POLICY] Direction of trust is asymmetric.** The school side may tell the home side *what to practise*. The home side may tell the school side *only an aggregate*. No school-side system may query an individual home learner's history through this bridge, now or later.

### C3. Data ownership and lifecycle

- **C3.1 [POLICY] Purpose limitation.** The only lawful purpose of data crossing this bridge is "completion of a named skill assigned by a teacher." Reuse for engagement scoring, teacher evaluation, marketing, or model training is out of purpose and requires a fresh consent and a fresh DPIA (long-term PRD §11).
- **C3.2 [POLICY] Aggregate-only egress to the teacher.** `aggregate_results` emits `assignment_id`, `skill_id`, `class_code`, `learners_attempted` (a set cardinality over tokens), and a per-item `{item_id, attempts, missed}` list. No token appears in the output. This is the invariant that keeps the teacher surface out of DPA-sensitive individual-profiling territory.
- **C3.3 [GAP] Small-*n* re-identification is unmitigated.** With one learner attempting, `learners_attempted: 1` plus a per-item miss list *is* an individual record from the teacher's point of view, because the teacher knows who they assigned it to. A durable implementation needs a minimum-cohort threshold (suppress or coarsen below *k* learners) before showing item detail.
- **C3.4 [POLICY] Cloud sync and re-explanation stay consent-gated and disabled by default** (long-term PRD §11; product plan D15). The Android client honours this by shipping `HOMEWORK_BASE_URL` as a build-config value and treating anything that is neither `https://` nor a loopback host as `Disabled` — no network call is attempted at all (`HomeworkService.normalizeEndpoint`).
- **C3.5 [POLICY] Never a column.** No production schema for this bridge may contain a learner name, alias, photo, face embedding, emotion or engagement score, national ID, phone number, GPS or location, school roster key, `iq_score`, `eq_score`, `disability_prediction`, `giftedness_label`, `behavioural_emotion_score`, or an opaque learner-ranking field (long-term PRD §9.2, which states this prohibition in those exact terms).
- **C3.6 [GAP] Retention and deletion are undefined.** Today the store is a process-local dictionary, so "deletion" is a process restart. A durable store needs a stated retention window for attempt rows, an export/correction/deletion path, and a defined behaviour when a household revokes consent after aggregates have already been displayed.

### C4. Volume and abuse bounds

| Bound | Value | Class |
|---|---|---|
| Request body size, backend | 2048 bytes (`MAX_PACK_BYTES`); oversize returns HTTP 400 | **[POLICY]** shape, **[NARROW]** value |
| Pack size, Android parse | 2048 bytes (`MAX_HOMEWORK_PACK_BYTES`), checked before JSON parsing | **[POLICY]** |
| Attempt rows per POST | 10 (`MAX_ITEMS_PER_ATTEMPT_POST`) | **[NARROW]** — must be at least the pack's `item_ids` length once packs grow |
| Distinct submissions per assignment | 40 (`MAX_ATTEMPTS_PER_ASSIGNMENT`) | **[NARROW]** — a real class of 60 (the pupil–teacher ratio the presenter PRD cites) already exceeds this |
| Class-code length | 4–16 characters | **[POLICY]** |
| Network timeout, Android | 8000 ms connect and read | **[NARROW]** |

### C5. Idempotency and concurrency

- **C5.1 [POLICY] Re-submission by the same token replaces, never appends.** `record_attempts` filters out any prior row for that `learner_token` before appending, so a retry after a flaky connection cannot inflate `learners_attempted` or double-count a miss. This must survive into any durable store as an upsert keyed on `(assignment_id, learner_token)`.
- **C5.2 [GAP] Replacement is whole-submission, not per-item.** A learner who submits three items and later re-submits one item ends up with one item on record. With a durable store the correct key is `(assignment_id, learner_token, item_id)`.
- **C5.3** The in-memory store serialises all access behind a single `threading.Lock` and returns deep copies. That is correct for one process and meaningless across the multiple workers a real deployment would run. **[GAP]**
- **C5.4 [GAP] Re-assigning a class code destroys history.** `assign_demo` overwrites the entry for `G3-HOME` with a fresh `assignment_id` and an empty submissions list. A durable model must key assignments by `assignment_id` and treat `class_code` as a lookup alias with a lifetime, so that issuing tomorrow's revision does not erase tonight's evidence.

### C6. Product-decision constraints that bind this capability

- **C6.1** Product plan **D9**: Athena Learn is a partner and channel, **not a dependency**, and a governance agreement is required first. Concretely: EduCloud's home loop must remain fully functional with no Athena present. The Android client honours this by shipping a bundled fixture pack (`DEMO_HOMEWORK_PACK_JSON`) and by falling back to it when the network is unavailable.
- **C6.2** Product plan **D17** demotes teacher/classroom integration to a future channel. This capability is a **deliberate, narrow exception** to D17 and must be named as such wherever it appears. The boundary of the exception: EduCloud may *consume* a pack and *emit an aggregate*. EduCloud may not grow class management, rosters, attendance, grading, fees, timetabling, or reporting. If a proposed change would make EduCloud usable as a school information system, D17 wins and the change is rejected.
- **C6.3** Devil's-advocate Critical findings 1–4 are binding non-goals, restated in the NON-GOALS section below.
- **C6.4** Checklist discipline (`MVP_LAUNCH_CHECKLIST.md`): a claim is not complete until it is verified with evidence. This document asserts no completion.
- **C6.5 [POLICY] The prototype must stay labelled.** The banner string in `homework_views.py` — "Contract prototype. Not Athena production. Not facial analysis. No names, photos, or classroom cameras." — and the `prototype` / `not_athena_production` / `not_facial_analysis` flags stamped into every pack and every error body are part of the contract, not decoration. Removing them requires the thing they deny to have actually become true.

---

## IMPLEMENTATION CONTRACT

### Actors

| Actor | Description | Exists today? |
|---|---|---|
| **Issuing teacher** | Teacher at the classroom Windows PC who names the covered skill and issues tonight's revision | **No.** Athena PRD §6: read-only analytics, teacher direction is their Phase B. Stood in for by the Django prototype page. |
| **Learner at home** | Child on a household Android phone entering the class code | Yes — `TonightFromClassViewModel` + the Grade 3 Maths quiz |
| **Reviewing teacher** | Same human as the issuing teacher, next morning, reading the aggregate | Prototype only, at `/demo/teacher/sidekick` |
| **Caregiver** | Later, via the D18 consent-code link and weekly digest | **No.** Explicitly out of scope for v1 of this capability. |
| **Operator** | Whoever runs the EduCloud backend | Undefined; see observability below |

### Surfaces

| Surface | Role | State |
|---|---|---|
| Athena classroom app (Tauri desktop, local SQLite, USB install) | Future pack issuer | Not built for this; no export format agreed |
| EduCloud Django prototype page `GET /demo/teacher/sidekick` | Labelled stand-in issuer + aggregate view | Built; renders a static HTML file with string substitution because Django templates crash the Python 3.14 test client — a known workaround, not a pattern to copy |
| EduCloud Android | Pack consumer, quiz runner, optional attempt submitter | Built; network path disabled unless `HOMEWORK_BASE_URL` is an HTTPS or loopback origin |
| EduCloud PWA / night-time school-lab PC | Future consumer | Not built (product plan D19 sequences the PWA after the Compose app) |

### States and transitions

**Assignment lifecycle (target).** Prototype coverage is marked on each transition.

```text
                 teacher composes
  (none) ──────────────────────────▶ DRAFTED        [not implemented — prototype has no draft step]
                 issue / publish
  DRAFTED ─────────────────────────▶ ISSUED         [implemented: POST .../assign → 201 pack]
                 learner enters class code, pack fetched
  ISSUED ──────────────────────────▶ CLAIMED        [not tracked — GET pack is a read with no server-side effect]
                 learner posts item results
  CLAIMED ─────────────────────────▶ ATTEMPTED      [implemented: POST .../attempts → 201 aggregate]
                 teacher reads the roll-up
  ATTEMPTED ───────────────────────▶ AGGREGATED     [implemented: GET .../results, and recomputed on every write]
                 issued_at + TTL passes
  ISSUED|CLAIMED|ATTEMPTED ────────▶ EXPIRED        [not implemented — no TTL, no expiry, packs live until process exit]
```

Three consequences of the gaps, stated plainly:

1. **CLAIMED is unobservable.** A teacher cannot distinguish "nobody opened it" from "everybody opened it and nobody submitted," because fetching a pack leaves no trace. If that distinction has product value, it needs a claim event — and a claim event is a new data point about a child, so it needs its own purpose justification, not a silent addition.
2. **EXPIRED does not exist**, so a class code is valid forever and is reusable by anyone who ever heard it. See open question OQ2.
3. **Re-issuing is destructive** (C5.4), so the lifecycle above is not actually a per-assignment state machine today; it is a single mutable slot per class code.

**Pack lifecycle on the home client.** `TonightAssignment` is a process-wide `@Volatile` singleton holding at most one pack. Accepting a new pack replaces the previous one; `clear()` drops it. There is no persistence, so an app restart loses tonight's assignment and the learner must re-enter the code. For a durable version the pack belongs in the existing Room database with the learner's local token.

### Interfaces

All four routes live under the demo namespace in `urls.py` and return `application/json`. Every error body is `{"error": "<message>", "prototype": true}`. There is no authentication on any of them.

---

**`POST /api/v1/demo/homework/assign`** (also accepts `GET`, which returns the current demo pack without creating one)

Request: an empty body, or a JSON object. Behaviour, in the order the code checks it:

| Condition | Result |
|---|---|
| Body > 2048 bytes, or not a JSON object | `400` "Assignment request is too large or is not a JSON object." |
| Body contains a forbidden key | `400` listing the offending keys |
| `skill_id` present and not `two_digit_subtraction_regrouping` (empty string and `null` are tolerated) | `400` "Unknown or unsupported skill_id." |
| Body contains `assignment_id` or `item_ids` | The merged pack is validated first (so a malformed custom pack returns its specific validation error), then **always** `400` "Custom packs are out of scope for this prototype." |
| Otherwise | `201` with a freshly generated demo pack |

Response (201):

```json
{
  "schema_version": "1",
  "assignment_id": "<uuid4>",
  "skill_id": "two_digit_subtraction_regrouping",
  "item_ids": ["g3-regroup-45-29", "g3-regroup-82-37", "g3-regroup-63-28"],
  "issued_at": "2026-08-26T09:00:00Z",
  "class_code": "G3-HOME",
  "label": "Tonight from class · Grade 3 regrouping (prototype)",
  "prototype": true,
  "not_athena_production": true,
  "not_facial_analysis": true
}
```

Note the honest reading: this endpoint does not accept an assignment, it *mints the fixture*. A teacher cannot choose a skill or a subset of items. Real issuance is unimplemented and is the single largest gap between the founder's intent and the code.

---

**`GET /api/v1/demo/homework/<class_code>`**

`200` with the pack shape above; `404` "Unknown class_code. Assign tonight's revision first." for a well-formed but unknown code; `400` for a code that violates the charset or length bounds. The literal code `G3-HOME` is auto-created on demand, so it never 404s — a demo affordance that must not survive into production, because it means an unknown code is silently valid.

---

**`POST /api/v1/demo/homework/<class_code>/attempts`**

Request:

```json
{
  "learner_token": "<uuid>",
  "attempts": [
    {"item_id": "g3-regroup-45-29", "correct": true},
    {"item_id": "g3-regroup-82-37", "correct": false}
  ]
}
```

Rejections (all `400` unless noted): body over 2048 bytes or not an object; any forbidden key at envelope or row level; `learner_token` not a UUID; `attempts` absent, not a list, or empty; more than 10 rows; an `item_id` that is missing, duplicated within the request, or not in this assignment's `item_ids`; a `correct` value that is not a JSON boolean; the assignment already holding 40 distinct submissions. Unknown class code returns `404`.

Response `201`: the recomputed aggregate (same shape as results, below). Re-posting with the same token replaces the earlier submission (C5.1).

---

**`GET /api/v1/demo/homework/<class_code>/results`**

```json
{
  "assignment_id": "<uuid>",
  "skill_id": "two_digit_subtraction_regrouping",
  "class_code": "G3-HOME",
  "learners_attempted": 3,
  "items": [
    {"item_id": "g3-regroup-45-29", "attempts": 3, "missed": 1},
    {"item_id": "g3-regroup-82-37", "attempts": 3, "missed": 2},
    {"item_id": "g3-regroup-63-28", "attempts": 2, "missed": 0}
  ],
  "prototype": true
}
```

`404` for an unknown class code. `items` always covers every `item_id` in the pack, zero-filled, so the teacher view distinguishes "nobody attempted this item" from "the item is not in the assignment."

---

**Client-side contract (Android).** `HomeworkService` returns one of four outcomes for a fetch — `Ready`, `Disabled` (no usable base URL configured), `Rejected` (HTTP 400/404, or a body that fails local pack validation), `Unavailable` (any other status, or an `IOException`) — and the parallel three for a submit. Two behaviours deserve explicit review rather than quiet acceptance:

- The client re-validates every fetched pack against its own copy of the rules and downgrades a `200` with a bad body to `Rejected`. This is correct and should be preserved: the home client does not trust the school side.
- `TonightFromClassViewModel` silently substitutes the bundled fixture whenever the entered code is `G3-HOME` and the network path did not produce a pack. During a demo this makes an offline device look identical to a live round trip. That is acceptable only because the presenter script and the on-screen labels say the pack is a prototype; it must not be carried into a build a real family uses, where the learner should be told the assignment is a sample rather than tonight's actual class work.

### Data-model implications

The store is `HomeworkStore`, a `dict` in module-level `STORE` inside one Python process. It is not a database. Restarting the server, or running a second worker, loses or forks all state. Making this durable needs roughly:

```text
assignment
  assignment_id      uuid  primary key
  schema_version     text
  issuing_source     text        -- which system emitted it (see OQ1)
  skill_id           text        -- FK to a verified content catalogue, not free text
  issued_at          timestamptz -- parsed, not just length-bounded (fixes C1.7)
  expires_at         timestamptz -- fixes the missing EXPIRED state (see OQ2)
  created_at         timestamptz

assignment_item
  assignment_id      uuid  FK
  item_id            text        -- FK to the content catalogue
  position           int
  primary key (assignment_id, item_id)

class_code
  code               text  primary key   -- A-Z0-9- , 4..16, normalised upper
  assignment_id      uuid  FK
  issued_at          timestamptz
  expires_at         timestamptz
  revoked_at         timestamptz null    -- rotation / single-use (see OQ2)

attempt
  assignment_id      uuid  FK
  learner_token      uuid                -- random v4 from the client, never derived (fixes C2.3)
  item_id            text  FK
  correct            boolean
  submitted_at       timestamptz
  primary key (assignment_id, learner_token, item_id)   -- fixes C5.2

consent_grant
  learner_token      uuid
  purpose            text        -- 'homework_aggregate_return'
  granted_at, revoked_at, consent_version
```

Design notes that are part of the contract, not preferences:

- **`learner_token` is the only identity column and it is opaque.** It has no foreign key to any person, household, phone number, or device fingerprint anywhere in the system. If a future feature needs to know "which child," that feature does not belong on this bridge.
- **Never a column, anywhere in this schema:** the full C3.5 list. A migration that adds one of those fields should fail review on sight.
- **Aggregation stays a query, never a stored per-learner profile.** The moment a `learner_state` row keyed on `learner_token` and visible to a teacher exists, this stops being an anonymous homework bridge and becomes learner profiling under the Kenya DPA, with a DPIA gate in front of it.
- **Consent precedes the write, not the read.** An attempt row should not be written at all without a live `consent_grant`; filtering at aggregation time is the wrong place, because the sensitive data already exists by then.
- **The content catalogue is the authority for `skill_id` and `item_id`.** Today that authority is a hard-coded tuple in two languages. It should become one generated source of truth so the Python allowlist and the Kotlin allowlist cannot drift — the repo already uses generated-file drift rejection for the Grade 3 rule cards, and the same discipline applies here.

### Security, policy, and rollout constraints

Before any deployment a real learner can reach:

1. Authentication and authorisation on issuance (C2.5). Anonymous `POST .../assign` is not deployable.
2. Rate limiting per client on all four routes; CSRF handling appropriate to the real caller.
3. A minimum-cohort threshold on aggregate display (C3.3).
4. ~~Random v4 learner tokens with no derivation path (C2.3).~~ Done 26 August 2026.
5. HTTPS-only in any non-loopback configuration — the Android side already enforces this direction; the backend must not be served over plaintext.
6. DPIA and verifiable guardian consent covering the aggregate return path, per long-term PRD §11 and open question OQ5.
7. A written data-governance agreement with Spidey Labs before any pack originates from Athena (D9; their risk register; OQ6).

Rollout should stay behind the existing pattern: disabled by default via build configuration, loopback or explicit HTTPS only, bundled fixture as the always-available path.

### Observability and operator requirements

The prototype has none, and that is defensible only while state is a dictionary. A durable version needs:

- **Counters, not payload logs:** packs issued, codes claimed, attempts accepted, attempts rejected split by rejection reason (forbidden key, bad token, unknown item, over-cap, oversize body). The rejection-reason breakdown is the operational signal that the contract is holding; a sustained rise in forbidden-key rejections means an upstream issuer is trying to send personal data and should be paged on.
- **A hard rule that request and response bodies are never written to logs**, since a rejected body is precisely the one most likely to contain a child's name.
- **Latency and error rates per route**, plus store size and per-assignment submission counts, so the 40-submission cap is observed rather than discovered by a class of 60.
- **An audit trail of issuance** (who issued, when, which skill) once issuance is authenticated. This is teacher-side data, not child data, and it is what makes "purpose limitation" auditable rather than aspirational.
- **An operator runbook entry for consent revocation:** how to delete a token's attempt rows and what the teacher's already-rendered aggregate should do afterwards.

---

## NON-GOALS

This capability explicitly does not own, and must not grow:

1. **Classroom computer vision of any kind** — no cameras, no face detection, no attention or gaze tracking, no "who is sleeping" detection, no mock heatmaps, and no "cameras later" tease. Devil's-advocate Critical 1; the defence failed adjudication. The long-term PRD independently forbids covert emotion analysis and camera monitoring.
2. **Affect or engagement inference from any signal**, including keystroke timing, response latency, session length, or time-of-day. The bridge carries `correct: true|false` and nothing that could be reprocessed into an emotional or motivational state.
3. **Teacher-performance scoring.** Aggregates describe items, not the adult who taught them. No class-versus-class comparison, no teacher league table, no export shaped for an administrator judging a teacher.
4. **Trait, IQ, EQ, personality, giftedness, or disability inference.** Athena's Part II Layer 3 is their proposal on their roadmap with their gates (multi-year data, realistically 2028+); it is not part of this bridge in any form, and the long-term PRD names those fields as permanently forbidden.
5. **Auto-marking of graded assessment.** Item correctness here is formative practice evidence. It is not a mark, not a grade, not a report-card input, and must never be presented as one.
6. **A competing school information system.** No fees, exams, report cards, attendance, timetabling, or admissions. Product plan D17 stands; this bridge is a named narrow exception, not the first module of a SIS.
7. **Named-roster sync in either direction.** No student list, no admission numbers, no class list, no "match this home learner to that school record." The absence of a join key is the safety property, not an inconvenience to be engineered away.
8. **Smart-board vendor SDKs.** "Smart board" here means the Windows PC that already drives the board. No Promethean, SMART, or equivalent integration. Devil's-advocate finding 7 is resolved only under that definition.
9. **A claim that Athena and EduCloud are one live system.** They are two products with no shared database, no shared learner, and currently no overlapping grade. Devil's-advocate Critical 3.
10. **Brain, neural, TRIBE, fMRI, or "phantom brain" framing** for anything this bridge produces. Predicted struggle, if it is ever built, comes from practice attempts and is described that way.

---

## OPEN QUESTIONS

Ordered by how hard they block an end-to-end pilot.

**OQ1 — Who issues packs, and in what format?** Athena's teacher-direction work is their Phase B, gated on Kuriot pilot feedback (Athena PRD §9), so today there is no system that can emit a pack from a real classroom. Unresolved: does Athena export a JSON pack over a local file or USB, expose a localhost endpoint from the Tauri app, or post to an EduCloud endpoint over the connectivity their own PRD says schools mostly lack? Their `practice_results` table also lacks `question_id`, `strand`, `sub_strand`, and `difficulty` — their Phase 0 — so they cannot currently name an item stably enough to fill `item_ids`. **Their Phase 0 is a prerequisite for our `item_ids` field to mean anything on their side.** Until an export format is agreed in writing, every issuance path in this document is speculative. *Blocks: any non-prototype issuer.*

**OQ2 — Do class codes expire, rotate, or become single-use?** Today a code is permanent, reusable, guessable within a 4–16-character alphanumeric space, unauthenticated, and destructive on re-issue (C5.4). Options span a TTL tied to `issued_at`, one code per assignment with rotation on re-issue, and single-use claim tokens per learner. Each has a different classroom cost: a teacher reading a code aloud to 60 children cannot manage per-learner tokens, and a code that expires overnight breaks the child who does homework the next morning. This needs a product decision informed by an actual classroom, not an engineering default. *Blocks: any deployment where a code reaches more than one household.*

**OQ3 — Grade mismatch: there is currently no real child who can use both products.** Athena covers Grades 6–9 (75 banks, 1,344 questions). EduCloud's verified content is Grade 3 Mathematics only — one skill and three items (`QuizSubjectCatalog`, and the allowlists in both bridge implementations). **The overlap is empty.** Every demonstration of this bridge is therefore Grade 3 content moving between two EduCloud surfaces, with Athena represented only by the contract shape. Closing it requires either EduCloud building verified Grade 6–9 content (a content programme with its own provenance and review gates, not a sprint) or Athena adding Grade 3 (against their stated JSS wedge and their KPSEA priority). Neither is currently planned. This is a genuine blocker for an end-to-end pilot with a real child, and the presenter guidance to run one persona per demo beat is a presentation mitigation, not a resolution. *Blocks: any pilot claim involving a real cross-product learner.*

**OQ4 — How does a shared home device map to learner tokens?** The presenter PRD's own problem statement says revision happens on shared phones. `TonightAssignment` holds exactly one pack process-wide and `HomeworkService` takes a single token per submission, so two siblings on one phone currently either collide into one token — corrupting `learners_attempted`, which counts distinct tokens — or need a profile-switch concept that does not exist. Any per-child profile on a shared device also reintroduces the question of how a child selects "which one am I" without a name, which is precisely the field the contract forbids. *Blocks: honest aggregate counts in the shared-device case, which is the common case.*

**OQ5 — What guardian consent is required before any aggregate flows back to a teacher?** The long-term PRD requires verifiable guardian consent, age-appropriate child assent, separate consent for teacher sharing specifically, and a DPIA before collection — and pseudonymised child data is still child data under the Kenya DPA 2019, with ODPC regulations naming children's data as a DPIA trigger. Unresolved: who obtains consent (school or EduCloud), what the consent artefact is on a shared feature-limited phone, whether a class-level aggregate with a *k*-threshold falls below the profiling threshold, and what happens to a displayed aggregate after revocation. There is no consent mechanism in the bridge code at all today. *Blocks: any attempt submission from a real child.*

**OQ6 — The Athena data-governance agreement is unsigned.** Product plan D9 makes a governance agreement a precondition for activating the partnership, the plan's own open-questions list names it as blocking D9, and the presenter PRD carries it as risk R7. Until it exists there is no agreed answer on data controllership, incident response across two companies, IP in the pack schema, or what either party may say publicly about the other. *Blocks: any joint pilot or joint public claim.*

**OQ7 — Where does this run, and who operates it?** Deployment target, TLS termination, worker topology (which invalidates the single-lock in-memory store), backup, and on-call are all undefined. Lower priority only because OQ1–OQ6 must resolve first.

---

## HANDOFF

**Verdict: needs product clarification first — it is not ready for direct implementation, and architecture review alone would not unblock it.**

The engineering contract is unusually well specified for a week-one prototype: the pack shape, the forbidden-key policy, the UUID-only token rule, the aggregate-only output, and idempotent re-submission are all real, tested-in-spirit invariants that a durable implementation can adopt as written. If the only question were "how do we make this persistent and safe," this would be an architecture-review item with a clear scope (authentication, a real schema, *k*-thresholding, random tokens, expiry).

It is not, because three product facts are unresolved and two of them are hard blockers. OQ3 is the sharpest: **there is no grade overlap between the two products, so no real child can currently use both**, which means no end-to-end pilot exists to build toward regardless of how good the bridge code becomes. OQ1 means the issuing actor in the capability statement is unbuilt on the partner side and needs their Phase 0 and Phase B first. OQ5 means no real child's attempt may be submitted at all until consent and a DPIA are in place. Building durable infrastructure ahead of those three would be building for a user who cannot exist yet.

There is also a standing tension worth restating rather than smoothing: the founder's intent describes a teacher-facing product — lesson planning, assigning, per-student tweaking, flagging struggling students, subchats looped back to school — while product plan **D17** demotes teacher/classroom integration to a future channel, and the devil's-advocate report rules the camera-based engagement component a hard non-goal. This manifest deliberately scopes only the thin bridge, which is the one piece that survives both constraints. The larger teacher-assistant product in the founder's description is a different capability with a different owner, and it would need D17 to be revisited on its merits rather than extended quietly through this exception.

**Next lane.** Take OQ1, OQ3, and OQ5 to a product decision session with the Athena partner before any further build; that conversation, not a design doc, is the blocker. Once those close, the follow-on engineering lane is `api-connector-builder` for a versioned, authenticated pack interface against Athena's agreed export format, followed by `tdd-workflow` for the durable store and the consent gate, with `verification-loop` supplying the evidence that `MVP_LAUNCH_CHECKLIST.md` requires before anything here is claimed as shipped.
