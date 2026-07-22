package com.example.educloud.ai

import com.example.educloud.content.Grade3MathContent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicTutorTest {

    private val countingTwos = checkNotNull(Grade3MathContent.findById("g3-t1-w2-l1-counting-twos"))
    private val tensAndOnes = checkNotNull(Grade3MathContent.findById("g3-t1-w2-l2-tens-ones"))

    @Test
    fun correctsAnIncorrectAnswerForAnIdentifiedSourceExample() {
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "Are 520 correct for 511, 513, 515?",
            lesson = countingTwos,
        )

        assertTrue(response.startsWith("Not yet. The verified answer is 517, 519."))
        assertTrue(response.contains("Source: Term 1 · Week 2 · Lesson 1 · Book p.14"))
    }

    @Test
    fun acceptsTheCompleteVerifiedSequenceButNotAPartialMatch() {
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "Are 517 and 519 the correct answers for 511, 513, 515?",
            lesson = countingTwos,
        )

        assertTrue(response.startsWith("Yes. The verified answer is 517, 519."))
    }

    @Test
    fun explainsOnlyTheVerifiedTensAndOnesFact() {
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "Explain the tens and ones in 36.",
            lesson = tensAndOnes,
        )

        assertTrue(response.contains("36 is 3 tens and 6 ones"))
        assertTrue(response.contains("Look at the tens digit"))
    }

    @Test
    fun answersANumericWhatIsQuestionWithTheVerifiedFact() {
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "What is 36 made of?",
            lesson = tensAndOnes,
        )

        assertTrue(response.startsWith("The answer is 3 tens and 6 ones."))
    }

    @Test
    fun givesAHintInsteadOfAnAnswerWhenTheLearnerAsksForOne() {
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "Give me a hint for counting in twos.",
            lesson = countingTwos,
        )

        assertTrue(response.startsWith("Hint: Find the change between two numbers."))
        assertFalse(response.contains("The answer is 517, 519"))
    }

    @Test
    fun answersOnlyAnExplicitSourceCardInsteadOfInventingANewSum() {
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "What is 7 plus 7?",
            lesson = countingTwos,
        )

        assertTrue(response.startsWith("I can explain Counting in twos"))
        assertFalse(response.contains("The answer is 14"))
    }

    @Test
    fun handlesGreetingWithoutNeedingToRetrieveALesson() {
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "Hello",
            lesson = null,
        )

        assertTrue(response.startsWith("Hello! I am your offline Grade 3 Maths tutor."))
    }

    @Test
    fun continuesAnySupportedCountingInTwosSequenceWithinTheGradeThreeRange() {
        val lesson = Grade3MathContent.find("What comes after 302, 304, 306?")
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "What comes after 302, 304, 306?",
            lesson = lesson,
        )

        assertTrue(lesson?.id == "g3-t1-w2-l1-counting-twos")
        assertTrue(response.startsWith("The next two numbers are 308, 310."))
    }

    @Test
    fun explainsAnyTwoDigitPlaceValueQuestionWithoutAModel() {
        val lesson = Grade3MathContent.find("What are the tens and ones in 47?")
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "What are the tens and ones in 47?",
            lesson = lesson,
        )

        assertTrue(lesson?.id == "g3-t1-w2-l2-tens-ones")
        assertTrue(response.startsWith("47 is 4 tens and 7 ones."))
    }

    @Test
    fun refusesAnUnmatchedQuestionInsteadOfSelectingTheFirstLesson() {
        val response = DeterministicTutor.respond(
            subject = Grade3MathContent.SUBJECT,
            learnerQuestion = "Tell me about dinosaurs.",
            lesson = Grade3MathContent.find("Tell me about dinosaurs."),
        )

        assertTrue(response.startsWith("I do not have a rule for that yet."))
        assertFalse(response.contains("mangoes"))
    }

    @Test
    fun generatesLlmPromptWithVerifiedAnswerRuleAnswerAndPageCitation() {
        val prompt = LlmTutorExplainer.generatePrompt(
            question = "What is 36 made of?",
            lesson = tensAndOnes,
            deterministicAnswer = "36 is 3 tens and 6 ones",
        )

        assertTrue(prompt.contains("VERIFIED ANSWER: 3 tens and 6 ones"))
        assertTrue(prompt.contains("EXACT RULE CALCULATED ANSWER: 36 is 3 tens and 6 ones"))
        assertTrue(prompt.contains("SOURCE LOCATION: Source: Term 1 · Week 2 · Lesson 2 · Book p.16"))
    }

    @Test
    fun groundedExplanationIncludesVerifiedAnswerAndSourcePage() {
        val explanation = LlmTutorExplainer.explainGrounded(
            question = "Explain 36",
            lesson = tensAndOnes,
            deterministicAnswer = "36 is 3 tens and 6 ones",
        )

        assertTrue(explanation.contains("36 is 3 tens and 6 ones"))
        assertTrue(explanation.contains("Source: Term 1 · Week 2 · Lesson 2 · Book p.16"))
    }
}
