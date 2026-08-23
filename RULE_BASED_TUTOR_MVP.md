# Rule-based Grade 3 Maths tutor — MVP decision

## Verdict

For the authorised, bounded Grade 3 Mathematics MVP, a deterministic tutor is
the better primary system. It is offline, fast, auditable, and suitable for a
numeric USSD interaction. It is **not** a general chatbot and must not claim to
understand every wording or solve unrestricted word problems.

The previous systems were not one shared tutor:

- Android had a separate ten-card Term One representation plus keyword
  retrieval and fixed replies.
- The safe Django USSD simulator worked, but used a different Grade 1–12
  unreviewed demo pack.
- `apps/ussd/views.py` is experimental and calls RAG/Gemini. It is not part of
  the MVP route and must remain disabled for this deterministic path.

## Rule contract v1

The canonical USSD pack is `content/grade3_rule_tutor_v1.json`.

Each card has a stable ID, topic, source reference, short explanation, hint,
practice question, three options, verified correct option, and correction.
The Android and USSD MVPs use the same four initial skills:

1. Positions in order
2. Counting in twos
3. Tens and ones
4. Number words

Android additionally supports bounded parameterised rules only where the
source pack supports the skill: continue a three-number sequence changing by
`+/- 2` within `0..999`, and split a two-digit number into tens and ones. All
other questions get an explicit, safe fallback.

## Channel design

```text
Canonical lesson/rule contract
        ├─ Android: natural phrasing + intent rules + local lesson context
        └─ USSD: Topic → Explain / Hint / Practice → 1/2/3 → feedback
```

USSD has no free-text tutor because accumulated USSD input, short sessions, and
the message-length limit make it unreliable. Its numeric menu is the correct
equivalent of Android's rule path—not a weaker substitute.

The active simulator now starts with `1. Grade 3 Maths tutor`. Its rule path is
`1 → topic → Hint or Practice → 1/2/3 answer`. The prior Grade 1–12 generic
demo has been removed after replacement tests were added.

## Release gates

- Every answer has an active content version and source reference.
- A deterministic solver has a tested domain and never runs outside it.
- Unsupported wording or maths safely returns a next action, not a guess.
- Android and Django regression tests cover valid, invalid, correct, and wrong
  paths.
- Teacher review is still required before claiming curriculum approval or
  broad classroom readiness.

## Next expansion order

Add one reviewed rule family at a time: ordinal positions to twelfth, number
words in the approved range, addition/subtraction, equal groups, fractions,
then time and measurement. Each family needs source cards, a bounded solver,
wrong-answer feedback, and generated boundary tests before release.
