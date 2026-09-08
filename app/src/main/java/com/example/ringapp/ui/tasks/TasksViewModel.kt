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

data class TasksUiState(val tasks: List<TaskEntity> = emptyList(), val points: Int = 0, val loading: Boolean = true, val error: String? = null)

@HiltViewModel
class TasksViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    observeProgress: ObserveUserProgressUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val deleteTask: DeleteTaskUseCase
) : ViewModel() {
    val state: StateFlow<TasksUiState> = combine(observeTasks(), observeProgress()) { tasks, progress -> TasksUiState(tasks, progress?.currentPoints ?: 0, false) }
        .catch { emit(TasksUiState(loading = false, error = it.message ?: "Unable to load tasks")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())
    fun toggle(task: TaskEntity) = viewModelScope.launch { completeTask(task) }
    fun delete(task: TaskEntity) = viewModelScope.launch { deleteTask(task.id) }
}
