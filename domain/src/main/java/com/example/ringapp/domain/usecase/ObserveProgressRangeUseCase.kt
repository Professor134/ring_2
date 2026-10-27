// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObserveProgressRangeUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Handles the observe progress range use case logic for this component without modifying runtime behavior.
// Represents the observe progress range use case structure used by the application.
class ObserveProgressRangeUseCase @Inject constructor(private val repository: HabitRepository) {
    operator fun invoke(from: Long, to: Long): Flow<List<HabitProgressEntity>> = repository.observeProgressRange(from, to)
}