# Grade 3 Mathematics content audit — 21 July 2026

## Decision

The PDF/OCR export is **quarantined from the learner-facing Android build**.
It remains in local staging and source control only to preserve provenance and
support a future permissioned, teacher-reviewed rebuild. It is not a lesson
pack, question bank, or curriculum-complete catalogue.

## Findings

| Finding | Evidence | Action taken |
|---|---:|---|
| Raw generated records | 142 OCR-derived records | Excluded `GeneratedGrade3MathLessons.kt` from Kotlin compilation and removed it from `Grade3MathContent.lessons`. |
| Learner-ready records | 10 short original/curated Term One demo lessons | Kept as the only active Grade 3 tutor corpus. |
| OCR pages requiring review | 47 staging pages; 32 generated records touch one | No record from this set can reach the tutor. |
| Generic titles | 71 of 142 use fallback `Term / Week / Lesson` titles | Quarantined; no automatic title repair. |
| Duplicate source IDs | 3 collisions | Quarantined; no ambiguous record can enter the Room index. |
| Truncated source text | 35 records reach the old 900-character cutoff | Quarantined; no partial excerpt can be presented as a lesson. |

## Product behaviour after the fix

- The tutor indexes only the ten coherent Grade 3 demo lessons.
- Only Term One → Numbers and whole numbers can open the learner tutor.
- Other displayed units are visibly unavailable pending human review; they do
  not route learners to unrelated raw text.
- The original PDF/OCR material is not deleted or rewritten. Its provenance is
  retained in `CONTENT_PROVENANCE.md` and `private-content-staging/`.

## Required before restoring any PDF-derived lesson

1. Written permission scope that covers the intended APK/demo distribution.
2. A teacher-reviewed manifest with stable unique IDs, accurate term/unit/topic
   mapping, exact page ranges, and approved learner-facing wording.
3. A quality gate that rejects `needs_review` pages, generic titles, truncated
   blocks, and duplicate IDs.
4. Unit-filtered Android navigation and retrieval tests for every approved
   lesson.

Do not use OCR repair or automated paraphrasing to invent answers or make a
source record sound like a teacher-approved lesson.
