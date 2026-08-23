# Shared Grade 3 Rule Tutor

## Outcome

Android chat, the Android USSD simulator, and backend USSD use one Grade 3
rule-content contract. Android remains offline: it compiles a generated Kotlin
copy of the pack and never calls Django for a lesson response.

## Current flow

```text
content/grade3_rule_tutor_v1.json
  ├─ backend Grade 3 numeric USSD adapter
  └─ tools/generate_grade3_rule_tutor.py
       └─ Android GeneratedGrade3RuleTutor.kt
            ├─ free-text deterministic tutor
            └─ USSD simulator
```

## How to add or change a lesson

1. Edit `content/grade3_rule_tutor_v1.json`; keep the card ID stable.
2. Supply all required fields: source, matching terms, teaching steps, hint,
   practice question, three numbered options, answer rules, and correction.
3. Run `edu-cloud/backend/.venv/Scripts/python.exe tools/generate_grade3_rule_tutor.py`.
4. Run backend tests from `edu-cloud/backend` with `DJANGO_SECRET_KEY` set.
5. Run Android unit tests from `android-app`.
6. Check Android chat and the USSD simulator show the same topic, source,
   question, options, answer, and content version.

## Rules

- The pack is the only authoring source for shared Grade 3 content.
- Android and USSD have separate adapters because free text and keypad menus
  are different interfaces; they must not invent different lesson facts.
- Numeric rules are limited to the pack's declared Grade 3 families.
- The Android feature-phone simulator is a simulator only. It does not send a
  message or collect a phone number.

## Removed redundant implementation

The former Grade 1–12 generic content pack, Android generated catalogue,
continuation-code flow, and old USSD menu branch were removed after the Grade 3
replacement and focused tests were added. Experimental model and telephony
modules are intentionally outside this change.

## Decision log

| Decision | Alternative | Reason |
|---|---|---|
| One JSON source, generated Kotlin | Android requests lessons from Django | Keeps the Android MVP offline and prevents duplicated authored facts. |
| Separate UI adapters | One identical chat/menu interface | Android and USSD have different input constraints. |
| Four shared cards | Keep the old Grade 1–12 generic tree | The MVP is Grade 3 Maths; the old tree contradicted that scope. |
