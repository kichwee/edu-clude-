package com.example.educloud.sync

import com.example.educloud.data.model.Interaction
import java.util.UUID

/** Maps only recorded, incorrect quiz selections into the bounded cloud contract. */
internal object TelemetryMapper {
    private const val TARGET_SKILL = "two_digit_subtraction_regrouping"
    private val subtractionPattern = Regex("(\\d{1,2})\\s*[-−]\\s*(\\d{1,2})")

    fun toAttemptOrNull(interaction: Interaction): SyncAttempt? {
        if (interaction.isCorrect != false || interaction.strand != TARGET_SKILL) return null
        val learnerAnswer = interaction.learnerAnswer?.toIntOrNull() ?: return null
        val match = subtractionPattern.find(interaction.question) ?: return null
        val minuend = match.groupValues[1].toIntOrNull() ?: return null
        val subtrahend = match.groupValues[2].toIntOrNull() ?: return null
        if (minuend !in 10..99 || subtrahend !in 1 until minuend) return null
        return SyncAttempt(
            attemptId = UUID.nameUUIDFromBytes("${interaction.id}:${interaction.question}".toByteArray()).toString(),
            skillId = TARGET_SKILL,
            minuend = minuend,
            subtrahend = subtrahend,
            learnerAnswer = learnerAnswer,
        )
    }
}
