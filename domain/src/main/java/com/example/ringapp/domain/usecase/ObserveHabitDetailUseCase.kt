// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObserveHabitDetailUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

// Handles the habit detail data logic for this component without modifying runtime behavior.
// Represents the habit detail data structure used by the application.
data class HabitDetailData(val habit: HabitEntity, val progress: List<HabitProgressEntity>)

// Represents the observe habit detail use case structure used by the application.
class ObserveHabitDetailUseCase @Inject constructor(private val repository: HabitRepository) {
    operator fun invoke(habitId: Long, rangeDays: Int): Flow<HabitDetailData?> {
        val end = startOfDay(System.currentTimeMillis())
        val start = end - rangeDays * DAY_MILLIS
        return combine(repository.observeById(habitId), repository.observeProgress(habitId, start, end)) { habit, progress ->
            habit?.let { HabitDetailData(it, progress) }
        }
    }

// Manages the start of day flow without changing runtime behavior.
    private fun startOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    companion object { private const val DAY_MILLIS = 86_400_000L }
}