package com.example.educloud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.data.model.Streak
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.data.repository.WeeklyLearningStats
import com.example.educloud.habit.WeekChart
import com.example.educloud.habit.WeekDayActivity
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProgressState(
    val streak: Streak? = null,
    val weekly: WeeklyLearningStats = WeeklyLearningStats(),
    val weekDays: List<WeekDayActivity> = emptyList(),
    val pathItems: List<PathItem> = emptyList(),
)

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
                val weekStart = System.currentTimeMillis() - WEEK_MILLIS
                combine(
                    learningRepository.getStreak(student.id),
                    learningRepository.observeWeeklyStats(student.id, weekStart),
                    learningRepository.getRecentInteractions(student.id),
                ) { streak, weekly, interactions ->
                    val today = LearningRepository.epochDayOf(System.currentTimeMillis())
                    val activeDays = interactions.mapTo(mutableSetOf()) { interaction ->
                        LearningRepository.epochDayOf(interaction.createdAt)
                    }
                    ProgressState(
                        streak = streak,
                        weekly = weekly,
                        weekDays = WeekChart.lastSevenDays(today, activeDays, Locale.getDefault()),
                        pathItems = Grade3LearningPath.from(
                            interactions,
                            lessonsPassed = streak?.lessonsPassed ?: 0,
                        ),
                    )
                }.collect { _state.value = it }
            }
        }
    }

    private companion object {
        const val WEEK_MILLIS = 7L * 86_400_000L
    }
}
