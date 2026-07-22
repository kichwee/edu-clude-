package com.example.educloud.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingState(
    val step: Int = 0,               // 0=Name, 1=Grade
    val alias: String = "",
    val grade: Int = 3,
    val languagePref: String = "en",
    val isLoading: Boolean = false,
    val isDone: Boolean = false,
    val error: String? = null
)

class OnboardingViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setAlias(alias: String) {
        _state.value = _state.value.copy(alias = alias)
    }

    fun setGrade(grade: Int) {
        // Grade 3 is the only installed MVP content pack. Keep this guard so
        // a future caller cannot create a profile for unavailable content.
        if (grade == 3) {
            _state.value = _state.value.copy(grade = grade)
        }
    }

    fun nextStep() {
        val current = _state.value
        if (current.step < 1) {
            _state.value = current.copy(step = current.step + 1)
        }
    }

    fun prevStep() {
        val current = _state.value
        if (current.step > 0) {
            _state.value = current.copy(step = current.step - 1)
        }
    }

    fun completeOnboarding() {
        val current = _state.value
        if (current.alias.isBlank()) {
            _state.value = current.copy(error = "Please enter your name")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null)
            try {
                studentRepository.createStudent(
                    alias = current.alias.trim(),
                    grade = 3,
                    languagePref = current.languagePref
                )
                _state.value = _state.value.copy(isLoading = false, isDone = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
