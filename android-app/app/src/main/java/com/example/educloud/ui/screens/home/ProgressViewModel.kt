package com.example.educloud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.data.model.Streak
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.data.repository.WeeklyLearningStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressState(
    val streak: Streak? = null,
    val weekly: WeeklyLearningStats = WeeklyLearningStats(),
)

/**
 * Feeds the Progress screen with real Room-derived data (plan §8): the live
 * streak plus rolling-week stats. No hardcoded mock numbers survive here.
 */
class ProgressViewModel(
    private val studentRepository: StudentRepository,
    private val learningRepository: LearningRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.activeStudent.collectLatest { student ->
                if (student == null) {
                    _state.value = ProgressState()
                    return@collectLatest
                }
                combine(
                    learningRepository.getStreak(student.id),
                    learningRepository.observeWeeklyStats(student.id, weekWindowStart()),
                ) { streak, weekly -> ProgressState(streak, weekly) }.collect { state ->
                    _state.value = state
                }
            }
        }
    }

    private companion object {
        const val WEEK_MILLIS = 7L * 86_400_000L

        fun weekWindowStart(): Long = System.currentTimeMillis() - WEEK_MILLIS
    }
}
