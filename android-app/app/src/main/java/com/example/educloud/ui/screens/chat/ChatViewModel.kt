package com.example.educloud.ui.screens.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.ai.TutorResponseEngine
import com.example.educloud.data.model.Interaction
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.OfflineLessonRepository
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.content.sourceLabel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long,
    val text: String,
    val isFromUser: Boolean,
    val isStreaming: Boolean = false
)

data class ChatState(
    val subject: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isThinking: Boolean = false,
    val isOomMode: Boolean = false,
    val forceRetrievalOnly: Boolean = false,
    val isOpenAiConnected: Boolean = false,
    val studentId: Int? = null,
    val inputError: String? = null,
)

class ChatViewModel(
    private val context: Context,
    private val studentRepository: StudentRepository,
    private val learningRepository: LearningRepository,
    private val offlineLessonRepository: OfflineLessonRepository,
) : ViewModel() {

    private val tutorResponseEngine = TutorResponseEngine(context)
    private var nextMessageId = 0L

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun init(subject: String) {
        viewModelScope.launch {
            val student = studentRepository.activeStudent.first()
            val isOpenAiActive = com.example.educloud.ai.OpenAiTutorService.checkApiHealth()
            val greeting = "Hi ${student?.alias ?: "friend"}! 🌟 I’m your Grade 3 Maths buddy. Ask me a number question, try a challenge, or ask for a story. I can explore lessons from Terms 1, 2 and 3 with you."

            _state.value = ChatState(
                subject = subject,
                studentId = student?.id,
                isOomMode = tutorResponseEngine.isLowMemory,
                isOpenAiConnected = isOpenAiActive,
                messages = listOf(ChatMessage(newMessageId(), greeting, isFromUser = false))
            )
        }
    }

    fun onInputChange(text: String) {
        val boundedText = text.take(MAX_QUESTION_LENGTH)
        _state.update {
            it.copy(
                inputText = boundedText,
                inputError = if (text.length > MAX_QUESTION_LENGTH) {
                    "Questions can be up to $MAX_QUESTION_LENGTH characters."
                } else {
                    null
                },
            )
        }
    }

    fun setForceRetrievalOnly(enabled: Boolean) {
        _state.update { it.copy(forceRetrievalOnly = enabled) }
    }

    fun sendSuggestedMessage(message: String) {
        if (_state.value.isThinking) return
        _state.update { it.copy(inputText = message, inputError = null) }
        sendMessage()
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isThinking || _state.value.inputError != null) return

        val studentId = _state.value.studentId
        if (studentId == null) {
            _state.update { it.copy(inputError = "Finish learner setup before asking a question.") }
            return
        }
        val subject = _state.value.subject

        // Add user message
        _state.update { it.copy(
            messages = it.messages + ChatMessage(newMessageId(), text, isFromUser = true),
            inputText = "",
            isThinking = true,
            inputError = null,
        )}

        viewModelScope.launch {
            // Add loading bubble
            _state.update { it.copy(
                messages = it.messages + ChatMessage(newMessageId(), "", isFromUser = false, isStreaming = true)
            )}

            try {
                val lesson = offlineLessonRepository.retrieve(text, subject, 3, studentId)
                var fullResponse = ""
                tutorResponseEngine.answer(
                    subject = subject,
                    learnerQuestion = text,
                    lesson = lesson,
                    forceRetrievalOnly = _state.value.forceRetrievalOnly,
                ).collect { partial ->
                    fullResponse = partial
                    _state.update { state ->
                        val messages = state.messages.toMutableList()
                        messages[messages.lastIndex] = messages.last().copy(text = partial, isStreaming = true)
                        state.copy(messages = messages)
                    }
                }

                _state.update { state ->
                    val messages = state.messages.toMutableList()
                    messages[messages.lastIndex] = messages.last().copy(text = fullResponse, isStreaming = false)
                    state.copy(messages = messages)
                }

                learningRepository.recordInteraction(
                    Interaction(
                        studentId = studentId,
                        question = text,
                        aiResponse = fullResponse,
                        ragSources = lesson?.let { "${it.id}|${it.sourceLabel()}" },
                        subject = subject,
                        channel = "app",
                    ),
                )
                learningRepository.updateStreak(studentId)
            } catch (_: Exception) {
                _state.update { state ->
                    val messages = state.messages.toMutableList()
                    messages[messages.lastIndex] = messages.last().copy(
                        text = "The local tutor could not answer just now. Please try again.",
                        isStreaming = false,
                    )
                    state.copy(messages = messages, inputError = "Your question was not saved. Try again.")
                }
            } finally {
                _state.update { it.copy(isThinking = false) }
            }
        }
    }

    private fun newMessageId(): Long = nextMessageId++

    private companion object {
        const val MAX_QUESTION_LENGTH = 240
    }
}
