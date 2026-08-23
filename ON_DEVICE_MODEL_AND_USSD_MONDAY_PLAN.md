# On-device model and USSD — Monday implementation checklist

**Purpose:** Deliver two demonstrable technical proofs without overstating what is production-ready:

1. An original EduCloud Grade 3 Maths tutor that can use the on-device **SmolLM2 360M Instruct LiteRT-LM** artifact to turn retrieved local lesson facts into short explanations.
2. A real **Africa's Talking Sandbox** USSD/SMS route, alongside the existing no-send simulator.

## Claim boundary

Until teacher-authored training data, a training run, held-out evaluation, and a converted replacement artifact exist, we will say **“on-device instruction model”**, not “EduCloud fine-tuned model.” The model is English-focused, Apache-2.0, and the LiteRT community artifact is Android-ready, but it must be validated in this app and on a target device. No model may calculate answers or introduce lesson facts; deterministic content/retrieval remains authoritative.

## Track A — on-device SmolLM2 baseline

| Order | Item | Done when |
| --- | --- | --- |
| A1 | [x] Pin the exact artifact revision and SHA-256 in `MODEL_PROVENANCE.md`. | A reproducible model source and licence record exist. |
| A2 | [x] Verify the artifact with LiteRT-LM CLI/Edge Gallery. | LiteRT-LM CLI `0.14.0` loaded the pinned file and returned a correct source-bounded baseline response on 17 July 2026. |
| A3 | [x] Add LiteRT-LM Android dependency and CPU-first `OnDeviceTutorEngine`. | Debug APK compiled with LiteRT-LM `0.14.0` and an isolated runner on 17 July 2026. |
| A4 | [x] Add an optional local model-pack path; never bundle an unverified large file into the APK. | The debug-only install helper copies a verified pack to app-private storage; a missing/failed pack falls back safely. |
| A5 | [x] Wire source-bounded prompting into the tutor view model. | Inference is off the UI thread, output is capped, and the retrieved source is always shown. |
| A6 | [x] Add a visible status/state: loading, ready, unavailable, retrieval-only. | The tutor makes the active state explicit and never claims the model ran when it did not. |
| A7 | [ ] Evaluate 15 original prompts. | **17–18 July workstation results: not ready.** SmolLM2 produced 6/15 strict passes; Gemma 3 270M produced 10/15 but accepted an incorrect arithmetic answer; Qwen2.5 0.5B could not load in LiteRT-LM `0.14.0` and exceeds the low-end memory budget; Qwen3 0.6B retained verified answers but violated the 55-word/runtime contract. None is enabled. See `MODEL_EVALUATION.md`. Android device, latency, and memory evidence remain pending. |

### Prompt contract

The model receives only: lesson title, grade, retrieved fact, verified answer (if relevant), and the learner question. It is instructed to use simple English, never add a fact, and return at most 55 words. If the retrieval result is missing, the runner is not called.

## Track B — Africa's Talking Sandbox proof

| Order | Item | Done when |
| --- | --- | --- |
| B1 | Obtain sandbox username/key, test channel/service code, consent wording, and a public HTTPS callback approval. | Values are in local `.env`, never source control or chat. |
| B2 | [x] Add `TELEPHONY_MODE=simulator|sandbox`; keep simulator the default. | No outbound call can occur accidentally; sandbox mode requires a long callback capability. |
| B3 | [x] Reuse one pure USSD state machine for the simulator and sandbox callback. | Tests cover empty menu, correct/wrong/repeated answer, bad callback capability, and default-disabled route. |
| B4 | Add a provider-approved inbound SMS adapter if it is needed for the demo (`START`, `MATH`, `QUIZ`, `HELP`, `STOP`). | Phone numbers are neither persisted nor placed in logs. The current command parser is simulator-only and no outbound SMS exists. |
| B5 | Deploy/tunnel the callback over HTTPS and configure it in Africa's Talking Sandbox. | A real sandbox request reaches the Django endpoint. |
| B6 | Capture evidence. | Redacted dashboard/callback/session recording proves the live sandbox path; UI says “Sandbox”, not “live service”. |

## Critical path and stop rules

1. **Today:** A1–A3 and B1. If B1 is not available by the agreed cutoff, continue only with simulator evidence and do not claim provider integration.
2. **Saturday:** A4–A6 and B2–B4, then build/test.
3. **Sunday:** A7 and B5–B6; capture screenshots/video evidence.
4. **Monday:** device smoke test, full demo recording, public README with model and telephony limits.

Stop model integration and use retrieval-only if initialization blocks the UI, the app crashes, the artifact will not load, or the model produces a source-less/incorrect answer. Stop telephony work and retain simulator mode if provider credentials/callback approval are absent.

## Fine-tuning — post-baseline gate

Fine-tuning starts only after the baseline works. Required evidence: original teacher-authored data, a held-out prompt set, a reproducible LoRA run, licence record, evaluation against the baseline, conversion to a new LiteRT artifact, and the same Android validation. The upgraded claim is permitted only after all six are complete.

## Evidence to retain

- Model repository URL, immutable revision, checksum, licence, file size, device, latency, memory observations, and prompt-evaluation table.
- Redacted Sandbox configuration, HTTPS callback proof, USSD/SMS request/response fixtures, and passing test output.
- README wording that distinguishes simulator, sandbox, on-device baseline, and future fine-tuning.
