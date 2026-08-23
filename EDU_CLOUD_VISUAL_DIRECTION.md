# EduCloud Visual Direction and Figma Handoff

## Original visual direction

The supplied `subway` references are used as **mood references**: a confident Kenyan child traveller, warm sunset/yellow light, hand-painted geometric patterns, bold destination labels, and a welcoming three-dimensional world. The EduCloud UI should translate those qualities into a school-safe learning space, not recreate the Subway Surfers game or its assets.

We also borrow only general learning-product patterns observed in current gamified education UI: a clear next activity, a visible learning path, immediate feedback, rounded controls, and small celebration moments. We do not copy Duolingo artwork, owl, logo, green palette, wording, screens, or branded visual assets.

## Proposed system

| Element | Direction |
|---|---|
| Brand mood | “Learning journeys across Kenya”: energetic, local, hopeful, calm enough for sustained reading. |
| Palette | Sunflower `#F6C344`, sunset orange `#F28C28`, lake teal `#1B9AAA`, deep navy `#16324F`, hibiscus coral `#EB5E55`, warm cloud `#FFF8ED`. |
| Subject accents | Maths blue; English coral; Kiswahili green; Science teal; Social Studies amber; Creative Arts purple; all paired with text/icon labels. |
| Characters | Original EduCloud learner guides: friendly East African children in age-appropriate, modern outfits. Use diverse skin tones and no character as a copy of the supplied references. |
| Illustration | Soft 3D clay/paper-cut hybrid, rounded geometry, subtle Kenyan textile-inspired border patterns, no noisy backgrounds behind instructional text. |
| Type | A rounded but highly legible sans serif for headings, paired with a sober Android-readable body face. Minimum 16 sp body text; large answers and 48 dp hit targets. |
| Motion | One quick progress/celebration cue after success; reduced-motion setting removes nonessential movement. |
| Rewards | “Journey stamps” and progress lanterns, not hearts/lives, gems, public leagues, or loss mechanics. |

## Screen-family boards to generate

Generate polished visual boards first; each board has six representative phone screens. These are the visual specification for all screens in the same family, then designers rebuild the individual screens as editable Figma components.

1. **Foundation & onboarding:** splash, welcome, alias, grade, profile picker, guardian gate.
2. **Home & subject discovery:** Today, subject library, curriculum map, topic library, topic detail, search.
3. **Lesson journey:** lesson preview, lesson player, worked example, tutor/source proof, low-memory mode, cloud fallback.
4. **Practice & assessment:** practice, hint sheet, quiz intro, question, answer feedback, result.
5. **Progress & revision:** review queue, revision session, progress overview, subject progress, learning history, journey stamps.
6. **Settings & support:** profile, accessibility, content/storage, support, privacy/licences, update state.
7. **Guardian & teacher (gated):** consent, guardian dashboard, notifications, channel status, teacher class overview, teacher lesson helper.
8. **System states:** loading, empty, offline, error/retry, content unavailable, consent-required.

## Figma file structure

Create one Figma file named **EduCloud Android Design System**:

```
00 Cover & principles
01 Foundations (colour, typography, spacing, elevation, icon rules)
02 Components (buttons, cards, progress, feedback, nav, sheets, input)
03 Patterns (offline, loading, empty, error, consent)
04 Learner flow — onboarding
05 Learner flow — home & subjects
06 Learner flow — lesson & practice
07 Learner flow — progress & profile
08 Guardian & teacher — gated
09 Prototype — MVP click path
10 Content notes & accessibility QA
```

Build every component with Android touch targets and variants (`default`, `pressed`, `disabled`, `loading`, `offline`, `error`). Build each subject card as one component with a subject accent property, not a separate hand-drawn card. Keep source/provenance and offline badges as mandatory reusable components.

## First Figma prototype

The first clickable prototype should demonstrate this single complete story:

`Welcome → alias → Grade 3 → Today → Mathematics → equal shares lesson → ask tutor → source proof → quick check → correct feedback → result → next review`

The second prototype adds a second subject (for example, English reading) to prove the system is truly multi-subject before scaling content.
