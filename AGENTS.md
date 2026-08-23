# Agent Instructions

## Skill Selection

- Before substantive work, inspect the available user skills and select the minimal relevant set for the task.
- Evaluate task complexity first: complete simple, contained edits directly; use specialised skills for complex, multi-domain, or explicitly skill-driven work.
- Read each selected skill's `SKILL.md` completely before acting, and follow its routing instructions.
- Announce skill use in progress updates and state material skill-driven decisions in the hand-off.
- Do not create skills unless the user specifically requests a new skill.

## Package Manager

- Android: Gradle wrapper in `android-app/`.
- Backend: Python virtual environment with `edu-cloud/backend/requirements-mvp.txt`.

## File-Scoped Commands

| Task | Command |
|---|---|
| Android unit tests | From `android-app`: set `JAVA_HOME` to Android Studio JBR, then `./gradlew.bat testDebugUnitTest` |
| Android debug package | From `android-app`: set `JAVA_HOME` to Android Studio JBR, then `./gradlew.bat assembleDebug --offline --no-daemon --console=plain` |
| Backend system check | From `edu-cloud/backend`: set `DJANGO_SECRET_KEY`, then `.\.venv\Scripts\python.exe manage.py check` |
| Backend tests | From `edu-cloud/backend`: set `DJANGO_SECRET_KEY`, then `.\.venv\Scripts\python.exe manage.py test educloud.tests -v 2` |

## Key Conventions

- Read `MVP_LAUNCH_CHECKLIST.md` and `IMPLEMENTATION_PLAN.md` before EduCloud feature work; update the checklist only with verified evidence.
- Keep cloud providers and real telephony disabled unless the required credentials, consent, safety controls, and explicit approval are supplied.
- Treat `edu-cloud/apps/mobile/` and unfinished backend modules as experimental; do not expose them in the MVP.
- Preserve original-content provenance in `CONTENT_PROVENANCE.md`.

## Commit Attribution

AI commits MUST include:

```
Co-Authored-By: Codex <noreply@openai.com>
```
