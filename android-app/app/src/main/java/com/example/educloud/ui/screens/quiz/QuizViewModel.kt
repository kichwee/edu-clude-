package com.example.educloud.ui.screens.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.ai.IrtEngine
import com.example.educloud.data.model.Interaction
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.sync.CloudSyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val difficulty: Float,
    val explanation: String,
    val skillId: String? = null,
)

data class QuizState(
    val subject: String = "",
    val currentQuestion: QuizQuestion? = null,
    val questionIndex: Int = 0,
    val selectedOption: Int? = null,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val score: Int = 0,
    val totalAnswered: Int = 0,
    val theta: Double = 0.0,           // IRT ability estimate
    val isFinished: Boolean = false,
    val studentId: Int? = null,
    val unavailableMessage: String? = null,
    val remediationMessage: String? = null,
)

class QuizViewModel(
    private val context: Context,
    private val studentRepository: StudentRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val irtEngine = IrtEngine()
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    companion object {
        private const val DEMO_QUESTION_COUNT = 3
    }

    fun init(subject: String) {
        viewModelScope.launch {
            val student = studentRepository.activeStudent.first()
            val theta = 0.0 // TODO: load from DB IRT parameters
            val questionBank = QuizSubjectCatalog.questionsFor(subject)
            _state.value = QuizState(
                subject = subject,
                studentId = student?.id,
                theta = theta,
                currentQuestion = questionBank?.let { getNextQuestion(it, theta, 0) },
                unavailableMessage = if (questionBank == null) {
                    QuizSubjectCatalog.unavailableMessage(subject)
                } else {
                    null
                },
            )
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val current = _state.value
        if (current.isAnswered || current.currentQuestion == null) return

        val isCorrect = optionIndex == current.currentQuestion.correctIndex
        val newTheta = irtEngine.updateTheta(
            currentTheta = current.theta,
            isCorrect = isCorrect,
            itemDifficulty = irtEngine.difficultyToB(current.currentQuestion.difficulty)
        )

        _state.update { it.copy(
            selectedOption = optionIndex,
            isAnswered = true,
            isCorrect = isCorrect,
            score = if (isCorrect) it.score + 1 else it.score,
            totalAnswered = it.totalAnswered + 1,
            theta = newTheta
        )}

        // Persist to Room
        current.studentId?.let { studentId ->
            viewModelScope.launch {
                learningRepository.recordInteraction(
                    Interaction(
                        studentId = studentId,
                        question = current.currentQuestion.text,
                        aiResponse = current.currentQuestion.explanation,
                        isCorrect = isCorrect,
                        learnerAnswer = current.currentQuestion.options[optionIndex],
                        subject = current.subject,
                        strand = current.currentQuestion.skillId,
                        difficulty = current.currentQuestion.difficulty,
                        channel = "app"
                    )
                )
            }
        }
    }

    fun nextQuestion() {
        val current = _state.value
        if (current.unavailableMessage != null) return
        if (current.totalAnswered >= DEMO_QUESTION_COUNT) {
            _state.update { it.copy(isFinished = true) }
            return
        }
        val nextIndex = current.questionIndex + 1
        val questionBank = QuizSubjectCatalog.questionsFor(current.subject) ?: return
        val nextQ = getNextQuestion(questionBank, current.theta, nextIndex)
        _state.update { it.copy(
            currentQuestion = nextQ,
            questionIndex = nextIndex,
            selectedOption = null,
            isAnswered = false,
            isCorrect = false
        )}
    }

    /** Queues the only cloud action after an explicit consent dialog in the UI. */
    fun requestPersonalisedRemediation() {
        val state = _state.value
        val studentId = state.studentId ?: return
        if (state.subject != QuizSubjectCatalog.MATHEMATICS_ID || state.score == state.totalAnswered) return
        val queued = CloudSyncWorker.enqueue(context.applicationContext, studentId)
        _state.update {
            it.copy(
                remediationMessage = if (queued) {
                    "Your anonymous maths mistakes are syncing. When the lesson is ready, open the offline Maths tutor and ask about regrouping."
                } else {
                    "The cloud demo is not configured in this build. Your local lessons remain available offline."
                },
            )
        }
    }

    private fun getNextQuestion(
        questionBank: List<QuizQuestion>,
        theta: Double,
        questionIndex: Int,
    ): QuizQuestion {
        val difficulty = irtEngine.selectNextDifficulty(theta)
        return questionBank.getOrNull(questionIndex)
            ?: checkNotNull(questionBank.minByOrNull { Math.abs(it.difficulty - difficulty) })
    }
}

