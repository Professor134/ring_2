// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\tasks\TasksViewModel.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.domain.usecase.CompleteTaskUseCase
import com.example.ringapp.domain.usecase.DeleteTaskUseCase
import com.example.ringapp.domain.usecase.ObserveTasksUseCase
import com.example.ringapp.domain.usecase.ObserveUserProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Handles the tasks ui state logic for this component without modifying runtime behavior.
// Represents the tasks ui state structure used by the application.
data class TasksUiState(val tasks: List<TaskEntity> = emptyList(), val points: Int = 0, val loading: Boolean = true, val error: String? = null)

@HiltViewModel
// Handles the tasks view model logic for this component without modifying runtime behavior.
// Represents the tasks view model structure used by the application.
class TasksViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    observeProgress: ObserveUserProgressUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val deleteTask: DeleteTaskUseCase
) : ViewModel() {
    private val _userError = MutableStateFlow<String?>(null)

    val state: StateFlow<TasksUiState> = combine(observeTasks(), observeProgress(), _userError) { tasks, progress, err -> TasksUiState(tasks, progress?.currentPoints ?: 0, false, err) }
        .catch { emit(TasksUiState(loading = false, error = it.message ?: "Unable to load tasks")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

// Manages the toggle flow without changing runtime behavior.
    fun toggle(task: TaskEntity) = viewModelScope.launch {
        try {
            _userError.value = null
            completeTask(task)
        } catch (e: Exception) {
            _userError.value = e.message ?: "Unable to complete task"
        }
    }
// Manages the delete flow without changing runtime behavior.
    fun delete(task: TaskEntity) = viewModelScope.launch { deleteTask(task.id) }
}
