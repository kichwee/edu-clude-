package com.example.educloud.ui.screens.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizSubjectCatalogTest {

    @Test
    fun canonicalMathIdResolvesTheQuestionBank() {
        assertEquals(3, QuizSubjectCatalog.questionsFor(QuizSubjectCatalog.MATHEMATICS_ID)?.size)
    }

    @Test
    fun displayLabelsAndUnsupportedDeepLinksAreUnavailable() {
        assertNull(QuizSubjectCatalog.questionsFor("Mathematics"))
        assertNull(QuizSubjectCatalog.questionsFor("english"))
        assertTrue(QuizSubjectCatalog.unavailableMessage("Mathematics").contains("not available"))
    }
}
