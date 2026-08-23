/**
 * RAG Database Interface for React Native
 * Uses react-native-quick-sqlite to query the local sqlite-vec database.
 */
import { openQuickSQLite } from 'react-native-quick-sqlite';
import { Platform } from 'react-native';

const DB_NAME = 'cbc_knowledge.db';
const DB_LOCATION = Platform.OS === 'android' ? 'Files' : 'Documents';

export class LocalRAGPipeline {
  private db: any;

  constructor() {
    this.db = openQuickSQLite(DB_NAME, DB_LOCATION);
    // Note: sqlite-vec extensions must be bundled in the native Android/iOS build
  }

  /**
   * Retrieve context via FTS5.
   * (Vector search omitted for MVP UI simplicity unless sqlite-vec is compiled natively)
   */
  async retrieve(query: string, subject: string, grade: number, lang: string = 'sw'): Promise<any[]> {
    try {
      // Basic FTS5 retrieval for offline mode
      const { rows } = await this.db.executeAsync(
        `SELECT c.chunk_id, c.text, c.topic 
         FROM chunks_fts 
         JOIN chunks c ON chunks_fts.rowid = c.rowid
         WHERE chunks_fts MATCH ? AND c.subject = ? AND c.grade = ? AND c.lang = ?
         LIMIT 3`,
        [query, subject, grade, lang]
      );
      
      const results = [];
      for (let i = 0; i < rows.length; i++) {
        results.push(rows.item(i));
      }
      return results;
    } catch (e) {
      console.error('RAG Retrieval error:', e);
      return [];
    }
  }

  async getQuizQuestion(subject: string, grade: number, theta: number, lang: string = 'sw'): Promise<any> {
    const targetDifficulty = Math.max(0.1, Math.min(0.9, (theta + 3) / 6));
    
    try {
      const { rows } = await this.db.executeAsync(
        `SELECT * FROM questions 
         WHERE subject = ? AND grade = ? AND lang = ?
         ORDER BY ABS(difficulty - ?) ASC LIMIT 1`,
        [subject, grade, lang, targetDifficulty]
      );
      
      if (rows.length > 0) {
        return rows.item(0);
      }
    } catch (e) {
      console.error('Quiz error:', e);
    }
    return null;
  }
}
