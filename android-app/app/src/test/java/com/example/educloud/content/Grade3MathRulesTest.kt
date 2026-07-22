package com.example.educloud.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Grade3MathRulesTest {
    @Test
    fun answersBasicAdditionDirectly() {
        assertEquals(
            "🎉 7 + 5 = 12. Put the two groups together!",
            Grade3MathRules.answerFor("What is 7 + 5?"),
        )
    }

    @Test
    fun answersAConstantNumberPatternDirectly() {
        assertEquals(
            "🎉 The next number is 8. Each time, add 2.",
            Grade3MathRules.answerFor("What comes next: 2, 4, 6?"),
        )
    }

    @Test
    fun explainsPlaceValueForTwoDigitNumbers() {
        assertEquals(
            "🎉 36 has 3 tens and 6 ones.",
            Grade3MathRules.answerFor("How many tens and ones are in 36?"),
        )
    }

    @Test
    fun declinesUnevenDivision() {
        assertTrue(Grade3MathRules.answerFor("What is 7 divided by 2?") == null)
    }
}
