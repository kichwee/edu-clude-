package com.example.educloud.ui.screens.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.ai.LlmTutorExplainer
import com.example.educloud.ai.TutorResponseEngine
import com.example.educloud.content.ContentLesson
import com.example.educloud.data.model.Interaction
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.OfflineLessonRepository
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.content.sourceLabel
import com.example.educloud.sync.MAX_LEARNER_QUESTION_CHARS
import com.example.educloud.sync.MAX_SOURCE_EXCERPT_CHARS
import com.example.educloud.sync.ReexplainOutcome
import com.example.educloud.sync.ReexplainRequestPayload
import com.example.educloud.sync.ReexplainService
import com.example.educloud.sync.validatedServerExplanation
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
    val canExplainMyWay: Boolean = false,
    val isExplainingMyWay: Boolean = false,
)

class ChatViewModel(
    private val context: Context,
    private val studentRepository: StudentRepository,
    private val learningRepository: LearningRepository,
    private val offlineLessonRepository: OfflineLessonRepository,
    private val reexplainService: ReexplainService = ReexplainService(),
) : ViewModel() {

    private val tutorResponseEngine = TutorResponseEngine(context)
    private var nextMessageId = 0L
    private var lastLesson: ContentLesson? = null
    private var lastQuestion: String = ""

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
                if (lesson != null) {
                    lastLesson = lesson
                    lastQuestion = text
                    _state.update { it.copy(canExplainMyWay = true) }
                }
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

    /**
     * Layer-2 re-explanation: one tap, one bounded cloud call through the Django
     * service, re-checked by [validatedServerExplanation] against the canonical
     * local lesson. Any failure — offline, throttled, contract-invalid, or no
     * interest chips chosen — falls back to the deterministic story so the
     * learner never hits a dead end. Never a retry loop.
     */
    fun explainMyWay() {
        val lesson = lastLesson
        if (_state.value.isThinking || _state.value.isExplainingMyWay) return
        if (lesson == null) {
            _state.update { it.copy(inputError = "Ask about a lesson first, then I can explain it your way!") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(
                isExplainingMyWay = true,
                messages = it.messages + ChatMessage(newMessageId(), "", isFromUser = false, isStreaming = true),
            )}
            try {
                val deviceId = studentRepository.activeStudent.first()?.deviceId
                val interests = studentRepository.interestDomains.first()
                val outcome = if (deviceId == null || interests.isEmpty()) null else reexplainService.reexplain(
                    ReexplainRequestPayload(
                        consent = true,
                        learnerId = deviceId,
                        lessonId = lesson.id,
                        analogyDomain = interests.first(),
                        sourceExcerpt = lesson.passage.take(MAX_SOURCE_EXCERPT_CHARS),
                        verifiedAnswer = lesson.verifiedAnswer?.take(64),
                        learnerQuestion = lastQuestion.take(MAX_LEARNER_QUESTION_CHARS),
                    ),
                )
                val validated = (outcome as? ReexplainOutcome.Ready)
                    ?.let { validatedServerExplanation(it.payload, lesson) }
                    ?.getOrNull()
                if (validated != null) {
                    replaceLastBubble("$validated\n\n${lesson.sourceLabel()} · ✨ explained your way")
                    recordReexplainInteraction(lesson, validated)
                } else {
                    serveDeterministicStory(lesson)
                }
            } catch (_: Exception) {
                serveDeterministicStory(lesson)
            } finally {
                _state.update { it.copy(isExplainingMyWay = false) }
            }
        }
    }

    private suspend fun serveDeterministicStory(lesson: ContentLesson) {
        replaceLastBubble(
            LlmTutorExplainer.explainGrounded(
                question = lastQuestion.ifBlank { lesson.topic },
                lesson = lesson,
                mode = LlmTutorExplainer.ExplainerMode.STORY,
            )
        )
    }

    private fun replaceLastBubble(text: String) {
        _state.update { state ->
            val messages = state.messages.toMutableList()
            if (messages.isNotEmpty()) {
                messages[messages.lastIndex] = messages.last().copy(text = text, isStreaming = false)
            }
            state.copy(messages = messages)
        }
    }

    private suspend fun recordReexplainInteraction(lesson: ContentLesson, explanation: String) {
        val studentId = _state.value.studentId ?: return
        learningRepository.recordInteraction(
            Interaction(
                studentId = studentId,
                question = "Explain it my way",
                aiResponse = explanation,
                ragSources = "${lesson.id}|${lesson.sourceLabel()}",
                subject = _state.value.subject,
                channel = "app-reexplain",
            ),
        )
    }

    private fun newMessageId(): Long = nextMessageId++

    private companion object {
        const val MAX_QUESTION_LENGTH = 240
    }
}
