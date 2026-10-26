package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(task: TaskEntity) = repository.update(task.copy(updatedAt = System.currentTimeMillis()))
}