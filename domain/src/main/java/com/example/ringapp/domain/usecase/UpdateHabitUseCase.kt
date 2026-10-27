// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\UpdateHabitUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.repository.HabitRepository
import javax.inject.Inject

// Handles the update habit use case logic for this component without modifying runtime behavior.
// Represents the update habit use case structure used by the application.
class UpdateHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: HabitEntity) = repository.update(habit)
}