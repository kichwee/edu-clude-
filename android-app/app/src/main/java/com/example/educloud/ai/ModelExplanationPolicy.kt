package com.example.educloud.ai

import com.example.educloud.content.ContentLesson

/**
 * The only output shape a future local model may return to the learner UI.
 * A model response is untrusted until this policy accepts it.
 */
data class ModelExplanation(
    val explanation: String,
    val sourceId: String,
    val usesOnlySource: Boolean,
)

internal object ModelExplanationPolicy {
    const val MAX_WORDS = 55

    fun validate(
        candidate: ModelExplanation,
        lesson: ContentLesson,
        requireVerifiedAnswer: Boolean,
    ): Result<String> = runCatching {
        require(candidate.sourceId == lesson.id) { "Model returned an unrecognised source." }
        require(candidate.usesOnlySource) { "Model did not confirm the source boundary." }

        val fullText = candidate.explanation.trim()
        require(fullText.isNotBlank()) { "Model returned no explanation." }
        val bodyText = fullText.substringBefore("✨ Powered by").substringBefore("Source:").trim()
        require(bodyText.wordCount() <= MAX_WORDS) { "Model response is too long." }

        if (requireVerifiedAnswer && lesson.verifiedAnswer != null) {
            val verifiedAnswer = lesson.verifiedAnswer
            val answerPreserved = if (ANSWER_TOKEN.matches(verifiedAnswer)) {
                verifiedAnswer in ANSWER_TOKEN.findAll(fullText).map { it.value }.toList()
            } else {
                fullText.contains(verifiedAnswer, ignoreCase = true)
            }
            require(answerPreserved) { "Model changed or omitted the verified answer." }
        }
        fullText
    }

    private fun String.wordCount(): Int =
        trim().split(Regex("\\s+")).filter(String::isNotBlank).size

    private val ANSWER_TOKEN = Regex("\\b\\d+(?:\\.\\d+)?\\b")
}
