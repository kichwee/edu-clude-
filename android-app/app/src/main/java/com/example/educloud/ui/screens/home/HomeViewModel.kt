package com.example.educloud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.data.model.Streak
import com.example.educloud.data.model.Student
import com.example.educloud.data.repository.LearningRepository
import com.example.educloud.data.repository.StudentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeState(
    val student: Student? = null,
    val streak: Streak? = null,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val studentRepository: StudentRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.activeStudent.collect { student ->
                _state.update { it.copy(student = student, isLoading = student == null) }
                student?.id?.let { id ->
                    learningRepository.getStreak(id).collect { streak ->
                        _state.update { it.copy(streak = streak) }
                    }
                }
            }
        }
    }
}
