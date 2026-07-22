package com.example.educloud.ai

import com.example.educloud.content.ContentLesson
import com.example.educloud.content.Grade3MathContent
import com.example.educloud.content.Grade3MathRules
import com.example.educloud.content.Grade3NumericRules
import com.example.educloud.content.LessonAnswerRule
import com.example.educloud.content.sourceLabel

/**
 * A SmarterChild-style tutor: it classifies a small set of text intents, then
 * selects only a handwritten response or answer card from the local pack.
 * It only evaluates the bounded numeric rule families in that pack; it never
 * guesses a fact or uses model output.
 */
internal object DeterministicTutor {

    fun respond(
        subject: String,
        learnerQuestion: String,
        lesson: ContentLesson?,
    ): String {
        if (subject != Grade3MathContent.SUBJECT) return unsupportedSubject()
        when (intentFor(learnerQuestion)) {
            Intent.GREETING -> return greeting()
            Intent.GOODBYE -> return goodbye()
            Intent.CAPABILITIES -> return capabilities()
            else -> Unit
        }
        if (intentFor(learnerQuestion) == Intent.PRACTICE && lesson == null) {
            return "🎲 Challenge time! Amina has 6 mangoes and gets 7 more. How many mangoes does she have now?\n\nLocal rule challenge"
        }
        if (lesson == null) {
            Grade3MathRules.answerFor(learnerQuestion)?.let { answer ->
                return "$answer\n\nLocal calculation rule"
            }
            return unsupportedQuestion()
        }

        val response = when (intentFor(learnerQuestion)) {
            Intent.ANSWER_CHECK -> answerCheck(learnerQuestion, lesson)
            Intent.HINT -> hint(lesson)
            Intent.PRACTICE -> practice(lesson)
            Intent.STORY -> story(lesson)
            Intent.DEFINITION -> definition(lesson)
            Intent.DIRECT_ANSWER -> directAnswer(learnerQuestion, lesson)
            else -> explanation(lesson)
        }
        return "$response\n\n${source(lesson)}"
    }

    private fun answerCheck(question: String, lesson: ContentLesson): String {
        val rule = answerRuleFor(question, lesson)
            ?: return askForSupportedExample(lesson)
        return if (matchesAnswer(question, rule.answer)) {
            "Yes. The verified answer is ${rule.answer}. ${rule.explanation}"
        } else {
            "Not yet. The verified answer is ${rule.answer}. ${rule.explanation}"
        }
    }

    private fun directAnswer(question: String, lesson: ContentLesson): String {
        Grade3NumericRules.answerFor(lesson, question)?.let { answer -> return answer }
        val rule = answerRuleFor(question, lesson)
            ?: return askForSupportedExample(lesson)
        return "The answer is ${rule.answer}. ${rule.explanation}"
    }

    private fun definition(lesson: ContentLesson): String =
        "${lesson.definition}\n\n${lesson.passage}"

    private fun hint(lesson: ContentLesson): String =
        "Hint: ${lesson.teachingSteps.firstOrNull() ?: lesson.definition}"

    private fun practice(lesson: ContentLesson): String = lesson.practicePrompt
        ?.let { "Practice: $it" }
        ?: "Practice: ${lesson.teachingSteps.lastOrNull() ?: lesson.definition}"

    private fun explanation(lesson: ContentLesson): String {
        if (lesson.answerRules.isEmpty()) {
            return "🌟 Let’s explore ${lesson.topic}!\n\n" +
                "I found this lesson in Term ${lesson.term ?: "?"}, Week ${lesson.week ?: "?"}. " +
                "Look for the worked example, then tell me the numbers or shape you can see. " +
                "I’ll help you solve it step by step."
        }
        val steps = lesson.teachingSteps.mapIndexed { index, step -> "${index + 1}. $step" }
            .joinToString(separator = "\n")
        return "Local lesson: ${lesson.topic}\n\n${lesson.passage}\n\nTry this:\n$steps"
    }

    private fun story(lesson: ContentLesson): String = when (lesson.id) {
        "g3-t1-w2-l1-counting-twos" ->
            "✨ Amina puts 2 mangoes in each little basket: 2, 4, 6, 8. She adds two mangoes each time, so she is counting in twos!"
        "g3-t1-w2-l2-tens-ones" ->
            "✨ Juma has 3 bags with 10 football stickers in each bag and 6 loose stickers. That is 3 tens and 6 ones — 36 stickers!"
        else -> "✨ Imagine you are a maths explorer. Use the example in ${lesson.topic} as your clue, take one small step, and celebrate when the pattern makes sense!"
    }

    private fun answerRuleFor(question: String, lesson: ContentLesson): LessonAnswerRule? {
        val tokens = tokens(question)
        return lesson.answerRules
            .filter { rule -> rule.evidenceTerms.all { evidence -> tokens.contains(evidence.lowercase()) } }
            .maxByOrNull { it.evidenceTerms.size }
    }

    private fun matchesAnswer(question: String, expected: String): Boolean {
        val expectedTokens = tokens(expected)
        return expectedTokens.isNotEmpty() && expectedTokens.all(tokens(question)::contains)
    }

    private fun intentFor(question: String): Intent {
        val normalized = question.lowercase()
        return when {
            tokens(normalized).any { it in GREETINGS } -> Intent.GREETING
            tokens(normalized).any { it in GOODBYES } -> Intent.GOODBYE
            normalized.contains("what can you") || normalized.contains("help me") || normalized == "help" -> Intent.CAPABILITIES
            normalized.contains("correct") || normalized.contains("right answer") || normalized.startsWith("is ") -> Intent.ANSWER_CHECK
            normalized.contains("hint") -> Intent.HINT
            normalized.contains("practice") || normalized.contains("quiz") || normalized.contains("try") -> Intent.PRACTICE
            normalized.contains("story") -> Intent.STORY
            normalized.startsWith("what does") || normalized.startsWith("what is a ") ||
                normalized.startsWith("what is an ") || " mean" in normalized -> Intent.DEFINITION
            normalized.startsWith("what is") || normalized.startsWith("what are") ||
                normalized.startsWith("how many") || normalized.startsWith("what comes") -> Intent.DIRECT_ANSWER
            else -> Intent.EXPLAIN
        }
    }

    private fun tokens(value: String): Set<String> = value.lowercase()
        .split(Regex("[^a-z0-9]+"))
        .filter(String::isNotBlank)
        .toSet()

    private fun askForSupportedExample(lesson: ContentLesson): String =
        "I can explain ${lesson.topic}, but I only give exact answers for the examples in this lesson. " +
            (lesson.practicePrompt ?: "Ask for a hint or an explanation.")

    private fun greeting(): String =
        "Hello! I am your offline Grade 3 Maths tutor. Ask me to explain a topic, give a hint, or start practice."

    private fun goodbye(): String =
        "Goodbye! Your Grade 3 Maths lesson pack stays on this device."

    private fun capabilities(): String =
        "I can help with Grade 3 Maths across Terms 1, 2 and 3. Ask a number question, say ‘give me a hint’, or ask for a practice challenge."

    private enum class Intent {
        GREETING, GOODBYE, CAPABILITIES, ANSWER_CHECK, HINT, PRACTICE, STORY, DEFINITION, DIRECT_ANSWER, EXPLAIN,
    }

    private fun source(lesson: ContentLesson): String =
        lesson.sourceLabel()

    private fun unsupportedSubject(): String =
        "This demo currently supports Grade 3 Mathematics only."

    private fun unsupportedQuestion(): String =
        "I do not have a rule for that yet. Try the topic name, a number pattern, or say ‘Give me a challenge’."

    private val GREETINGS = setOf("hello", "hi", "hey")
    private val GOODBYES = setOf("bye", "goodbye", "thanks", "thank")
}
