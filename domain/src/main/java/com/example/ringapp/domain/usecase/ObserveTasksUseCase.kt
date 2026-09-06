package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<TaskEntity>> = repository.observeActive()
}
