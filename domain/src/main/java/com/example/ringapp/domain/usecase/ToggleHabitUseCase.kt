// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ToggleHabitUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.domain.engine.ScheduleEngine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

// Handles the toggle habit use case logic for this component without modifying runtime behavior.
// Represents the toggle habit use case structure used by the application.
class ToggleHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: HabitEntity) {
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayDate = LocalDate.now()
        check(ScheduleEngine.isActiveOnDate(habit, todayDate)) { "This habit is not scheduled today" }
        repository.toggleHabit(habit, today)
    }
}
