package com.example.educloud.content

/**
 * Small, bounded arithmetic rules supported by the Grade 3 MVP source pack.
 * These are deterministic transformations, not model-generated answers.
 */
internal object Grade3NumericRules {

    fun answerFor(lesson: ContentLesson, learnerQuestion: String): String? = when (lesson.id) {
        "g3-t1-w2-l1-counting-twos" -> sequenceAnswer(learnerQuestion)
        "g3-t1-w2-l2-tens-ones" -> placeValueAnswer(learnerQuestion)
        else -> null
    }

    fun lessonIdFor(question: String): String? {
        val sequence = sequence(question) ?: return null
        return if (sequence.step in setOf(2, -2)) "g3-t1-w2-l1-counting-twos" else null
    }

    fun isPlaceValueQuestion(question: String): Boolean {
        val normalized = question.lowercase()
        return "tens" in normalized && "ones" in normalized && numberIn(question) in 10..99
    }

    private fun sequenceAnswer(question: String): String? {
        val sequence = sequence(question) ?: return null
        val next = sequence.last + sequence.step
        val afterNext = next + sequence.step
        if (next !in 0..999 || afterNext !in 0..999) return null
        return "The next two numbers are $next, $afterNext. Keep counting by ${kotlin.math.abs(sequence.step)}."
    }

    private fun placeValueAnswer(question: String): String? {
        if (!isPlaceValueQuestion(question)) return null
        val number = checkNotNull(numberIn(question))
        return "$number is ${number / 10} tens and ${number % 10} ones."
    }

    private fun sequence(question: String): Sequence? {
        val numbers = NUMBER.findAll(question).map { it.value.toIntOrNull() }.filterNotNull().toList()
        if (numbers.size < 3 || numbers.take(3).any { it !in 0..999 }) return null
        val firstStep = numbers[1] - numbers[0]
        if (firstStep !in SUPPORTED_STEPS || numbers[2] - numbers[1] != firstStep) return null
        return Sequence(last = numbers[2], step = firstStep)
    }

    private fun numberIn(question: String): Int? = NUMBER.find(question)?.value?.toIntOrNull()

    private data class Sequence(val last: Int, val step: Int)

    private val NUMBER = Regex("\\b\\d{1,3}\\b")
    private val SUPPORTED_STEPS = setOf(-2, 2)
}
