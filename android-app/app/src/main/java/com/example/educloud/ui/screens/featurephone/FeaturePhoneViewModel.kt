package com.example.educloud.ui.screens.featurephone

import androidx.lifecycle.ViewModel
import com.example.educloud.content.Grade3MathContent
import com.example.educloud.content.Grade3RuleTutorCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FeaturePhoneState(
    val ussdPath: String = "",
    val ussdReply: String = FeaturePhoneDemoLogic.ussdReply(""),
    val ussdInput: String = "",
)

/** Offline rendering of the same Grade 3 rule cards used by the backend USSD adapter. */
object FeaturePhoneDemoLogic {
    fun ussdReply(path: String): String {
        val steps = path.split("*").filter(String::isNotBlank)
        if (steps.isEmpty()) return menu(
            "Welcome to EduCloud",
            "1. Grade 3 Maths tutor",
            "2. Quick quiz",
            "3. Exit",
        )
        if (steps == listOf("3")) return "END Thanks for trying EduCloud."
        if (steps[0] == "2") return quizReply(steps)
        if (steps[0] != "1") return "END That option is unavailable. Dial again."
        return grade3Reply(steps.drop(1))
    }

    private fun grade3Reply(steps: List<String>): String {
        val cards = Grade3MathContent.ruleCards
        if (steps.isEmpty()) return menu(
            "Grade 3 Maths tutor",
            *cards.mapIndexed { index, card -> "${index + 1}. ${card.lesson.topic}" }.toTypedArray(),
        )
        val topicIndex = steps.first().toIntOrNull()?.minus(1) ?: -1
        val card = cards.getOrNull(topicIndex) ?: return "END That topic is unavailable. Dial again."
        return when {
            steps.size == 1 -> menu(
                "${card.lesson.topic}\n${card.lesson.passage}\n${card.lesson.source}",
                "1. Hint",
                "2. Practice",
            )
            steps == listOf(steps[0], "1") -> menu("Hint: ${card.hint}", "1. Practice")
            steps == listOf(steps[0], "2") || steps == listOf(steps[0], "1", "1") ->
                menu(card.question, *card.options.toTypedArray())
            steps.size == 3 && steps[1] == "2" -> feedback(card, steps[2])
            steps.size == 4 && steps.subList(1, 3) == listOf("1", "1") -> feedback(card, steps[3])
            else -> "END That lesson has ended. Dial again for another topic."
        }
    }

    private fun feedback(card: Grade3RuleTutorCard, selected: String): String {
        if (selected !in setOf("1", "2", "3")) return "END That answer is unavailable. Dial again."
        return if (selected == card.correctOption) "END Nice work! ${card.correction}"
        else "END Almost. ${card.correction}"
    }

    private fun quizReply(steps: List<String>): String = when {
        steps.size == 1 -> menu("What is 7 + 5?", "1. 11", "2. 12", "3. 13")
        steps.size == 2 -> if (steps[1] == "2") "END Nice work! 7 + 5 = 12." else "END Almost. 7 + 5 = 12."
        else -> "END That quiz has ended. Dial again for another one."
    }

    private fun menu(title: String, vararg options: String): String =
        "CON $title\n${options.joinToString("\n")}" 
}

class FeaturePhoneViewModel : ViewModel() {
    private val _state = MutableStateFlow(FeaturePhoneState())
    val state: StateFlow<FeaturePhoneState> = _state.asStateFlow()

    fun onUssdInput(value: String) = _state.update { it.copy(ussdInput = value.take(1)) }

    fun submitUssd() {
        val choice = _state.value.ussdInput.trim()
        if (choice !in setOf("1", "2", "3")) return
        val nextPath = listOf(_state.value.ussdPath, choice).filter(String::isNotBlank).joinToString("*")
        _state.update { it.copy(ussdPath = nextPath, ussdInput = "", ussdReply = FeaturePhoneDemoLogic.ussdReply(nextPath)) }
    }

    fun resetUssd() = _state.update { it.copy(ussdPath = "", ussdInput = "", ussdReply = FeaturePhoneDemoLogic.ussdReply("")) }
}
