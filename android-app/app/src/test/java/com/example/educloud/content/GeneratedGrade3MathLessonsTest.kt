package com.example.educloud.content

import org.junit.Assert.assertTrue
import org.junit.Test

class LearnerFacingGrade3MathContentTest {

    @Test
    fun containsRuleCardsAndAllThreeTermSourceRecords() {
        assertTrue(Grade3MathContent.lessons.size >= 140)
        assertTrue(Grade3MathContent.lessons.all { it.passage.isNotBlank() && it.teachingSteps.isNotEmpty() })
        assertTrue(Grade3MathContent.lessons.any { it.term == 1 })
        assertTrue(Grade3MathContent.lessons.any { it.term == 2 })
        assertTrue(Grade3MathContent.lessons.any { it.term == 3 })
        assertTrue(Grade3MathContent.find("Explain spending and saving") != null)
    }
}
