# On-device model baseline evaluation

**Status:** failed quality gate — the model route is disabled in the Android tutor.

## Method

On 17 July 2026, the pinned `SmolLM2_360M_instruct.litertlm` artifact was run on the local workstation with LiteRT-LM CLI `0.14.0`, CPU backend, disk cache, `top-k=10`, `top-p=0.9`, `temperature=0.2`, six CPU threads, and a 512-token cache. Each of 15 original, independent prompts supplied only a Grade 3 Maths lesson fact, a deterministic verified answer, and a learner question. The runner was restarted for every case to avoid cross-prompt context leakage.

The pass condition was strict: a short answer had to preserve the verified arithmetic result, remain within the supplied fact, and not answer an unrelated question.

| Result | Cases | Evidence |
| --- | --- | --- |
| Pass | E02, E04, E08, E09, E10, E15 | Correct, source-bounded equal-shares or equal-groups explanations. |
| Fail: incorrect or internally contradictory arithmetic | E03, E05, E07, E11, E12, E13 | It said each child gets 1 mango, calculated `47 + 35` as 92, mishandled ones, or accepted incorrect answers (7, 83, and 11). |
| Fail: unsupported or off-topic content | E01, E06, E14 | It expanded the definition beyond the supplied fact, invented an invalid carrying explanation, and gave a dinosaur answer to an unrelated question. |

**Score:** 6/15 strict passes (40%). This does not meet the prompt contract and is not a safe explanation layer.

## Latency

The independent CPU runs took 8.903–23.270 seconds each (mean 14.656 seconds; median 12.846 seconds). These are workstation measurements only, not Android performance data.

## Decision

The Android app uses its deterministic, provenance-labelled retrieval response path. To avoid shipping a disabled native runtime, the active MVP no longer packages LiteRT-LM; the former runner is preserved under `android-app/experimental/on-device-model/`. The model pack installer is also retained solely for a future experiment. Neither may return to the active build unless a replacement baseline passes a documented evaluation and an Android-device test.

## Gemma 3 270M candidate evaluation

**Status:** failed quality gate — not enabled or packaged in the Android app.

On 17 July 2026, `litert-community/gemma-3-270m-it` (`gemma3-270m-it-q8.litertlm`) was downloaded after the account holder accepted the gated repository terms. It was evaluated using the same 15 independent prompts and CLI settings as the SmolLM2 test: LiteRT-LM `0.14.0`, CPU, disk cache, `top-k=10`, `top-p=0.9`, `temperature=0.2`, six CPU threads, and a 512-token cache.

| Result | Cases | Evidence |
| --- | --- | --- |
| Pass | E04, E05, E07–E10, E12–E15 | Correct or source-bounded explanations, including refusing to discuss the unrelated dinosaur question. |
| Fail: incorrect or unsupported explanation | E01, E02, E03, E06 | It incorrectly defined equal/half or invented an unsupported explanation of carrying a ten. |
| Fail: incorrect arithmetic validation | E11 | It said that 7 mangoes for each child was correct, despite the verified answer of 6. |

**Score:** 10/15 strict passes (67%). The candidate is faster and improves on SmolLM2's 6/15 result, but still fails the non-negotiable arithmetic and grounding requirement.

Warm-cache workstation latency was 1.740–2.955 seconds per independent case (mean 2.279 seconds; median 2.259 seconds). The initial download/load took 136 seconds. These are not Android measurements and do not demonstrate suitability for low-end devices.

## Qwen2.5 0.5B candidate load test

**Status:** incompatible with the installed LiteRT-LM runtime — no prompt evaluation was possible.

On 17 July 2026, the `Qwen2.5-0.5B-Instruct_seq128_q8_ekv1280.tflite` artifact from `litert-community/Qwen2.5-0.5B-Instruct` was downloaded (489.4 MiB transferred). LiteRT-LM CLI `0.14.0` rejected it during engine creation with `INVALID_ARGUMENT: Unsupported file format` and `Invalid magic number ... TFL3`.

This repository supplies `.tflite` and `.task` files rather than a compatible `.litertlm` artifact for the installed CLI. The model card also reports a 521 MB dynamic-int8 model and 1,363 MB peak RAM on a Samsung S24 Ultra. It is therefore neither runnable in the current experiment stack nor credible for EduCloud's low-end Android MVP.

## Qwen3 0.6B INT4 no-think candidate evaluation

**Status:** best factual result so far, but still not approved or packaged for the MVP.

On 18 July 2026, `litert-community/Qwen3-0.6B-int4` was evaluated using `qwen3_0.6b_nothink_q4_block32_ekv1280.litertlm`. The 331.2 MiB artifact was run through LiteRT-LM CLI `0.14.0`, CPU, disk cache, `top-k=10`, `top-p=0.9`, `temperature=0.2`, six CPU threads, and a 512-token cache.

| Check | Result |
| --- | --- |
| Arithmetic and answer-checking | The 15 cases retained their verified answers, including correct rejection of 7 mangoes, 83, and 11. |
| Grounding and off-topic behaviour | The outputs stayed on the supplied lesson facts; the dinosaur prompt returned lesson content rather than unrelated dinosaur information. |
| Prompt contract | **Failed.** Several responses exceeded the required 55-word limit and repeated prompt metadata rather than providing concise Grade 3 language. |
| Runtime resilience | **Failed.** The E13 command hit a Windows `cp1252` Unicode-output exception after producing its correct response. |
| Latency | Cached independent runs were 6.366–13.849 seconds (mean 8.890 seconds; median 8.728 seconds). The initial download/load response took 541 seconds. These are workstation, not Android, measurements. |

This result does not justify enabling Qwen3. It is a candidate only for a later, higher-capability optional model pack after: (1) deterministic answer/output validation, (2) a hard response-length limiter, (3) Android-device performance testing, and (4) a larger adversarial evaluation. The 25-prompt adversarial extension was not run because the 15-case prompt contract did not fully pass.
