package com.example.ringapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.domain.usecase.ObserveHabitsUseCase
import com.example.ringapp.domain.usecase.ObserveTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHabits: ObserveHabitsUseCase,
    observeTasks: ObserveTasksUseCase
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(observeHabits(), observeTasks()) { habits, tasks ->
        HomeUiState(habits, tasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

data class HomeUiState(
    val habits: List<HabitEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList()
)
