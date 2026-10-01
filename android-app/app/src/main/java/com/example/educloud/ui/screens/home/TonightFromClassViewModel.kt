package com.example.educloud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educloud.sync.DEMO_HOMEWORK_CLASS_CODE
import com.example.educloud.sync.HomeworkFetchOutcome
import com.example.educloud.sync.HomeworkPack
import com.example.educloud.sync.HomeworkService
import com.example.educloud.sync.TonightAssignment
import com.example.educloud.sync.demoHomeworkPack
import com.example.educloud.sync.normalizeClassCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TonightFromClassState(
    val classCode: String = DEMO_HOMEWORK_CLASS_CODE,
    val pack: HomeworkPack? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isBundledSample: Boolean = false,
)

class TonightFromClassViewModel(
    private val homeworkService: HomeworkService = HomeworkService(),
) : ViewModel() {

    private val _state = MutableStateFlow(TonightFromClassState(pack = TonightAssignment.current))
    val state: StateFlow<TonightFromClassState> = _state.asStateFlow()

    fun onClassCodeChange(value: String) {
        _state.update { it.copy(classCode = value.take(16), error = null) }
    }

    fun useDemoFixture() {
        accept(demoHomeworkPack(), isBundledSample = true)
    }

    fun openClassCode() {
        val code = normalizeClassCode(_state.value.classCode)
        if (code.length < 4) {
            _state.update { it.copy(error = "Enter the class code from the teacher page.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val outcome = homeworkService.fetchPack(code)) {
                is HomeworkFetchOutcome.Ready -> accept(outcome.pack)
                HomeworkFetchOutcome.Disabled,
                HomeworkFetchOutcome.Unavailable,
                HomeworkFetchOutcome.Rejected,
                -> {
                    if (code == DEMO_HOMEWORK_CLASS_CODE) {
                        accept(demoHomeworkPack(), isBundledSample = true)
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "That class code is not on this demo device. Use $DEMO_HOMEWORK_CLASS_CODE or the demo assignment.",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun accept(pack: HomeworkPack, isBundledSample: Boolean = false) {
        TonightAssignment.replace(pack)
        _state.update {
            it.copy(
                pack = pack,
                classCode = pack.classCode,
                isLoading = false,
                error = null,
                isBundledSample = isBundledSample,
            )
        }
    }
}
