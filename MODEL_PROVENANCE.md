# On-device model provenance

## Pinned baseline — workstation-verified, not approved for learner responses

| Field | Value |
| --- | --- |
| Intended runtime | LiteRT-LM Android `0.14.0` |
| Intended artifact | `litert-community/SmolLM2-360M-Instruct` |
| Base model | `HuggingFaceTB/SmolLM2-360M-Instruct` |
| Intended role | Source-bounded, on-device English explanation only; not an arithmetic solver or an EduCloud fine-tune. |
| Licence to retain | Apache-2.0 |
| Immutable revision | `507c99cfe6541ba2bcd84818786f7b025935e5e1` |
| File | `SmolLM2_360M_instruct.litertlm` (local only; git-ignored) |
| SHA-256 | `8E2834DA211B439751AF968ED650FEBDDE5A8CB8D88BC6C1A3059F049CAA5C2E` |
| Verified file size | 373,719,040 bytes |
| Workstation validation | LiteRT-LM CLI `0.14.0`, CPU, 17 July 2026 — the artifact loads and produces responses. It failed the full 15-prompt source-bounded quality evaluation (6/15 strict passes); see [MODEL_EVALUATION.md](MODEL_EVALUATION.md). |
| Tested Android device/latency/memory | Pending; no device/emulator was connected at validation time. |

The model file must not be committed, silently downloaded, represented as teacher-reviewed, or enabled in learner responses. It is an on-device instruction-model baseline, not an EduCloud fine-tune. A replacement model route needs a passing prompt evaluation plus Android-device evidence before any positive learner-facing claim.
