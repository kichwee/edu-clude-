# Stitch-to-Android screen map

The Android app now exposes a native Compose destination for all **39** local Stitch exports from the home dashboard: **Explore the learning space**. The screen library is intentionally reachable in-app so the designs are not dead files.

| Stitch export family | Android destination |
|---|---|
| Splash Screen (including 10 prototype variants) | Shared Splash visual state |
| Welcome (including 8 prototype variants) | Shared Welcome visual state |
| Name Setup, Choose Grade, Pick a Learner | Local-only onboarding states |
| Today Dashboard, Learning Journey, Learning Path | Today, plan, and Maths journey states |
| Explore Subjects, Downloads, Library Story | Subject library, saved local lessons, and Story Time direction |
| Equal Shares Intro, Equal Concept, Lesson Activity, Great Job, Lesson Complete | Lesson preview, concept reinforcement, activity, feedback, and completion states |
| Your Progress, Achievements, Streak Tracker | Local progress, badges, and streak states |
| For Guardians | Guardian safety and privacy information state |

## Scope truthfulness

The visual exports contain subjects, guardian analytics, downloads, and stories beyond the locked MVP. Their Android screens remain visibly informational or disabled unless supported by the current Grade 3 Maths local pack. This prevents the UI from claiming that cloud accounts, live downloads, SMS/USSD, Science, English, or guardian reporting exist when they do not.

The repeated prototype exports are reconciled to their shared Splash or Welcome state rather than copied as 18 separate, indistinguishable app flows. The exported HTML remains source reference material in `stitch_screens/`; it is not embedded in the Android app.
