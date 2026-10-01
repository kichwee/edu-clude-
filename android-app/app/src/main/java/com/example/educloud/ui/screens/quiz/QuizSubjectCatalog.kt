package com.example.educloud.ui.screens.quiz

/**
 * Canonical quiz subject IDs. UI labels must never be used as navigation or
 * storage values because labels are not stable identifiers.
 */
internal object QuizSubjectCatalog {
    const val MATHEMATICS_ID = "math"

    fun normalize(raw: String): String {
        return when (raw.trim().lowercase()) {
            "math", "mathematics", "grade 3 mathematics", "grade3 math", "grade3math" -> MATHEMATICS_ID
            "english" -> "english"
            "kiswahili" -> "kiswahili"
            "science", "science & tech", "science and tech" -> "science"
            "social-studies", "social studies" -> "social-studies"
            "creative-arts", "creative arts" -> "creative-arts"
            else -> raw.trim().lowercase()
        }
    }

    fun questionsFor(subjectId: String): List<QuizQuestion>? = questionBanks[normalize(subjectId)]

    fun unavailableMessage(subjectId: String): String {
        val display = subjectId.ifBlank { "this subject" }
        return "Quick checks are not available for $display. " +
            "This MVP currently supports Grade 3 Mathematics only."
    }

    private val questionBanks: Map<String, List<QuizQuestion>> = mapOf(
        MATHEMATICS_ID to listOf(
            QuizQuestion(
                text = "What is 45 - 29?",
                options = listOf("26", "16", "24", "14"),
                correctIndex = 1,
                difficulty = 0.3f,
                explanation = "Trade 1 ten from 4 tens. Then 15 - 9 = 6 and 3 - 2 = 1, so the answer is 16.",
                skillId = "two_digit_subtraction_regrouping",
                itemId = "g3-regroup-45-29",
            ),
            QuizQuestion(
                text = "What is 82 - 37?",
                options = listOf("55", "45", "35", "47"),
                correctIndex = 1,
                difficulty = 0.4f,
                explanation = "Trade 1 ten so 12 - 7 = 5. Then 7 - 3 = 4, so the answer is 45.",
                skillId = "two_digit_subtraction_regrouping",
                itemId = "g3-regroup-82-37",
            ),
            QuizQuestion(
                text = "What is 63 - 28?",
                options = listOf("45", "35", "25", "41"),
                correctIndex = 1,
                difficulty = 0.5f,
                explanation = "Trade 1 ten so 13 - 8 = 5. Then 5 - 2 = 3, so the answer is 35.",
                skillId = "two_digit_subtraction_regrouping",
                itemId = "g3-regroup-63-28",
            )
        )
    )
}
