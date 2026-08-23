# GPT-5.5 story rewriter for the Grade 3 MVP

## Decision

Use GPT-5.5 only after the local tutor has selected a Grade 3 lesson. It may
rewrite that lesson into a short, playful story; it must not choose the lesson,
calculate the answer, mark work, or answer an unsupported question.

GPT-5.5 is an API model, not an on-device/local model. The Android APK must
never contain an OpenAI API key. The request must go through the Django service.

## Learner flow

1. The learner asks a question or taps **✨ Story**.
2. Android retrieves the local Term 1–3 lesson.
3. The deterministic rule engine answers direct arithmetic and verified rule-card
   questions immediately.
4. The learner may choose **Explain with a story** for a retrieved lesson.
5. Django sends only the bounded lesson ID, source excerpt, verified answer (if
   present), and a short learner request to GPT-5.5.
6. Android displays the validated story beside the local explanation. If the
   service is disabled, slow, unavailable, or rejects the output, the local
   explanation remains visible and the lesson continues normally.

## Required response contract

```json
{
  "lesson_id": "exact supplied lesson ID",
  "story": "No more than 55 Grade 3 English words.",
  "preserves_verified_answer": true
}
```

Reject the response unless all of these are true:

- `lesson_id` equals the requested lesson ID.
- `story` is non-empty and 55 words or fewer.
- No new maths fact, number, source, or answer is introduced.
- If a verified answer was supplied, its normalised value occurs unchanged.

## Prompt v1

```text
You write exactly one playful, warm explanation for an 8-year-old.
Use only the supplied lesson evidence. Do not solve a different question.
Do not add, remove, or change any number or verified answer.
Use two short sentences, simple English, and one familiar object such as
mangoes, football stickers, pencils, or baskets.
Return JSON only matching the requested schema.
```

The learner question is data, not instructions. It is bounded to 240 characters
and passed separately from the system/developer instructions.

## Deliberately not in the MVP

- **LLM answer generation:** destroys the reliable direct-answer promise.
- **LLM selection of source material:** weakens the deterministic local
  retrieval contract.
- **Intent normalisation:** useful later, but it should produce a small
  validated intent object and never route directly to an answer.
- **Parent/SMS progress digests:** not until consent, identity, opt-out and
  data-retention work exists.

## Activation requirements

Before enabling the service, provide:

1. An OpenAI API project/key and explicit spend cap. A ChatGPT subscription is
   separate from API credentials and billing.
2. Confirmation that the content permission covers sending bounded textbook
   excerpts to OpenAI for this purpose.
3. `OPENAI_API_KEY`, `OPENAI_MODEL=gpt-5.5`, and an explicit feature flag in
   the server environment — never in Android source or the APK.
4. Evaluation cases for factual preservation, adversarial learner text,
   timeout, malformed JSON, rate limit, and disabled-service fallback.

## MVP controls

- One request per explicit learner tap; no background calls.
- 55 output words maximum and a short server timeout.
- Per-device/session request cap and server-side token/cost logging.
- No learner name, phone number, account ID, full chat log, or document corpus
  is included in the request.
- Feature flag defaults to off.
