package com.example.educloud.data.repository

import com.example.educloud.content.Grade3MathContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineLessonRepositoryTest {

    @Test
    fun buildsFtsQueryFromSafeWordTokens() {
        assertEquals("equal* OR groups*", LessonSearchQuery.build("Equal groups!!!"))
        assertEquals("7* OR 5*", LessonSearchQuery.build("7 + 5"))
    }

    @Test
    fun rejectsQuestionWithNoSearchableTokens() {
        assertNull(LessonSearchQuery.build("?!"))
        assertNull(LessonSearchQuery.build(""))
    }

    @Test
    fun doesNotTreatTheXInAnOrdinaryWordAsMultiplication() {
        assertNull(Grade3MathContent.find("Explain dinosaurs"))
    }

    @Test
    fun selectsTheCountingTwosExampleForABareAnswerCheck() {
        assertEquals(
            "g3-t1-w2-l1-counting-twos",
            Grade3MathContent.find("Are 517 and 520 the answers?")?.id,
        )
    }
}
