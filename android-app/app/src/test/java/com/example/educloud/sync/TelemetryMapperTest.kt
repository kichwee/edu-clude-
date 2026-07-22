package com.example.educloud.sync

import com.example.educloud.data.model.Interaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TelemetryMapperTest {

    @Test
    fun mapsOnlyAnIncorrectRegroupingAttemptWithTheActualSelectedAnswer() {
        val interaction = Interaction(
            id = 7,
            studentId = 1,
            question = "What is 45 - 29?",
            aiResponse = "Try regrouping.",
            isCorrect = false,
            learnerAnswer = "26",
            subject = "math",
            strand = "two_digit_subtraction_regrouping",
        )

        val attempt = TelemetryMapper.toAttemptOrNull(interaction)

        assertEquals(45, attempt?.minuend)
        assertEquals(29, attempt?.subtrahend)
        assertEquals(26, attempt?.learnerAnswer)
    }

    @Test
    fun rejectsUnselectedOrCorrectAnswersInsteadOfFabricatingTelemetry() {
        val unsavedSelection = Interaction(
            id = 8,
            studentId = 1,
            question = "What is 82 - 37?",
            aiResponse = "Try regrouping.",
            isCorrect = false,
            subject = "math",
            strand = "two_digit_subtraction_regrouping",
        )
        val correctAnswer = unsavedSelection.copy(id = 9, isCorrect = true, learnerAnswer = "45")

        assertNull(TelemetryMapper.toAttemptOrNull(unsavedSelection))
        assertNull(TelemetryMapper.toAttemptOrNull(correctAnswer))
    }
}
