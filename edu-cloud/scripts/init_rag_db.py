import sqlite3
import sqlite_vec
from pathlib import Path
import os
import argparse

def init_db(db_path: str):
    """
    Initialize the SQLite database for the RAG pipeline.
    Creates tables for chunks, embeddings (sqlite-vec), FTS5, and questions.
    """
    db_file = Path(db_path)
    if db_file.exists():
        print(f"Warning: Database {db_path} already exists.")
        
    db_file.parent.mkdir(parents=True, exist_ok=True)
        
    conn = sqlite3.connect(db_path)
    conn.enable_load_extension(True)
    sqlite_vec.load(conn)
    conn.enable_load_extension(False)
    
    cur = conn.cursor()
    
    # 1. Chunks table (stores text and metadata)
    cur.execute("""
        CREATE TABLE IF NOT EXISTS chunks (
            chunk_id TEXT PRIMARY KEY,
            text TEXT NOT NULL,
            subject TEXT NOT NULL,
            grade INTEGER NOT NULL,
            strand TEXT,
            topic TEXT,
            lang TEXT DEFAULT 'sw',
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    """)
    
    # 2. Vector Embeddings table (sqlite-vec)
    # 384 dimensions for multilingual-e5-small
    cur.execute("""
        CREATE VIRTUAL TABLE IF NOT EXISTS chunk_embeddings USING vec0(
            chunk_id TEXT PRIMARY KEY,
            embedding float[384]
        )
    """)
    
    # 3. FTS5 table for full-text search
    cur.execute("""
        CREATE VIRTUAL TABLE IF NOT EXISTS chunks_fts USING fts5(
            text,
            content='chunks',
            content_rowid='rowid'
        )
    """)
    
    # Trigger to keep FTS index updated when chunks are inserted
    cur.execute("""
        CREATE TRIGGER IF NOT EXISTS chunks_ai AFTER INSERT ON chunks
        BEGIN
            INSERT INTO chunks_fts(rowid, text) VALUES (new.rowid, new.text);
        END
    """)
    
    # Trigger to keep FTS index updated when chunks are deleted
    cur.execute("""
        CREATE TRIGGER IF NOT EXISTS chunks_ad AFTER DELETE ON chunks
        BEGIN
            INSERT INTO chunks_fts(chunks_fts, rowid, text) VALUES ('delete', old.rowid, old.text);
        END
    """)
    
    # 4. Questions table (for IRT-adapted quizzes)
    cur.execute("""
        CREATE TABLE IF NOT EXISTS questions (
            question_id TEXT PRIMARY KEY,
            subject TEXT NOT NULL,
            grade INTEGER NOT NULL,
            strand TEXT,
            lang TEXT DEFAULT 'sw',
            difficulty REAL NOT NULL DEFAULT 0.5,
            question_text TEXT NOT NULL,
            option_a TEXT NOT NULL,
            option_b TEXT NOT NULL,
            option_c TEXT NOT NULL,
            option_d TEXT NOT NULL,
            correct_option INTEGER NOT NULL CHECK (correct_option BETWEEN 1 AND 4),
            explanation TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    """)
    
    # Indexes for fast filtering
    cur.execute("CREATE INDEX IF NOT EXISTS idx_chunks_filter ON chunks(subject, grade, lang)")
    cur.execute("CREATE INDEX IF NOT EXISTS idx_questions_filter ON questions(subject, grade, lang)")
    cur.execute("CREATE INDEX IF NOT EXISTS idx_questions_difficulty ON questions(difficulty)")
    
    conn.commit()
    conn.close()
    print(f"✅ RAG Database initialized successfully at: {db_path}")

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Initialize RAG Database")
    parser.add_argument('--path', default='data/cbc_knowledge.db', help='Path to SQLite database file')
    args = parser.parse_args()
    
    # Resolve relative to script location if needed
    if not os.path.isabs(args.path):
        base_dir = Path(__file__).resolve().parent.parent
        db_path = str(base_dir / args.path)
    else:
        db_path = args.path
        
    init_db(db_path)
