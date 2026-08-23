# EduCloud Content Provenance Register

## Monday hackathon demo

| Content ID | Description | Source / owner | Permitted use | Review status | Notes |
|---|---|---|---|---|---|
| `demo-g3-math-0.1` | Three short Grade 3 Mathematics teaching passages and three check questions | Original EduCloud demonstration material | Hackathon demonstration only | Automatic arithmetic and schema checks | Stored in the Android content layer and the sandbox-only backend USSD tree (`edu-cloud/backend/educloud/ussd_learning.py`). Not represented as KEC/KICD curriculum content. |
| `g3-rule-tutor-v1` | Four Grade 3 Mathematics rule cards used by Android and the USSD simulator | Grade 3 Mathematics pupil's book supplied privately by the project owner; permission evidence is held privately by the owner and is not stored in this repository | Owner-authorised MVP use | Review record not supplied | `content/grade3_rule_tutor_v1.json` is the one canonical pack. It generates Android's offline cards and is loaded by the backend USSD adapter. The learner experience shows the lesson source, not an internal status label. |
| `agentic-remediation-demo-v1` | On-demand micro-lessons and three subtraction practice questions compiled into local patches | Original AI-assisted demo content generated only from bounded, anonymous arithmetic-error patterns | Local hackathon demonstration only; never a curriculum claim | Automatic schema, scope, provenance-label, and arithmetic validation | Generated packs are stored only in the local demo database, include this limitation in their provenance field, and must not quote or transform KEC/KICD material. |

**21 July 2026 audit action:** the 142 PDF/OCR records are retained for provenance but excluded from Android compilation and learner retrieval. See [docs/GRADE3_CONTENT_AUDIT.md](docs/GRADE3_CONTENT_AUDIT.md). Only the four shared Grade 3 rule cards are learner-facing.

## KEC/KICD approval register — complete before importing material

| Approval ID | Asset/source URL | Rights holder | Scrape/store | Chunk/embed | Fine-tune | Public demo | Commercial/pilot | Attribution / restrictions | Evidence location |
|---|---|---|---|---|---|---|---|---|---|
| Pending |  |  |  |  |  |  |  |  |  |

No content may be labelled KEC, KICD-approved, or curriculum-complete until its row is complete and evidence has been reviewed by the project owner.
