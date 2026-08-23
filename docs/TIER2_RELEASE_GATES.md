# Tier-2 demo architecture and release gates

## Implemented locally

- `content/grade3_rule_tutor_v1.json` is the authored Grade 3 source for both
  surfaces. Run `tools/generate_grade3_rule_tutor.py` after editing it; do not
  hand-edit `GeneratedGrade3RuleTutor.kt`. CI rejects drift.
- USSD displays a public lesson locator such as `G3M11`. The Android feature-
  phone simulator can open the matching lesson. The locator carries no learner
  identity, session, score, or phone number.
- Each demo lesson uses a two-step, stateless retry check. It says “you
  improved” only when the first answer is wrong and the second is correct; it
  does not create a learner profile, mastery score, or analytics event.
- The demo remains deterministic and stateless. It is intentionally not an
  adaptive-learning, learner-account, analytics, or parent-report system.

## Required before a real pilot

- Replace temporary tunnels with a durable HTTPS deployment and secret manager.
- Implement provider-documented request verification, replay/idempotency rules,
  shared rate limiting, monitoring, and incident logging.
- Establish guardian consent, shared-phone profile selection, data retention,
  deletion, and opt-out workflows before storing any learner or phone data.
- Add a teacher/content workflow, written content permissions where needed, and
  a reviewed versioned content pack before describing lessons as curriculum
  aligned or school-ready.
- Prove Android performance on representative low-RAM devices and add provider
  sandbox end-to-end coverage. The local test workflow is already checked in.

## Explicitly deferred

Cloud AI tutoring, parent reports, SMS dispatch, voice/IVR, school hubs,
cross-device learner sync, adaptive mastery, and production analytics. None is
implemented or claimable in this MVP.
