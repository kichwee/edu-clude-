# Grade 3 Mathematics Proof-Carrying Tutor — MVP Build Plan

## Purpose

Turn one permissioned Grade 3 Mathematics pupil's book into an offline-first Android tutor. The tutor explains only concepts supported by a retrieved textbook passage, displays the lesson/page source, and refuses questions outside the available source evidence.

## Scope lock

- **Learners:** Grade 3, English.
- **Subject:** Mathematics only. Environmental Activities/Science is not in this MVP.
- **Knowledge source:** `GRADE  3 .pdf`, supplied by the project owner. It is a 300-page Grade 3 Mathematics book structured by term, topic, week, lesson, activity, and practice work.
- **Product path:** Android, offline-first, no web search, accounts, or child-data collection.
- **Initial content:** a bundled Term 1–3 local source index, with four curated
  rule cards used for exact-answer demonstrations and the remaining lessons
  used for source-bounded discovery and explanation.
- **Non-goals:** General chat, curriculum-wide claims, source-free explanations, fine-tuning, and cloud-dependent tutoring.

## Provenance gate

The book itself says it is Government of Kenya property and is not for sale. The project owner has stated that private written permission covers this exact asset. The evidence must remain outside source control, but before any external demo the owner must record the permission holder, date, exact permitted actions, expiry (if any), and private evidence location in `CONTENT_PROVENANCE.md`.

Permitted actions must explicitly include: local storage, text extraction, chunking, indexing/embedding, on-device model prompting, adaptation into short explanations, and MVP demonstration. If any action is missing, the associated pipeline stage remains disabled.

## Architecture

```text
Permissioned PDF
  -> offline extraction + page-image/diagram review
  -> teacher review and CBC lesson mapping
  -> signed/versioned content pack
  -> Android Room FTS index (topic + source passage + page)
  -> retrieve the most relevant approved lesson
  -> deterministic source-backed explanation
  -> optional guarded local-model rewrite
  -> claim/source validator
  -> learner answer with lesson/page/source
```

The model is never a knowledge source. It can only rewrite retrieved evidence. If retrieval is weak, validation fails, memory is low, or model execution fails, the app shows the deterministic explanation and its source.

## Content-pack contract

Each approved lesson chunk must contain:

```kotlin
data class TextbookLesson(
    val id: String,
    val subject: String,
    val grade: Int,
    val term: Int,
    val topic: String,
    val week: Int?,
    val lesson: Int?,
    val pageStart: Int,
    val pageEnd: Int,
    val sourceTitle: String,
    val sourceVersion: String,
    val passage: String,
    val teacherApprovedExplanation: String,
    val approvedKeywords: Set<String>,
    val diagramsNeedReview: Boolean,
)
```

Chunks follow lesson/activity boundaries, not arbitrary character counts. Pages containing diagrams, tables, or OCR uncertainty require teacher review before publication. Content packs must retain the page range and document version so a learner can see what supports an answer.

## Retrieval policy

1. Limit search to Grade 3 Mathematics and the active content-pack version.
2. Search local Room FTS using bounded, sanitised learner terms.
3. Return no answer when no lesson is supported; do not select the closest unrelated lesson.
4. For answer checks, use only a teacher-approved verified answer tied to the retrieved lesson.
5. Attach the lesson, page range, source title, and content-pack version to every response.

The existing FTS repository and deterministic tutor are the baseline. Semantic embeddings and reranking are later optimisation work, only after retrieval tests show an FTS recall problem.

## Explanation-provider policy

| Provider | MVP status | Rationale |
|---|---|---|
| Deterministic source-backed tutor | Active baseline | Reliable, offline, provenance-labelled. |
| Qwen2.5-0.5B-Instruct | Not runnable in current LiteRT stack | Current `.tflite` artefact is rejected by LiteRT-LM and recorded peak RAM is unsuitable for the low-end-device target. |
| Gemma 3 270M | Benchmark only | Faster but failed factual grounding/answer-checking (10/15 strict passes). |
| Qwen3 0.6B INT4 no-think | Benchmark only | Best factual result but fails the word-limit/resilience gate and has unproven Android performance. |

No model is enabled in the shipping path until it passes the documented 15-prompt baseline plus 25 adversarial cases, emits valid structured output, remains under 55 words, preserves verified answers, and is measured on a target Android device.

## Guarded model contract

The model input is strictly bounded:

```text
SYSTEM: Explain only supplied textbook evidence in simple Grade 3 English.
SOURCE_ID: <lesson-id>
SOURCE_PAGES: <start>-<end>
EVIDENCE: <retrieved passage>
APPROVED_ANSWER: <optional verified answer>
QUESTION: <bounded learner question>
```

Expected output is structured and validated before display:

```json
{
  "explanation": "55 words or fewer",
  "source_id": "exact supplied lesson id",
  "uses_only_source": true
}
```

Reject output if the source ID differs, length exceeds the limit, answer-check values disagree, it contains unsupported citations, or it is empty. Rejection returns the deterministic response, never a retry loop or an error screen.

## Delivery phases

### Phase 1 — controlled textbook-pack intake

1. Record private-permission metadata without committing the legal document.
2. Extract Term One text into a private staging area; retain page references and source hash.
3. Have a teacher select the highest-use concept lessons, correct extraction defects, and flag diagrams.
4. Keep exact-answer rules limited to reviewed cards; use the wider source index
   for retrieval and explanation until each rule is authored and evaluated.

### Phase 2 — Android evidence path

1. Replace the original demo-content source labels only after approved textbook lessons exist.
2. Extend the content and Room entities with term/week/page metadata.
3. Display a source card in the tutor conversation.
4. Add retrieval regression tests: supported query, unsupported query, wrong answer, punctuation-only query, and source metadata.

### Phase 3 — guarded model experiment

1. Keep the provider interface separate from the active tutor response engine.
2. Re-run Gemma and Qwen3 on the approved evaluation pack, plus 25 adversarial cases.
3. Add output validator and automatic fallback before any UI integration.
4. Test cold/warm latency, RAM, battery impact, and failure recovery on a target device.

### Phase 4 — release evidence

1. Record the model, licence, quantisation, runtime, device, test prompts, and results.
2. Run Android unit tests and debug build.
3. Verify no source PDF, permission record, learner data, or model credentials enter the public repository unless explicitly permitted.

## Acceptance criteria

- Every visible answer names a source lesson, page range, and version.
- Unsupported questions receive a clear boundary response.
- No model output can replace a verified answer or cite a nonexistent source.
- Retrieval-only tutoring works with model disabled or device low-memory mode enabled.
- All published textbook chunks have teacher review and provenance metadata.
- A model is enabled only with recorded quality, latency, device, and licence evidence.

## Decision log

| Decision | Alternatives | Decision and reason |
|---|---|---|
| Knowledge source | General LLM/web, raw curriculum/textbook corpus | Use one permissioned Grade 3 Mathematics book; narrow source boundary is testable. |
| Retrieval | Full PDF in prompt, model fine-tuning | Lesson-aware local retrieval; supports citations and updates. |
| Model role | Model as teacher/knowledge source | Model as optional source-bounded rewriter; deterministic tutor remains authoritative. |
| Initial model | Gemma, SmolLM2, Qwen2.5, Qwen3 | No active model until evaluation passes; Qwen3 and Gemma remain benchmarks. |
| Platform | Cloud-first, Android offline-first | Android offline-first because learning must not depend on connectivity. |
