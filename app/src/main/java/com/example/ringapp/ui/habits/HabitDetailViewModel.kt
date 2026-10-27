// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\habits\HabitDetailViewModel.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
// Handles the habit detail view model logic for this component without modifying runtime behavior.
// Represents the habit detail view model structure used by the application.
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDetail: ObserveHabitDetailUseCase,
    private val toggleHabit: ToggleHabitUseCase,
    private val deleteHabit: DeleteHabitUseCase
) : ViewModel() {
    private val habitId: Long = checkNotNull(savedStateHandle.get<String>("habitId")).toLong()
    private val _range = MutableStateFlow(30)
    val selectedRange: StateFlow<Int> = _range.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HabitDetailData?> = _range.flatMapLatest { days -> observeDetail(habitId, days) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

// Manages the set range flow without changing runtime behavior.
    fun setRange(days: Int) { _range.value = days }
// Manages the complete today flow without changing runtime behavior.
    fun completeToday() = viewModelScope.launch { 
        state.value?.habit?.let { toggleHabit(it) }
    }
// Manages the delete flow without changing runtime behavior.
    fun delete() = viewModelScope.launch { deleteHabit(habitId) }
}
