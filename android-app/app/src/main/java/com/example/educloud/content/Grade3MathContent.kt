package com.example.educloud.content

data class ContentLesson(
    val id: String,
    val topic: String,
    val keywords: Set<String>,
    val source: String,
    val version: String,
    val passage: String,
    val teachingSteps: List<String>,
    val definition: String,
    val verifiedAnswer: String? = null,
    val verifiedAnswerExplanation: String? = null,
    val verificationTokens: Set<String> = emptySet(),
    val answerRules: List<LessonAnswerRule> = emptyList(),
    val practicePrompt: String? = null,
    /** Optional curriculum location for the Term 1–3 source index. */
    val term: Int? = null,
    val week: Int? = null,
    val lesson: Int? = null,
    val sourcePageStart: Int? = null,
    val sourcePageEnd: Int? = null,
    val bookPageStart: Int? = null,
    val bookPageEnd: Int? = null,
)

data class LessonAnswerRule(
    val evidenceTerms: Set<String>,
    val answer: String,
    val explanation: String,
)

fun ContentLesson.sourceLabel(): String = "Source: $source ($version)"

/**
 * Android's offline view of the Grade 3 rule pack. The Kotlin cards are
 * generated from content/grade3_rule_tutor_v1.json; do not hand-edit them.
 */
object Grade3MathContent {
    const val SUBJECT = "math"
    const val GRADE = 3
    // Bump this whenever the bundled index changes so Room reseeds on-device.
    const val CONTENT_VERSION = "grade3-all-terms-source-0.3"

    internal val ruleCards: List<Grade3RuleTutorCard> = GeneratedGrade3RuleTutor.cards
    /**
     * The short rule cards remain the authoritative exact-answer path. The
     * full local source pack makes all three terms discoverable and lets the
     * tutor give a source-bounded explanation for the rest of Grade 3 Maths.
     */
    val lessons: List<ContentLesson> = ruleCards.map(Grade3RuleTutorCard::lesson) +
        GeneratedGrade3MathLessons.lessons

    fun find(question: String, candidates: Collection<ContentLesson> = lessons): ContentLesson? {
        Grade3NumericRules.lessonIdFor(question)?.let { lessonId ->
            candidates.firstOrNull { it.id == lessonId }?.let { return it }
        }
        if (Grade3NumericRules.isPlaceValueQuestion(question)) {
            candidates.firstOrNull { it.id == "g3-t1-w2-l2-tens-ones" }?.let { return it }
        }
        val questionTokens = tokenize(question)
        if (questionTokens.isEmpty()) return null

        return candidates
            .map { lesson -> lesson to score(questionTokens, lesson.keywords + lesson.verificationTokens) }
            .maxByOrNull { (_, score) -> score.total }
            ?.takeIf { (_, score) -> score.meetsConfidence }
            ?.first
    }

    fun findById(id: String): ContentLesson? = lessons.firstOrNull { it.id == id }

    private fun score(questionTokens: Set<String>, keywords: Set<String>): MatchScore {
        val matches = keywords.filter { keyword ->
            val keywordTokens = tokenize(keyword)
            keywordTokens.isNotEmpty() && keywordTokens.all(questionTokens::contains)
        }
        val hasNumericEvidence = matches.any { keyword -> tokenize(keyword).any { it.isNumericToken() } }
        val weightedScore = matches.sumOf { keyword ->
            if (tokenize(keyword).size > 1 || tokenize(keyword).any { it.isNumericToken() }) 2 else 1
        }
        return MatchScore(weightedScore, hasNumericEvidence || weightedScore >= MINIMUM_MATCH_SCORE)
    }

    private fun tokenize(value: String): Set<String> = value.lowercase()
        .split(Regex("[^a-z0-9]+"))
        .filter(String::isNotBlank)
        .toSet()

    private fun String.isNumericToken(): Boolean = isNotEmpty() && all(Char::isDigit)

    private data class MatchScore(val total: Int, val meetsConfidence: Boolean)
    private const val MINIMUM_MATCH_SCORE = 1
}
