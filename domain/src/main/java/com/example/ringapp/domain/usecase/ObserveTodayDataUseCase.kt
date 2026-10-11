// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObserveTodayDataUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.repository.TaskRepository
import com.example.ringapp.domain.engine.ScheduleEngine
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

// Handles the today data logic for this component without modifying runtime behavior.
// Represents the today data structure used by the application.
data class TodayData(val progress: List<com.example.ringapp.data.local.entities.HabitProgressEntity>, val tasks: List<com.example.ringapp.data.local.entities.TaskEntity>)

// Represents the observe today data use case structure used by the application.
class ObserveTodayDataUseCase @Inject constructor(private val habits: HabitRepository, private val tasks: TaskRepository) {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<TodayData> {
        val today = LocalDate.now()
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return combine(habits.observeProgressForDate(start), tasks.observeActive()) { progress, allTasks ->
            val todayTasks = allTasks.filter { ScheduleEngine.isActiveOnDate(it, today) }
            TodayData(progress, todayTasks)
        }
    }
}
