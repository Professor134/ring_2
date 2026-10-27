// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\CompleteHabitUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.local.entities.HabitEntity
import java.util.Calendar
import com.example.ringapp.domain.engine.ScheduleEngine
import javax.inject.Inject

// Handles the complete habit use case logic for this component without modifying runtime behavior.
// Represents the complete habit use case structure used by the application.
class CompleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: HabitEntity) {
        val now = System.currentTimeMillis()
        val today = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayDate = java.time.Instant.ofEpochMilli(today).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        check(ScheduleEngine.isActiveOnDate(habit, todayDate)) { "This habit is not scheduled today" }
        repository.recordProgress(habit, today, habit.target, null)
    }
}