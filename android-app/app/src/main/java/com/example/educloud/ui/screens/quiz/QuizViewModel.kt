package com.example.educloud.ui.screens.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.ai.IrtEngine
import com.example.educloud.data.model.Interaction
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.habit.HabitEngine
import com.example.educloud.sync.CloudSyncWorker
import com.example.educloud.sync.HomeworkItemResult
import com.example.educloud.sync.HomeworkService
import com.example.educloud.sync.LearnerToken
import com.example.educloud.sync.TonightAssignment
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val difficulty: Float,
    val explanation: String,
    val skillId: String? = null,
    val itemId: String? = null,
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
    /** Habit-shell ceremony data; arrives shortly after [isFinished] flips true. */
    val lessonResult: LessonResult? = null,
    val itemResults: List<HomeworkItemResult> = emptyList(),
    val learnerToken: String? = null,
)

/** What this lesson earned: mastery XP (D11), streak state and milestones just crossed. */
data class LessonResult(
    val xpGained: Int,
    val currentStreak: Int,
    val newMilestones: List<HabitEngine.Milestone> = emptyList(),
)

class QuizViewModel(
    private val context: Context,
    private val studentRepository: StudentRepository,
    private val learningRepository: LearningRepository,
    private val homeworkService: HomeworkService = HomeworkService(),
) : ViewModel() {

    private val irtEngine = IrtEngine()
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    companion object {
        private const val DEMO_QUESTION_COUNT = 3
        private const val MAX_QUESTION_TIME_MS = 10 * 60 * 1000
    }

    private var questionShownAtMs: Long = 0L

    fun init(subject: String) {
        // Idempotent: LaunchedEffect re-fires on activity recreation and must
        // not wipe an in-flight quiz or the finished ceremony (rotation safety).
        val current = _state.value
        if (current.subject == subject &&
            (current.currentQuestion != null || current.unavailableMessage != null || current.isFinished)
        ) {
            return
        }
        viewModelScope.launch {
            val student = studentRepository.activeStudent.first()
            val theta = 0.0 // TODO: load from DB IRT parameters
            val questionBank = QuizSubjectCatalog.questionsFor(subject)
            _state.value = QuizState(
                subject = subject,
                studentId = student?.id,
                learnerToken = LearnerToken.current(context),
                theta = theta,
                currentQuestion = questionBank?.let { getNextQuestion(it, theta, 0) },
                unavailableMessage = if (questionBank == null) {
                    QuizSubjectCatalog.unavailableMessage(subject)
                } else {
                    null
                },
            )
            if (_state.value.currentQuestion != null) {
                questionShownAtMs = System.currentTimeMillis()
            }
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

        val itemId = current.currentQuestion.itemId
        _state.update { it.copy(
            selectedOption = optionIndex,
            isAnswered = true,
            isCorrect = isCorrect,
            score = if (isCorrect) it.score + 1 else it.score,
            totalAnswered = it.totalAnswered + 1,
            theta = newTheta,
            itemResults = if (itemId.isNullOrBlank()) {
                it.itemResults
            } else {
                it.itemResults + HomeworkItemResult(itemId = itemId, correct = isCorrect)
            },
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
                        timeTakenMs = elapsedMs(),
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
        if (current.unavailableMessage != null || current.isFinished) return
        if (current.totalAnswered >= DEMO_QUESTION_COUNT) {
            _state.update { it.copy(isFinished = true) }
            reportTonightFromClassAttempts()
            // D11: a perfect lesson is the one mastery event a quick-check can pay.
            val studentId = current.studentId
            if (studentId != null && current.totalAnswered > 0) {
                viewModelScope.launch {
                    try {
                        val before = learningRepository.getStreakOnce(studentId)
                            ?.let { HabitEngine.Totals(it.currentStreak, it.totalXp.toInt(), it.lessonsPassed) }
                            ?: HabitEngine.Totals(0, 0, 0)
                        learningRepository.updateStreak(studentId)
                        val perfect = current.score == current.totalAnswered
                        if (perfect) {
                            learningRepository.addMasteryXp(studentId, HabitEngine.XpEvent.LESSON_PASSED)
                        }
                        val after = learningRepository.getStreakOnce(studentId)
                        _state.update { state ->
                            state.copy(
                                lessonResult = LessonResult(
                                    xpGained = if (perfect) HabitEngine.xpFor(HabitEngine.XpEvent.LESSON_PASSED) else 0,
                                    currentStreak = after?.currentStreak ?: 1,
                                    newMilestones = after?.let { s ->
                                        HabitEngine.newlyReached(
                                            before,
                                            HabitEngine.Totals(s.currentStreak, s.totalXp.toInt(), s.lessonsPassed),
                                        )
                                    } ?: emptyList(),
                                ),
                            )
                        }
                    } catch (ignored: Exception) {
                        // Offline-first: a failed award must never crash the celebration;
                        // totals stay at their last honest values and the next lesson retries.
                    }
                }
            }
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
        questionShownAtMs = System.currentTimeMillis()
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

    private fun reportTonightFromClassAttempts() {
        val pack = TonightAssignment.current ?: return
        val snapshot = _state.value
        val token = snapshot.learnerToken ?: return
        val results = snapshot.itemResults.filter { result -> result.itemId in pack.itemIds }
        if (results.isEmpty()) return
        viewModelScope.launch {
            homeworkService.submitAttempts(pack, results, token)
        }
    }

    private fun elapsedMs(): Int {
        if (questionShownAtMs <= 0L) return 0
        val elapsed = System.currentTimeMillis() - questionShownAtMs
        return elapsed.coerceIn(0L, MAX_QUESTION_TIME_MS.toLong()).toInt()
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

