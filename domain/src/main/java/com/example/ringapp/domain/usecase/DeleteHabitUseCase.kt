// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\DeleteHabitUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import javax.inject.Inject

// Handles the delete habit use case logic for this component without modifying runtime behavior.
// Represents the delete habit use case structure used by the application.
class DeleteHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long) = repository.delete(habitId, System.currentTimeMillis())
}