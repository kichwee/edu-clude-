# EduCloud image prompts — complete Android UI set

These prompts are for **visual exploration**, not final UI implementation. Generate one screen at a time, then rebuild the approved layout as editable Figma components. Do not ask an image model to generate dense text, exact icons, or a whole application in one image.

## Use this style directive with every prompt

```text
Create one original, high-fidelity Android app screen for EduCloud, a multi-subject Kenyan primary-school learning app for shared, low-end devices. Frame: portrait 360 × 800 mobile screen, one-column layout, generous 16–24 dp margins, 48 dp minimum touch targets, strong information hierarchy, large readable labels, calm whitespace, no clutter.

Visual language: warm cloud cream background, deep navy text, sunflower yellow highlights, sunset-orange primary actions, lake-teal secondary accents, tiny hibiscus-coral details. Rounded Material 3-inspired surfaces, but with an original EduCloud identity: quiet Kenyan textile-inspired geometric accents only in decorative margins, soft clay-and-paper-cut illustrations, original diverse East African child learner guides. Use Noto Sans-like rounded, legible typography.

Learning language: encouraging and grounded, no public leaderboard, no hearts, no lives, no gems, no streak-pressure, no forced account. Never imitate or show Duolingo, Subway Surfers, or another company’s logo, mascot, assets, wording, screen layout, or palette. Do not create generic AI dashboards, glowing robots, fake chatbots, or excessive gradients. Use only short, legible English labels. No watermark.
```

Append exactly one screen prompt below after the style directive.

## A — setup and trust

| ID | Screen prompt |
|---|---|
| A01 | “Splash / local content loading. Centre the EduCloud wordmark, a small illustrated lantern made of learning cards, and the quiet status ‘Preparing your lessons’. Show a clear retry link and a small ‘Works offline’ badge; no spinner-only empty screen.” |
| A02 | “Welcome. Hero: original child learner-guide holding a notebook beside a warm sunrise path. Headline ‘Learn in small steps.’ Supporting copy ‘Lessons stay ready, even when the internet does not.’ One primary button: ‘Start learning’.” |
| A03 | “Learner name. Friendly, minimal alias form with headline ‘What should we call you?’, one large text field, a privacy note ‘Saved on this device’, and ‘Continue’ button. Keep child guide small and supportive.” |
| A04 | “Grade selection. Six large grade chips in a calm grid, Grade 3 selected with a clear tick and subject-colour ring. Explain ‘Choose the lessons that fit you’. Bottom action ‘Continue’.” |
| A05 | “Shared-device learner picker. Three local learner profile cards with illustrated avatars, alias, grade, and last lesson; include ‘Add learner’ and a subtle ‘This phone can be shared’ explanation.” |
| A06 | “Guardian consent. Clearly separate guardian-only setup from child learning. Use a lock illustration, plain language data choices, two simple consent rows, ‘Not now’ and ‘Continue as guardian’ actions.” |
| A07 | “Permission explainer. Explain one optional permission in child-safe, plain language before a system prompt. Show ‘Why we ask’, ‘You can choose later’, and an outlined ‘Not now’ action.” |
| A08 | “Offline pack update. Show an approved content-pack card with grade, subject count, size, version, Wi-Fi recommendation, and buttons ‘Download on Wi-Fi’ and ‘Later’. Make offline availability obvious.” |

## B — home and daily direction

| ID | Screen prompt |
|---|---|
| B01 | “Today home. Greeting ‘Good afternoon, Amina’, small offline-ready badge, one strong ‘Continue: Equal shares’ card with visual progress trail, two calm secondary cards ‘Review 1 lesson’ and ‘Explore subjects’, and bottom navigation Today, Learn, Progress, Profile.” |
| B02 | “Daily plan. A vertically ordered, low-pressure list: ‘One quick review’, ‘Continue equal shares’, ‘Try English reading’. Include short duration labels and a ‘Do this later’ affordance, never a punitive missed-goal message.” |
| B03 | “Notifications inbox. In-app cards for a review reminder, downloaded pack update, and guardian-approved note. Each card has an icon, date, short summary, and a clear unread state.” |
| B04 | “Search. Large friendly search field ‘Find a lesson’, recent searches, filters for subject and completed state, and results with subject accent, time, grade, and offline badge.” |

## C — content discovery

| ID | Screen prompt |
|---|---|
| C01 | “Subject library. A vivid but orderly two-column card grid: Mathematics, English, Kiswahili, Science & Technology, Social Studies, Creative Arts. Every card has an original mini illustration, progress line, subject name, and availability badge.” |
| C02 | “Grade 3 curriculum map. One selected subject header and five simple strand cards along a calm learning trail. Show one recommended starting strand and avoid a game-map overload.” |
| C03 | “Topic library. Topic cards for Mathematics: Equal shares, Add within 100, Groups and multiplication. Each card includes progress, expected time, lesson count, and download status.” |
| C04 | “Topic detail. ‘Equal shares’ header with simple fruit-sharing illustration, objective ‘Share equally’, a short lesson list, one completed marker, one next lesson marker, and ‘Start next lesson’.” |
| C05 | “Lesson preview. Show title, objective, three short learning steps, 5-minute estimate, source-pack/version card, materials ‘None needed’, and prominent ‘Start lesson’.” |
| C06 | “Saved lessons. Show downloaded lessons as compact cards with subject accent, size, content version, last opened date, storage meter, and a guardian-safe ‘Manage downloads’ link.” |

## D — teaching loop

| ID | Screen prompt |
|---|---|
| D01 | “Maths lesson player: Equal shares. A top progress stepper, large tactile illustration of 8 mangoes being shared between 2 baskets, one sentence explanation, and primary action ‘Try it’.” |
| D02 | “Guided practice. Present one clear task: ‘Put 8 mangoes into 2 equal groups.’ Use large draggable-looking mango tokens and two baskets, an optional ‘Show a hint’, and no timer.” |
| D03 | “Tutor question. A lesson-grounded question screen, not a generic chatbot. Show learner question ‘What does equal mean?’, one concise answer card, an illustration, source badge, and ‘Ask about this lesson’ input.” |
| D04 | “Source proof bottom sheet. Overlay a tidy sheet headed ‘Why this answer?’, listing Grade 3 Mathematics, Equal shares, original EduCloud demo content, version 1.0, a short quoted lesson fact, and close handle.” |
| D05 | “Low-memory mode. A reassuring sheet with a small lantern icon: ‘Your lesson is still ready.’ Explain that EduCloud is using the local lesson pack, show available actions, and one ‘Continue learning’ button.” |
| D06 | “Cloud comparison. A strictly optional comparison with two equal cards: ‘Local lesson answer’ and ‘Cloud-enhanced explanation’. Show source labels and a prominent offline fallback. Do not imply cloud is required.” |
| D07 | “Check for understanding. Single question ‘If 8 mangoes are shared equally between 2 children, how many does each child get?’ Four huge answer cards, clear progress ‘1 of 1’, and no distracting decoration.” |
| D08 | “Hint sheet. Progressive hint design: first ‘Try sharing one at a time’, then optional worked answer. Use a visual reveal and text ‘Using a hint helps you learn’.” |
| D09 | “Lesson complete. Warm, restrained celebration: a journey stamp appears on a paper-like card, ‘You learned how to share equally’, one next review card, buttons ‘Review later’ and ‘Continue’.” |

## E — assessment and revision

| ID | Screen prompt |
|---|---|
| E01 | “Quick check intro. State ‘3 short practice questions’, explain ‘This helps choose your next lesson’, show time estimate, and primary ‘Begin’.” |
| E02 | “Quiz question. One large illustrated Maths question, answer cards A–D, simple progress bar, back button, and accessible option contrast.” |
| E03 | “Answer feedback. Correct state with a small journey-stamp animation moment, green plus icon, explanation in plain words, and ‘Next question’. Include an equally kind incorrect state as a small inset.” |
| E04 | “Quiz result. Clear score summary, ‘You are practising equal shares’, strengths and one recommended next step. Use a small, warm illustration rather than a trophy explosion.” |
| E05 | “Review queue. Show due review cards with ‘Why now?’ labels: Today, in 3 days, and later. One strong ‘Start a 3-minute review’ action.” |
| E06 | “Revision session. Compact mixed review: visual recall question, one multiple choice choice, progress 1 of 4, source pack badge, and finish action.” |

## F — progress and encouragement

| ID | Screen prompt |
|---|---|
| F01 | “Progress overview. Child-friendly overview with subject progress rings, lessons completed, review due, and learning time this week. Avoid ability scores, ranks, or competitive metrics.” |
| F02 | “Subject progress. Mathematics topic list grouped as ‘Started’, ‘Practising’, ‘Confident’. Use colour plus labels and icons, never colour alone.” |
| F03 | “Learning history. Chronological local history of lessons and quick checks with date, subject, learning activity, and ‘Open again’ affordance.” |
| F04 | “Journey stamps. A calm collection wall of earned stamps for effort and completed topics. Include a gentle message ‘Every small step counts’, no broken-streak language.” |

## G — profile, accessibility, and care

| ID | Screen prompt |
|---|---|
| G01 | “Learner profile. Local alias, Grade 3, selected avatar, completed lessons, and a button to change learner. Keep it playful but uncluttered.” |
| G02 | “App settings. Clear grouped rows for text size, sound, reduce motion, colour/contrast, language availability, and offline preferences.” |
| G03 | “Content and storage. Installed content packs with grade, subjects, provenance/review marker, content version, storage used, and update availability.” |
| G04 | “Help and support. Large help topics: ‘Using lessons offline’, ‘Sharing this device’, ‘Changing text size’, ‘Contact support’. Include no scary technical language.” |
| G05 | “About, privacy and licences. Calm informational page with app version, privacy summary, content provenance, open-source licences, and known limits.” |
| G06 | “Guardian gate. PIN entry with lock illustration and wording ‘This area is for a parent or guardian’. Include ‘Back to learning’.” |
| G07 | “Guardian controls. Manage learner profiles, consent choices, cloud option, local data export/deletion request, and notification preferences. Use obvious protected-area styling.” |
| G08 | “Sync recovery. Explain one conflict in plain language: ‘This phone and your account have different progress.’ Offer safe choices: keep this phone, use account progress, or get help.” |

## H — companion and professional surfaces

| ID | Screen prompt |
|---|---|
| H01 | “Feature-phone simulator. Clearly label ‘Demo only — no message is sent’. Use two large tabs USSD and SMS, a simple keypad/message interface, and a safety explanation.” |
| H02 | “Channel status. Guardian-only screen showing simulator active, sandbox/testing status, consent requirement, and clearly disabled live messaging until approved.” |
| H03 | “Teacher mode entry. Secure role entry with teacher illustration, school sign-in explanation, and separate learner/teacher paths.” |
| H04 | “Teacher class overview. Authorised teacher dashboard with class-level topic progress, reviews needing support, and an unobtrusive privacy notice. No child data overload.” |
| H05 | “Teacher learner support. An authorised view of one learner’s evidence: recent lesson, practice response, suggested approved resource, and an audit/access note.” |
| H06 | “Teacher lesson helper. Teacher-reviewed lesson-plan draft workspace with Grade, Subject, Strand selectors, structured lesson plan sections, evidence/source panel, and ‘Review before use’ badge.” |

## S — system states

| ID | Screen prompt |
|---|---|
| S01 | “First load / skeleton. Skeleton cards for Today screen with a small lantern loading illustration and text ‘Getting your local lessons ready’.” |
| S02 | “Empty state. Empty saved-lessons view with a small backpack illustration, text ‘No saved lessons yet’, and ‘Explore subjects’ action.” |
| S03 | “Offline state. Connectivity banner with Wi-Fi-off icon: ‘You are offline. Your downloaded lessons are still ready.’ Keep content usable underneath.” |
| S04 | “Error and retry. Friendly recoverable error with a folded-map illustration, clear reason, preserved learner work note, ‘Try again’ and ‘Go back’ actions.” |
| S05 | “Content unavailable. Explain that a lesson is not yet downloaded, show pack size/version and guardian-safe ‘Download on Wi-Fi’ action.” |
| S06 | “Cloud unavailable fallback. Optional cloud card is unavailable; local lesson answer remains fully shown. Copy: ‘The extra explanation could not load. Continue with your offline lesson.’” |
| S07 | “Consent required. Protected action stop state with lock icon, no pressure on the child, wording ‘A parent or guardian can choose this later’, and ‘Back to learning’.” |
| S08 | “Update required. Content update sheet with why it matters, download size, Wi-Fi recommendation, time estimate, ‘Update now’ and ‘Keep using current lessons’.” |
| S09 | “Maintenance notice. Lightweight service banner explaining that online extras are temporarily unavailable while local lessons continue normally.” |
| S10 | “Accessibility preview. Compare normal and increased text size, high contrast, and reduced-motion settings in a readable interactive preview with ‘Apply changes’.” |

## Asset prompts for illustrations

Use these independently when building Figma assets.

```text
Original East African Grade 3 learner guide holding a notebook and a small paper lantern made from learning cards; warm, confident expression; soft 3D clay-and-paper-cut hybrid; sunset orange, sunflower yellow, lake teal, deep navy details; no text, logo, watermark, branded character, game aesthetic, robot, or busy backdrop.
```

```text
Educational illustration of 8 mangoes being shared equally into 2 simple woven baskets; clear visual grouping, child-friendly, tactile paper-cut and clay hybrid, warm cream background, no text, no logo, no watermark.
```
