package com.example.educloud.ai

/**
 * IrtEngine — 2-Parameter Item Response Theory adaptive difficulty.
 *
 * Implements the 2PL IRT model from FR-06:
 *   P(correct | θ, a, b) = 1 / (1 + exp(-a * (θ - b)))
 *
 * Where:
 *   θ (theta) = student ability estimate
 *   a         = item discrimination parameter
 *   b         = item difficulty parameter
 *
 * Ability is updated via a simplified MLE update after each response.
 * SM-2 spaced repetition intervals: 1, 3, 7, 14, 30 days.
 */
class IrtEngine {

    companion object {
        // SM-2 spaced repetition intervals in days
        val SM2_INTERVALS = intArrayOf(1, 3, 7, 14, 30)
        const val THETA_MIN = -3.0
        const val THETA_MAX = 3.0
        const val LEARNING_RATE = 0.3
    }

    /**
     * Updates student ability (theta) after answering a question.
     * @param currentTheta current ability estimate
     * @param isCorrect whether the student answered correctly
     * @param itemDifficulty difficulty of the item (b parameter, default 0.5)
     * @param itemDiscrimination discrimination of the item (a parameter, default 1.0)
     * @return updated theta estimate
     */
    fun updateTheta(
        currentTheta: Double,
        isCorrect: Boolean,
        itemDifficulty: Double = 0.0,
        itemDiscrimination: Double = 1.0
    ): Double {
        val pCorrect = probability(currentTheta, itemDiscrimination, itemDifficulty)
        val observed = if (isCorrect) 1.0 else 0.0
        val gradient = itemDiscrimination * (observed - pCorrect)
        val newTheta = currentTheta + LEARNING_RATE * gradient
        return newTheta.coerceIn(THETA_MIN, THETA_MAX)
    }

    /**
     * Selects next question difficulty based on current theta.
     * Targets a question near the student's ability level (b ≈ theta).
     */
    fun selectNextDifficulty(theta: Double): Float {
        // Normalize theta (-3..3) to difficulty (0..1)
        return ((theta + 3.0) / 6.0).toFloat().coerceIn(0.1f, 0.9f)
    }

    /**
     * Returns SM-2 review interval index based on consecutive correct answers.
     */
    fun getReviewInterval(consecutiveCorrect: Int): Int {
        val index = consecutiveCorrect.coerceAtMost(SM2_INTERVALS.size - 1)
        return SM2_INTERVALS[index]
    }

    /**
     * 2PL IRT probability of correct response.
     */
    private fun probability(theta: Double, a: Double, b: Double): Double {
        return 1.0 / (1.0 + Math.exp(-a * (theta - b)))
    }

    /**
     * Converts a stored Float difficulty (0..1) to IRT b parameter (-3..3 scale).
     */
    fun difficultyToB(difficulty: Float): Double = (difficulty * 6.0) - 3.0

    /**
     * Converts IRT theta to a human-readable level label.
     */
    fun thetaToLabel(theta: Double): String = when {
        theta < -1.5 -> "Beginner"
        theta < 0.0 -> "Developing"
        theta < 1.5 -> "Proficient"
        else -> "Advanced"
    }
}
