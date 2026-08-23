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
    fun displayLabelsNormalizeToCanonicalIdsWhileUnknownSubjectsStayUnavailable() {
        // Display labels resolve through normalize() to the same canonical bank;
        // genuinely unknown subjects still get no bank and the honest message.
        assertEquals(
            QuizSubjectCatalog.questionsFor(QuizSubjectCatalog.MATHEMATICS_ID),
            QuizSubjectCatalog.questionsFor("Mathematics"),
        )
        assertNull(QuizSubjectCatalog.questionsFor("history"))
        assertTrue(QuizSubjectCatalog.unavailableMessage("History").contains("not available"))
    }
}
