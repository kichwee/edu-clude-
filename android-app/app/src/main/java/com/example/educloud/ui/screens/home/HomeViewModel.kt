package com.example.educloud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.data.model.Streak
import com.example.educloud.data.model.Student
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.habit.HabitEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeState(
    val student: Student? = null,
    val streak: Streak? = null,
    val quizAnswersToday: Int = 0,
    val pathNextTitle: String? = null,
    val isLoading: Boolean = true,
) {
    val dailyGoalMet: Boolean get() = HabitEngine.dailyQuizGoalMet(quizAnswersToday)
    val dailyGoalTarget: Int get() = HabitEngine.DAILY_QUIZ_GOAL
}

class HomeViewModel(
    private val studentRepository: StudentRepository,
    private val learningRepository: LearningRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.activeStudent.collectLatest { student ->
                if (student == null) {
                    _state.value = HomeState(isLoading = false)
                    return@collectLatest
                }
                val todayStart = LearningRepository.startOfLocalDayMillis(System.currentTimeMillis())
                combine(
                    learningRepository.getStreak(student.id),
                    learningRepository.observeQuizAnswersSince(student.id, todayStart),
                    learningRepository.getRecentInteractions(student.id),
                ) { streak, answersToday, interactions ->
                    val path = Grade3LearningPath.from(
                        interactions,
                        lessonsPassed = streak?.lessonsPassed ?: 0,
                    )
                    HomeState(
                        student = student,
                        streak = streak,
                        quizAnswersToday = answersToday,
                        pathNextTitle = Grade3LearningPath.nextOpenTitle(path),
                        isLoading = false,
                    )
                }.collect { _state.value = it }
            }
        }
    }
}
