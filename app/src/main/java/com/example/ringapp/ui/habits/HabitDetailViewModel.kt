package com.example.ringapp.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.domain.usecase.CompleteHabitUseCase
import com.example.ringapp.domain.usecase.DeleteHabitUseCase
import com.example.ringapp.domain.usecase.HabitDetailData
import com.example.ringapp.domain.usecase.ObserveHabitDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDetail: ObserveHabitDetailUseCase,
    private val completeHabit: CompleteHabitUseCase,
    private val deleteHabit: DeleteHabitUseCase
) : ViewModel() {
    private val habitId: Long = checkNotNull(savedStateHandle.get<String>("habitId")).toLong()
    private val range = MutableStateFlow(30)
    val selectedRange: StateFlow<Int> = range
    val state: StateFlow<HabitDetailData?> = range.flatMapLatest { observeDetail(habitId, it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setRange(days: Int) { range.value = days }

    fun completeToday() {
        state.value?.habit?.let { habit -> viewModelScope.launch { completeHabit(habit) } }
    }

    fun delete() { viewModelScope.launch { deleteHabit(habitId) } }
}