#!/usr/bin/env python3
"""Generate an unreviewed, private Grade 3 Maths lesson pack from staging text.

This mechanical generator preserves lesson/page boundaries and source excerpts.
It deliberately creates no answer keys or pedagogical claims: a source record is
not teacher approval. Generated output must remain private until permission for
packaging/distribution is documented.
"""

from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from pathlib import Path


TERM_STARTS = ((9, 1), (107, 2), (197, 3))
MARKER = re.compile(r"Week\s+(\d+)\s+Lesson\s+(\d+)", re.IGNORECASE)
HEADING = re.compile(
    r"Week\s+\d+\s+Lesson\s+\d+\s+([A-Z][A-Za-z0-9 ,\-]{2,90}?)(?=\s+(?:Activity|Example|Work to do|Steps|$))",
    re.IGNORECASE,
)
STOP_WORDS = {
    "activity", "answer", "book", "example", "first", "grade", "kenya", "lesson",
    "and", "book", "example", "first", "for", "from", "government", "grade", "have", "kenya", "lesson",
    "not", "property", "sale", "term", "the", "this", "what", "week", "with", "work", "your",
}


def term_for(page: int) -> int:
    if page >= 197:
        return 3
    if page >= 107:
        return 2
    return 1


def compact(text: str) -> str:
    return " ".join(text.replace("\u00ad", "").split())


def kotlin_string(value: str) -> str:
    return json.dumps(value, ensure_ascii=False)


def topic_for(text: str, week: int, lesson: int, term: int) -> str:
    match = HEADING.search(text)
    if match:
        title = compact(match.group(1)).strip(" -,:.")
        if len(title) >= 3:
            return title[:90]
    return f"Term {term} Week {week} Lesson {lesson}"


def keywords(topic: str, excerpt: str) -> list[str]:
    tokens = re.findall(r"[a-z0-9]+", f"{topic} {excerpt}".lower())
    # Four characters avoids accidental matches such as `out` inside an
    # unrelated question; FTS still retrieves exact source terms first.
    words = [token for token in tokens if len(token) >= 4 and token not in STOP_WORDS]
    counts = Counter(words)
    # Keep lexical evidence from the source itself. FTS narrows candidates first;
    # these words then choose the best matching local lesson deterministically.
    return [word for word, _count in counts.most_common(48)] or ["maths"]


def lesson_blocks(pages: list[dict]) -> list[dict]:
    starts: list[tuple[int, int, int, int]] = []
    for page in pages:
        page_number = int(page["pdf_page"])
        for marker in MARKER.finditer(page.get("text", "")):
            starts.append((page_number, marker.start(), int(marker.group(1)), int(marker.group(2))))
    starts.sort()
    blocks: list[dict] = []
    for index, (start, _offset, week, lesson) in enumerate(starts):
        term = term_for(start)
        next_start = starts[index + 1][0] if index + 1 < len(starts) and term_for(starts[index + 1][0]) == term else None
        term_end = {1: 106, 2: 196, 3: 300}[term]
        end = max(start, min(term_end, (next_start - 1) if next_start else term_end))
        relevant = [compact(page.get("text", "")) for page in pages if start <= int(page["pdf_page"]) <= end]
        excerpt = compact(" ".join(relevant))[:900]
        if not excerpt:
            continue
        blocks.append(
            {
                "term": term,
                "week": week,
                "lesson": lesson,
                "start": start,
                "end": end,
                "topic": topic_for(excerpt, week, lesson, term),
                "excerpt": excerpt,
            }
        )
    return blocks


def render(blocks: list[dict]) -> str:
    lines = [
        "package com.example.educloud.content",
        "",
        "/** Generated from the private staging manifest; every record is unreviewed source evidence. */",
        "object GeneratedGrade3MathLessons {",
        "    const val VERSION = \"grade3-full-unreviewed-0.2\"",
        "    private const val SOURCE = \"Grade 3 Mathematics Pupil's Book — private MVP source\"",
        "",
        "    val lessons = listOf(",
    ]
    for block in blocks:
        unit_id = f"g3-t{block['term']}-w{block['week']}-l{block['lesson']}-source"
        topic = block["topic"]
        key_list = ", ".join(kotlin_string(word) for word in keywords(topic, block["excerpt"]))
        lines.extend([
            "        ContentLesson(",
            f"            id = {kotlin_string(unit_id)},",
            f"            topic = {kotlin_string(topic)},",
            f"            keywords = setOf({key_list}),",
            "            source = SOURCE,",
            "            version = VERSION,",
            f"            term = {block['term']}, week = {block['week']}, lesson = {block['lesson']},",
            f"            sourcePageStart = {block['start']}, sourcePageEnd = {block['end']},",
            f"            bookPageStart = {block['start'] - 8}, bookPageEnd = {block['end'] - 8},",
            f"            passage = {kotlin_string(block['excerpt'])},",
            "            teachingSteps = listOf(\"Read the local source excerpt.\", \"Find the worked example or activity on the cited pages.\", \"Ask a question using the topic words.\"),",
            f"            definition = {kotlin_string('Unreviewed local source record for ' + topic + '.')},",
            "        ),",
        ])
    lines.extend(["    )", "}", ""])
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("staging", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    manifest = json.loads(args.staging.read_text(encoding="utf-8"))
    blocks = lesson_blocks(manifest["pages"])
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(render(blocks), encoding="utf-8")
    print(f"Generated {len(blocks)} unreviewed lesson records at {args.output}")


if __name__ == "__main__":
    main()
