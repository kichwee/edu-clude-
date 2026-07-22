package com.example.educloud.content

/**
 * Small, inspectable calculation rules for common Grade 3 questions. These
 * rules run before retrieval when a learner asks a direct number question, so
 * a correct answer never depends on an LLM or a lucky keyword match.
 */
internal object Grade3MathRules {
    fun answerFor(question: String): String? {
        val normalized = question.lowercase()
        simpleCalculation(normalized)?.let { return it }
        evenOdd(normalized)?.let { return it }
        successorPredecessor(normalized)?.let { return it }
        comparison(normalized)?.let { return it }
        fractionsAndOperations(normalized)?.let { return it }
        curriculumFacts(normalized)?.let { return it }
        placeValue(normalized)?.let { return it }
        pattern(normalized)?.let { return it }
        return null
    }

    private fun simpleCalculation(question: String): String? {
        val match = CALCULATION.find(question) ?: return null
        val first = match.groupValues[1].toIntOrNull() ?: return null
        val operation = match.groupValues[2]
        val second = match.groupValues[3].toIntOrNull() ?: return null
        if (first > MAX_VALUE || second > MAX_VALUE) return null

        return when (operation) {
            "+", "plus", "add" -> "🎉 $first + $second = ${first + second}. Put the two groups together!"
            "-", "minus", "subtract" -> "🎉 $first − $second = ${first - second}. Take $second away from $first."
            "x", "×", "times", "multiplied by" -> "🎉 $first × $second = ${first * second}. Think of $first groups of $second."
            "÷", "/", "divided by" -> {
                if (second == 0 || first % second != 0) null
                else "🎉 $first ÷ $second = ${first / second}. Share $first equally into $second groups."
            }
            else -> null
        }
    }

    private fun evenOdd(question: String): String? {
        if (!(question.contains("even") || question.contains("odd"))) return null
        val num = NUMBER.find(question)?.value?.toIntOrNull() ?: return null
        return if (num % 2 == 0) {
            "🎉 $num is an EVEN number! You can share it equally into 2 groups."
        } else {
            "🎉 $num is an ODD number! When shared into 2 groups, 1 is left over."
        }
    }

    private fun successorPredecessor(question: String): String? {
        val numbers = NUMBER.findAll(question).mapNotNull { it.value.toIntOrNull() }.toList()
        if (numbers.size != 1) return null
        val num = numbers[0]
        return when {
            question.contains("after") || question.contains("next") -> "🎉 The number after $num is ${num + 1}."
            question.contains("before") -> "🎉 The number before $num is ${num - 1}."
            else -> null
        }
    }

    private fun comparison(question: String): String? {
        val nums = NUMBER.findAll(question).mapNotNull { it.value.toIntOrNull() }.take(2).toList()
        if (nums.size < 2) return null
        val (a, b) = nums[0] to nums[1]
        return when {
            question.contains("bigger") || question.contains("greater") || question.contains("larger") -> {
                val winner = if (a >= b) a else b
                "🎉 $winner is bigger! ($a vs $b)"
            }
            question.contains("smaller") || question.contains("less") -> {
                val winner = if (a <= b) a else b
                "🎉 $winner is smaller! ($a vs $b)"
            }
            else -> null
        }
    }

    private fun fractionsAndOperations(question: String): String? {
        val num = NUMBER.find(question)?.value?.toIntOrNull() ?: return null
        return when {
            question.contains("half") -> "🎉 Half of $num is ${num / 2}."
            question.contains("double") -> "🎉 Double $num is ${num * 2}."
            question.contains("quarter") -> {
                if (num % 4 == 0) "🎉 A quarter of $num is ${num / 4}."
                else null
            }
            else -> null
        }
    }

    private fun curriculumFacts(question: String): String? {
        return when {
            question.contains("minute") && question.contains("hour") -> "🎉 There are 60 minutes in 1 hour."
            question.contains("hour") && question.contains("day") -> "🎉 There are 24 hours in 1 day."
            question.contains("cent") || question.contains("shilling") -> "🎉 100 cents equal 1 Kenyan shilling."
            question.contains("triangle") -> "🎉 A triangle has 3 sides and 3 corners."
            question.contains("square") -> "🎉 A square has 4 equal sides."
            question.contains("rectangle") -> "🎉 A rectangle has 4 sides (2 long sides and 2 short sides)."
            question.contains("circle") -> "🎉 A circle has 1 round curved side and 0 corners."
            else -> null
        }
    }

    private fun placeValue(question: String): String? {
        if (!(question.contains("tens") && question.contains("ones"))) return null
        val number = NUMBER.findAll(question).lastOrNull()?.value?.toIntOrNull() ?: return null
        if (number !in 0..999) return null
        val tens = (number % 100) / 10
        val ones = number % 10
        return if (number < 100) {
            "🎉 $number has $tens tens and $ones ones."
        } else {
            val hundreds = number / 100
            "🎉 $number has $hundreds hundreds, $tens tens and $ones ones."
        }
    }

    private fun pattern(question: String): String? {
        if (!PATTERN_WORDS.any(question::contains)) return null
        val values = NUMBER.findAll(question).mapNotNull { it.value.toIntOrNull() }.toList()
        if (values.size < 3 || values.any { it > MAX_VALUE }) return null
        val differences = values.zipWithNext { first, second -> second - first }
        val step = differences.firstOrNull() ?: return null
        if (step == 0 || step !in -100..100 || differences.any { it != step }) return null
        val next = values.last() + step
        val action = if (step > 0) "add $step" else "take away ${-step}"
        return "🎉 The next number is $next. Each time, $action."
    }

    private const val MAX_VALUE = 10_000
    private val NUMBER = Regex("\\d{1,5}")
    private val CALCULATION = Regex(
        "(?<!\\d)(\\d{1,5})\\s*(\\+|-|×|x|÷|/|plus|minus|add|subtract|times|multiplied by|divided by)\\s*(\\d{1,5})(?!\\d)",
    )
    private val PATTERN_WORDS = setOf("next", "comes", "continue", "pattern", "missing")
}
