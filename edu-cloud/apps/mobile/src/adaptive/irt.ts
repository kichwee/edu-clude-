/**
 * Item Response Theory (IRT) Engine for Adaptive Learning (Track A)
 * 1PL (Rasch) Model implemented in JavaScript for offline sync compatibility.
 * Mirrors the Python implementation in apps/irt/engine.py
 */

const LEARNING_RATE = 0.4;
const MIN_THETA = -3.0;
const MAX_THETA = 3.0;

export class IRTEngine {
  /**
   * Calculate probability of correct answer given student ability and question difficulty
   */
  static probabilityCorrect(theta: number, difficultyB: number): number {
    const x = Math.max(Math.min(theta - difficultyB, 10), -10);
    return 1.0 / (1.0 + Math.exp(-x));
  }

  /**
   * Update student ability (theta) after answering a question
   */
  static recordResponse(currentTheta: number, isCorrect: boolean, difficultyB: number = 0.0): number {
    const expectedProb = this.probabilityCorrect(currentTheta, difficultyB);
    const actualScore = isCorrect ? 1.0 : 0.0;
    
    const update = LEARNING_RATE * (actualScore - expectedProb);
    const newTheta = Math.max(MIN_THETA, Math.min(MAX_THETA, currentTheta + update));
    
    return newTheta;
  }

  /**
   * SuperMemo-2 Spaced Repetition Algorithm
   * Returns [nextIntervalDays, newEasinessFactor]
   */
  static sm2NextInterval(repetitions: number, easinessFactor: number, quality: number): [number, number] {
    let interval = 1;
    
    if (quality < 3) {
      repetitions = 0;
      interval = 1;
    } else {
      if (repetitions === 0) {
        interval = 1;
      } else if (repetitions === 1) {
        interval = 6;
      } else {
        interval = Math.round(interval * easinessFactor);
      }
      repetitions += 1;
    }
    
    let ef = easinessFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
    ef = Math.max(1.3, ef);
    
    return [interval, ef];
  }
}
