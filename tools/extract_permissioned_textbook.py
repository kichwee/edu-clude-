#!/usr/bin/env python3
"""Create a private staging manifest from a permissioned textbook PDF.

This tool does not create an Android content pack. A teacher must select lesson
boundaries, correct extraction errors, approve the text/diagrams, and add CBC
metadata before anything is bundled in the app.
"""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
from typing import Any

from pypdf import PdfReader


def normalise(text: str) -> str:
    return " ".join(text.replace("\u00ad", "").split())


def extract(pdf_path: Path, output_path: Path, source_id: str, version: str) -> dict[str, Any]:
    digest = hashlib.sha256(pdf_path.read_bytes()).hexdigest()
    reader = PdfReader(str(pdf_path))
    pages: list[dict[str, Any]] = []

    for pdf_page, page in enumerate(reader.pages, start=1):
        text = normalise(page.extract_text() or "")
        pages.append(
            {
                "source_id": source_id,
                "source_version": version,
                "pdf_page": pdf_page,
                "text": text,
                "needs_review": not text or len(text) < 80,
                "review_reason": "Low or missing extractable text; inspect the page image."
                if not text or len(text) < 80
                else "Teacher must confirm lesson boundaries, diagrams, and accuracy.",
            }
        )

    manifest = {
        "source_id": source_id,
        "source_version": version,
        "source_sha256": digest,
        "page_count": len(reader.pages),
        "publication_state": "private_staging_only",
        "required_next_step": "Teacher-approved lesson chunking before Android packaging.",
        "pages": pages,
    }
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pdf", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--source-id", required=True)
    parser.add_argument("--version", required=True)
    args = parser.parse_args()

    manifest = extract(args.pdf, args.output, args.source_id, args.version)
    print(
        f"Wrote {manifest['page_count']} private staging pages to {args.output}. "
        "Do not package them before teacher review."
    )


if __name__ == "__main__":
    main()
