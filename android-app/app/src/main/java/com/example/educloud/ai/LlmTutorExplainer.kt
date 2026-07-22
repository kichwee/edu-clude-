package com.example.educloud.ai

import com.example.educloud.content.ContentLesson
import com.example.educloud.content.sourceLabel

/**
 * Source-bounded LLM Explainer for Grade 3 learners.
 *
 * Designed to communicate at a Grade 3 cognitive level: warm, encouraging,
 * engaging, non-robotic, and easy to understand (simple vocabulary, short
 * sentences). Uses the retrieved lesson passage as strict context to prevent
 * hallucination.
 */
object LlmTutorExplainer {

    fun generatePrompt(
        question: String,
        lesson: ContentLesson,
        mode: ExplainerMode = ExplainerMode.EXPLAIN,
        deterministicAnswer: String? = null,
    ): String {
        val topic = lesson.topic
        val passage = lesson.passage
        val definition = lesson.definition
        val steps = lesson.teachingSteps.joinToString(separator = "; ")
        val verifiedAnswerInfo = lesson.verifiedAnswer?.let { "VERIFIED ANSWER: $it" } ?: ""
        val verifiedExplanationInfo = lesson.verifiedAnswerExplanation?.let { "VERIFIED EXPLANATION: $it" } ?: ""
        val ruleAnswerInfo = deterministicAnswer?.let { "EXACT RULE CALCULATED ANSWER: $it" } ?: ""
        val pageInfo = "SOURCE LOCATION: ${lesson.sourceLabel()}" +
            (lesson.bookPageStart?.let { start -> " (Book p.$start" + (lesson.bookPageEnd?.let { end -> "–$end" } ?: "") + ")" } ?: "")

        return when (mode) {
            ExplainerMode.STORY -> """
                You are a friendly Grade 3 Maths teacher in Kenya.
                Explain this lesson using a short, fun story with local items like mangoes, goats, or football stickers.
                Keep it under 50 words, very warm and encouraging.
                
                LESSON TOPIC: $topic
                LESSON PASSAGE: $passage
                LESSON DEFINITION: $definition
                $verifiedAnswerInfo
                $ruleAnswerInfo
                $pageInfo
                QUESTION: $question
            """.trimIndent()

            ExplainerMode.HINT -> """
                You are a supportive Grade 3 Maths tutor.
                Give the learner a small, friendly hint to help them think about the answer.
                Do not give away the answer directly. Keep it under 25 words.
                
                LESSON TOPIC: $topic
                TEACHING STEPS: $steps
                $pageInfo
                QUESTION: $question
            """.trimIndent()

            ExplainerMode.EXPLAIN -> {
                val isNumericOnly = question.trim().all { it.isDigit() }
                val strictAnswerInstruction = if (isNumericOnly || question.trim().length < 3) {
                    "The user selected this topic. Extract and summarize the core concepts directly from the LESSON PASSAGE in a fun, engaging way. Do NOT just output the page number."
                } else if (deterministicAnswer != null || lesson.verifiedAnswer != null) {
                    "You MUST state the EXACT answer provided below, and cite the book page."
                } else {
                    "Extract the specific answer directly from the LESSON PASSAGE based on the keywords. State it clearly and cite the book page."
                }
                
                """
                You are an encouraging Grade 3 Maths teacher.
                Answer the question directly and simply. $strictAnswerInstruction
                Keep sentences short and cheerful. Maximum 55 words.
                
                LESSON TOPIC: $topic
                LESSON PASSAGE: $passage
                LESSON DEFINITION: $definition
                TEACHING STEPS: $steps
                $verifiedAnswerInfo
                $verifiedExplanationInfo
                $ruleAnswerInfo
                $pageInfo
                QUESTION: $question
                """.trimIndent()
            }
        }
    }

    /**
     * Synthesizes a warm, non-robotic Grade 3 explanation grounded in the lesson passage.
     * When offline or on low-memory devices, it dynamically transforms the lesson passage
     * into engaging child-friendly prose.
     */
    fun explainGrounded(
        question: String,
        lesson: ContentLesson,
        mode: ExplainerMode = ExplainerMode.EXPLAIN,
        deterministicAnswer: String? = null,
    ): String {
        val baseExplanation = when (mode) {
            ExplainerMode.STORY -> {
                val storyContext = when {
                    "position" in lesson.id -> "Imagine 5 friends standing in line for chai. The first person gets chai first! Mary is fifth in line."
                    "counting-twos" in lesson.id -> "Juma has baskets of mangoes! Each basket has 2 mangoes: 2, 4, 6, 8... We just add 2 more every time!"
                    "tens-ones" in lesson.id -> "Amina has 3 bundles of 10 sticks and 6 single sticks. That makes 3 tens and 6 ones, or 36 altogether!"
                    "number-words" in lesson.id -> "When we write numbers in words, 25 becomes 'twenty five'. Like writing names for numbers!"
                    else -> "Imagine sharing ${lesson.topic.lowercase()} with friends! ${lesson.passage}"
                }
                "🌟 Here is a story for you!\n\n$storyContext"
            }
            ExplainerMode.HINT -> {
                val hintStep = lesson.teachingSteps.firstOrNull() ?: lesson.definition
                "💡 Helpful Hint!\n\n$hintStep\nCan you give it a try now?"
            }
            ExplainerMode.EXPLAIN -> {
                val stepsText = lesson.teachingSteps.mapIndexed { i, step -> "Step ${i + 1}: $step" }.joinToString("\n")
                val answerPrefix = deterministicAnswer ?: lesson.verifiedAnswer?.let { "The answer is $it." } ?: ""
                val header = if (answerPrefix.isNotBlank()) "$answerPrefix\n\n" else ""
                "${header}Hello there! Let's learn about ${lesson.topic} together! 😊\n\n${lesson.passage}\n\n$stepsText"
            }
        }

        return "$baseExplanation\n\n${lesson.sourceLabel()}"
    }

    enum class ExplainerMode {
        EXPLAIN, STORY, HINT
    }
}
