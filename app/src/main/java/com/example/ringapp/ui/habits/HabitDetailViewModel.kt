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

    fun setRange(days: Int) { _range.value = days }
    fun completeToday() = viewModelScope.launch { 
        state.value?.habit?.let { toggleHabit(it) }
    }
    fun delete() = viewModelScope.launch { deleteHabit(habitId) }
}
