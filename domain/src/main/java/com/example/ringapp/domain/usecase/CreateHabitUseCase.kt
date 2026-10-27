// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\CreateHabitUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitType
import com.example.ringapp.data.local.entities.ScheduleType
import com.example.ringapp.data.repository.HabitRepository
import javax.inject.Inject

// Handles the create habit use case logic for this component without modifying runtime behavior.
// Represents the create habit use case structure used by the application.
class CreateHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(name: String, description: String, categoryId: Long?, type: HabitType, target: Double, unit: String, scheduleType: ScheduleType, scheduleDays: List<Int>, startDate: Long) {
        val now = System.currentTimeMillis()
        repository.create(HabitEntity(name = name, description = description.ifBlank { null }, categoryId = categoryId, type = type, target = target, unit = unit.ifBlank { null }, scheduleType = scheduleType, scheduleDays = scheduleDays, startDate = startDate, color = 0xFF00A84F.toInt(), createdAt = now, updatedAt = now))
    }
}