package com.example.educloud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.data.repository.StudentRepository
import com.example.educloud.sync.MAX_INTEREST_CHIPS
import com.example.educloud.sync.sanitizeInterestDomains
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val interests: Set<String> = emptySet(),
)

class ProfileViewModel(
    private val studentRepository: StudentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.interestDomains.collect { domains ->
                _state.update { it.copy(interests = domains.toSet()) }
            }
        }
    }

    fun toggleInterest(domain: String) {
        val current = _state.value.interests
        val updated = when {
            domain in current -> current - domain
            current.size < MAX_INTEREST_CHIPS -> current + domain
            else -> current
        }
        viewModelScope.launch {
            studentRepository.saveInterestDomains(sanitizeInterestDomains(updated))
        }
    }
}
