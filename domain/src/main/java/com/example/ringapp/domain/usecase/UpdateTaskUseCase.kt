// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\UpdateTaskUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.repository.TaskRepository
import javax.inject.Inject

// Handles the update task use case logic for this component without modifying runtime behavior.
// Represents the update task use case structure used by the application.
class UpdateTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(task: TaskEntity) = repository.update(task.copy(updatedAt = System.currentTimeMillis()))
}