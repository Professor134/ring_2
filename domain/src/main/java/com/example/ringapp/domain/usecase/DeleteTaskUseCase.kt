package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(private val repository: TaskRepository) { suspend operator fun invoke(taskId: Long) = repository.delete(taskId, System.currentTimeMillis()) }