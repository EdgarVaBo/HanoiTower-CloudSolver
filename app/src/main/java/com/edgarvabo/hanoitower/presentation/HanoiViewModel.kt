package com.edgarvabo.hanoitower.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgarvabo.hanoitower.domain.model.HanoiSolution
import com.edgarvabo.hanoitower.domain.usecase.GetHanoiSolutionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HanoiUiState {
    data object Idle : HanoiUiState()
    data object Loading : HanoiUiState()
    data class Success(val solution: HanoiSolution) : HanoiUiState()
    data class Error(val message: String) : HanoiUiState()
}

@HiltViewModel
class HanoiViewModel @Inject constructor(
    private val getHanoiSolutionUseCase: GetHanoiSolutionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HanoiUiState>(HanoiUiState.Idle)
    val uiState: StateFlow<HanoiUiState> = _uiState.asStateFlow()

    fun resetToIdle() {
        _uiState.value = HanoiUiState.Idle
    }

    fun solveTower(disks: Int) {
        viewModelScope.launch {
            _uiState.value = HanoiUiState.Loading

            getHanoiSolutionUseCase(disks).onSuccess { solution ->
                _uiState.value = HanoiUiState.Success(solution)
            }.onFailure { error ->
                _uiState.value = HanoiUiState.Error(error.message ?: "Error desconocido")
            }
        }
    }
}