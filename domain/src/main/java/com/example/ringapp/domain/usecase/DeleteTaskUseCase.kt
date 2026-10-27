// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\DeleteTaskUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.TaskRepository
import javax.inject.Inject

// Handles the delete task use case logic for this component without modifying runtime behavior.
// Represents the delete task use case structure used by the application.
class DeleteTaskUseCase @Inject constructor(private val repository: TaskRepository) { suspend operator fun invoke(taskId: Long) = repository.delete(taskId, System.currentTimeMillis()) }