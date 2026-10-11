// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\CompleteTaskUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.TaskRepository
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.local.entities.RepeatType
import com.example.ringapp.domain.engine.ScheduleEngine
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

// Handles the complete task use case logic for this component without modifying runtime behavior.
// Represents the complete task use case structure used by the application.
class CompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: TaskEntity) {
        val now = System.currentTimeMillis()
        if (task.completed) {
            repository.update(task.copy(completed = false, completedAt = null, updatedAt = now))
            repository.revokeTaskPoints(task.id, now)
            return
        }

        val subtasks = task.subtasks
        if (!subtasks.isNullOrEmpty() && subtasks.any { !it.completed }) {
            throw IllegalStateException("Please complete all subtasks before completing the task.")
        }

        repository.awardTaskPoints(task.id, now)

        if (task.repeatType != RepeatType.NONE) {
            val currentDueDate = task.dueDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() } ?: LocalDate.now()
            val baseDate = if (currentDueDate.isAfter(LocalDate.now())) currentDueDate else LocalDate.now()
            val nextDueDate = ScheduleEngine.getNextActiveDueDate(task, baseDate)
            val resetSubtasks = subtasks?.map { it.copy(completed = false) }
            repository.update(
                task.copy(
                    dueDate = nextDueDate,
                    dueTime = nextDueDate,
                    completed = false,
                    completedAt = null,
                    subtasks = resetSubtasks,
                    updatedAt = now
                )
            )
        } else {
            repository.update(task.copy(completed = true, completedAt = now, updatedAt = now))
        }
    }
}
