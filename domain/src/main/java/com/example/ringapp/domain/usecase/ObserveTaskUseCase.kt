package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.TaskRepository
import javax.inject.Inject

class ObserveTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(taskId: Long) = repository.observeById(taskId)
}