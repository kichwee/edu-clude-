package com.example.educloud.ai

import com.example.educloud.content.Grade3MathContent
import com.example.educloud.data.repository.LessonSearchQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The five minimum safety cases for the bundled, deterministic tutor path. */
class RetrievalRegressionTest {

    @Test
    fun matchedConceptRetrievesTheCountingTwosLesson() {
        assertEquals(
            "g3-t1-w2-l1-counting-twos",
            Grade3MathContent.find("How do I count in twos?")?.id,
        )
    }

    @Test
    fun offTopicQuestionDoesNotRetrieveALesson() {
        assertNull(Grade3MathContent.find("Tell me about dinosaurs."))
    }

    @Test
    fun wrongAnswerIsCorrectedWithThePackAnswer() {
        val lesson = checkNotNull(Grade3MathContent.findById("g3-t1-w2-l1-counting-twos"))
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "Are 517 and 520 the correct answers for 511, 513, 515?",
            lesson = lesson,
        )

        assertTrue(response.startsWith("Not yet. The verified answer is 517, 519."))
    }

    @Test
    fun punctuationOnlyInputNeverCreatesAnFtsQuery() {
        assertNull(LessonSearchQuery.build("?!..."))
    }

    @Test
    fun everyAnswerCarriesTheSharedPackSource() {
        val lesson = checkNotNull(Grade3MathContent.findById("g3-t1-w2-l2-tens-ones"))
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "What is 36 made of?",
            lesson = lesson,
        )

        assertTrue(response.contains("Source: Term 1 · Week 2 · Lesson 2 · Book p.16"))
    }

    @Test
    fun substringOnlyMatchDoesNotPassRetrievalConfidence() {
        assertNull(Grade3MathContent.find("someone"))
    }

    @Test
    fun allTermSourceRecordsAreIncludedInTheLearnerFacingPack() {
        assertTrue(Grade3MathContent.findById("g3-t1-w1-l1-source") != null)
        assertTrue(Grade3MathContent.lessons.any { it.term == 2 })
        assertTrue(Grade3MathContent.lessons.any { it.term == 3 })
    }
}
