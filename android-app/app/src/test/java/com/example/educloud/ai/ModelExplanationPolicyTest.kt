package com.example.educloud.ai

import com.example.educloud.content.Grade3MathContent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelExplanationPolicyTest {
    private val tensAndOnes = checkNotNull(Grade3MathContent.findById("g3-t1-w2-l2-tens-ones"))

    @Test
    fun acceptsAShortExplanationWithTheCorrectSourceAndVerifiedAnswer() {
        val result = ModelExplanationPolicy.validate(
            candidate = ModelExplanation(
                explanation = "36 is 3 tens and 6 ones.",
                sourceId = tensAndOnes.id,
                usesOnlySource = true,
            ),
            lesson = tensAndOnes,
            requireVerifiedAnswer = true,
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun rejectsAChangedVerifiedAnswer() {
        val result = ModelExplanationPolicy.validate(
            candidate = ModelExplanation(
                explanation = "36 is 3 tens and 7 ones.",
                sourceId = tensAndOnes.id,
                usesOnlySource = true,
            ),
            lesson = tensAndOnes,
            requireVerifiedAnswer = true,
        )

        assertFalse(result.isSuccess)
    }

    @Test
    fun rejectsAnExplanationThatClaimsAnotherSource() {
        val result = ModelExplanationPolicy.validate(
            candidate = ModelExplanation(
                explanation = "36 is 3 tens and 6 ones.",
                sourceId = "made-up-source",
                usesOnlySource = true,
            ),
            lesson = tensAndOnes,
            requireVerifiedAnswer = false,
        )

        assertFalse(result.isSuccess)
    }

    @Test
    fun rejectsMoreThanFiftyFiveWords() {
        val result = ModelExplanationPolicy.validate(
            candidate = ModelExplanation(
                explanation = List(56) { "word" }.joinToString(" "),
                sourceId = tensAndOnes.id,
                usesOnlySource = true,
            ),
            lesson = tensAndOnes,
            requireVerifiedAnswer = false,
        )

        assertFalse(result.isSuccess)
    }
}
