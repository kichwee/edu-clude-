"""
RAG Pipeline — Core retrieval engine for Education Cloud.
Implements: hybrid FTS5 + sqlite-vec search with Reciprocal Rank Fusion (RRF).

Research basis:
- Lewis et al. (2020) RAG paper (arXiv:2005.11401)
- RAGdb (2025): "Single-File Knowledge Container" — all in one SQLite file
- Multilingual RAG paper (ACL 2024): use multilingual-e5-small, retrieve in student's language
- PRD FR-01: multilingual-e5-small → sqlite-vec top-3 → Qwen2 generates answer
"""
import sqlite3
import sqlite_vec
import struct
import numpy as np
from pathlib import Path
from typing import Optional
import structlog
from django.conf import settings
from sentence_transformers import SentenceTransformer

logger = structlog.get_logger('educloud.rag')

# Singleton model (loaded once at startup)
_embedding_model: Optional[SentenceTransformer] = None


def get_embedding_model() -> SentenceTransformer:
    global _embedding_model
    if _embedding_model is None:
        logger.info('loading_embedding_model', model=settings.RAG_EMBEDDING_MODEL)
        _embedding_model = SentenceTransformer(settings.RAG_EMBEDDING_MODEL)
    return _embedding_model


def embed_text(text: str) -> bytes:
    """Embed text to 384-dim float32 vector, returned as bytes for sqlite-vec"""
    model = get_embedding_model()
    vec = model.encode([text], normalize_embeddings=True)[0]
    return struct.pack(f'{len(vec)}f', *vec)


class RAGPipeline:
    """
    Hybrid RAG pipeline: FTS5 BM25 + sqlite-vec cosine → RRF merge.
    Single SQLite file contains both text chunks and embeddings.
    """

    def __init__(self, db_path: Optional[str] = None):
        self.db_path = db_path or settings.RAG_DB_PATH
        self._conn: Optional[sqlite3.Connection] = None

    def _get_conn(self) -> sqlite3.Connection:
        if self._conn is None:
            self._conn = sqlite3.connect(self.db_path, check_same_thread=False)
            self._conn.row_factory = sqlite3.Row
            self._conn.enable_load_extension(True)
            sqlite_vec.load(self._conn)
            self._conn.enable_load_extension(False)
        return self._conn

    def retrieve(
        self,
        query: str,
        subject: str,
        grade: int,
        lang: str = 'sw',
        top_k: int = None,
    ) -> list[dict]:
        """
        Hybrid retrieval: FTS5 + vector search → RRF merge.
        Returns list of {chunk_id, text, subject, grade, strand, topic, score}.
        """
        top_k = top_k or settings.RAG_TOP_K
        conn = self._get_conn()

        # 1. Embed query
        query_vec = embed_text(query)

        # 2. Vector search (cosine similarity via sqlite-vec)
        vec_results = conn.execute(
            """
            SELECT c.chunk_id, c.text, c.subject, c.grade, c.strand, c.topic, c.lang,
                   vec_distance_cosine(e.embedding, ?) AS distance
            FROM chunks c
            JOIN chunk_embeddings e ON c.chunk_id = e.chunk_id
            WHERE c.subject = ? AND c.grade = ? AND c.lang = ?
            ORDER BY distance ASC
            LIMIT ?
            """,
            (query_vec, subject, grade, lang, top_k * 2),
        ).fetchall()

        # 3. FTS5 full-text search
        fts_results = conn.execute(
            """
            SELECT c.chunk_id, c.text, c.subject, c.grade, c.strand, c.topic, c.lang,
                   bm25(chunks_fts) AS bm25_score
            FROM chunks_fts
            JOIN chunks c ON chunks_fts.rowid = c.rowid
            WHERE chunks_fts MATCH ? AND c.subject = ? AND c.grade = ? AND c.lang = ?
            ORDER BY bm25_score ASC
            LIMIT ?
            """,
            (query, subject, grade, lang, top_k * 2),
        ).fetchall()

        # 4. Reciprocal Rank Fusion (RRF) merge
        # RRF score = 1/(k + rank), k=60 is standard
        k = 60
        scores: dict[str, float] = {}
        chunks: dict[str, dict] = {}

        for rank, row in enumerate(vec_results):
            cid = row['chunk_id']
            scores[cid] = scores.get(cid, 0) + 1 / (k + rank + 1)
            chunks[cid] = dict(row)

        for rank, row in enumerate(fts_results):
            cid = row['chunk_id']
            scores[cid] = scores.get(cid, 0) + 1 / (k + rank + 1)
            if cid not in chunks:
                chunks[cid] = dict(row)

        # 5. Sort by RRF score and return top_k
        ranked = sorted(scores.items(), key=lambda x: x[1], reverse=True)[:top_k]
        results = []
        for cid, score in ranked:
            # RRF scores use a different scale from cosine similarity.  With
            # k=60, even a document ranked first by both searches is ~0.033;
            # comparing it to a cosine-derived threshold silently drops every
            # valid result.  Configure an explicit RRF threshold if desired.
            if score >= getattr(settings, 'RAG_MIN_RRF_SCORE', 0.0):
                c = chunks[cid]
                results.append({
                    'chunk_id': cid,
                    'text': c['text'],
                    'subject': c['subject'],
                    'grade': c['grade'],
                    'strand': c.get('strand', ''),
                    'topic': c.get('topic', ''),
                    'lang': c.get('lang', lang),
                    'rrf_score': score,
                })

        logger.debug('rag_retrieved', query_len=len(query), results=len(results), subject=subject)
        return results

    def get_context_string(self, chunks: list[dict]) -> str:
        """Format retrieved chunks as context string for LLM prompt"""
        if not chunks:
            return ''
        lines = []
        for c in chunks:
            source = f"[Grade {c['grade']} {c['subject'].title()} — {c.get('topic', '')}]"
            lines.append(f'{source}\n{c["text"]}')
        return '\n\n---\n\n'.join(lines)

    def get_quiz_question(
        self,
        subject: str,
        grade: int,
        theta: float = 0.0,
        lang: str = 'sw',
    ) -> dict:
        """
        Fetch an IRT-calibrated MCQ from the content database.
        Difficulty b ≈ theta (target the student's current ability level).
        """
        conn = self._get_conn()
        target_difficulty = max(0.1, min(0.9, (theta + 3) / 6))  # Normalize IRT theta to [0.1, 0.9]

        row = conn.execute(
            """
            SELECT q.question_id, q.question_text, q.option_a, q.option_b,
                   q.option_c, q.option_d, q.correct_option, q.explanation,
                   q.difficulty, q.subject, q.grade, q.strand
            FROM questions q
            WHERE q.subject = ? AND q.grade = ? AND q.lang = ?
            ORDER BY ABS(q.difficulty - ?) ASC
            LIMIT 1
            """,
            (subject, grade, lang, target_difficulty),
        ).fetchone()

        if row:
            return {
                'question': row['question_text'],
                'a': row['option_a'],
                'b': row['option_b'],
                'c': row['option_c'],
                'd': row['option_d'],
                'correct': str(row['correct_option']),
                'explanation': row['explanation'],
                'difficulty': row['difficulty'],
                'strand': row['strand'],
            }

        # Fallback if no question found
        return {
            'question': 'Nambari 5 + 3 = ?' if lang == 'sw' else 'What is 5 + 3?',
            'a': '7', 'b': '8', 'c': '9', 'd': '6',
            'correct': '2',
            'explanation': '5 + 3 = 8' if lang == 'sw' else '5 + 3 = 8',
            'difficulty': 0.3,
            'strand': 'arithmetic',
        }

    def get_daily_lesson(self, subject: str, grade: int, lang: str = 'sw') -> str:
        """Get a short lesson tip for the day (rotates daily)"""
        from datetime import date
        day_of_year = date.today().timetuple().tm_yday
        conn = self._get_conn()

        rows = conn.execute(
            'SELECT text, topic FROM chunks WHERE subject = ? AND grade = ? AND lang = ? LIMIT 50',
            (subject, grade, lang),
        ).fetchall()

        if not rows:
            return 'Hakuna somo leo. Jaribu kesho!' if lang == 'sw' else 'No lesson today. Try tomorrow!'

        # Rotate through lessons by day
        chunk = rows[day_of_year % len(rows)]
        return f"Somo la Leo — {chunk['topic']}:\n{chunk['text'][:200]}"
