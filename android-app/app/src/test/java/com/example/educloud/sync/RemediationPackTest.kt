package com.example.educloud.sync

import com.example.educloud.content.Grade3MathContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemediationPackTest {

    @Test
    fun downloadedReviewRequiredPackBecomesASearchableOfflineLesson() {
        val pack = RemediationPack(
            schemaVersion = "1",
            packId = "9430e213-1399-4655-aa4b-96ef6a36150f",
            contentVersion = "remediation-9430e2131399",
            label = "Personalised Grade 3 Maths remediation",
            targetSkill = "two_digit_subtraction_regrouping",
            validationStatus = "automatic_validation_passed",
            provenance = "AI-generated original demo remediation; automatically validated for arithmetic.",
            lessons = listOf(
                RemediationLesson(
                    id = "remediation-9430e2131399-regrouping",
                    topic = "Trade one ten, then subtract",
                    source = "Personalised Grade 3 Maths practice · automatic checks passed",
                    keywords = listOf("subtraction", "regrouping", "borrow", "tens", "ones"),
                    microLesson = "Trade one ten for ten ones before subtracting the ones column.",
                    teachingSteps = listOf("Check ones", "Trade a ten", "Subtract ones then tens"),
                    definition = "Regrouping trades one ten for ten ones.",
                    practiceQuestions = listOf(
                        RemediationPracticeQuestion(minuend = 45, subtrahend = 29, answer = 16),
                        RemediationPracticeQuestion(minuend = 82, subtrahend = 37, answer = 45),
                        RemediationPracticeQuestion(minuend = 63, subtrahend = 28, answer = 35),
                    ),
                ),
            ),
        )

        val lesson = pack.lessons.single().toContentLesson(pack.contentVersion)
        val match = Grade3MathContent.find("Help me regroup when I subtract", listOf(lesson))

        assertEquals("remediation-9430e2131399-regrouping", lesson.id)
        assertTrue(lesson.source.contains("automatic checks passed"))
        assertTrue(lesson.passage.contains("45 - 29 = 16"))
        assertEquals(lesson.id, match?.id)
    }
}
