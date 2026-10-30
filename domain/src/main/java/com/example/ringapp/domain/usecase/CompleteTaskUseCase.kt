// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\CompleteTaskUseCase.kt
// Purpose: Handles main task completion, subtask checks, points awards, and iteration history for repeatable tasks.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.RepeatType
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.repository.TaskRepository
import com.example.ringapp.domain.engine.ScheduleEngine
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: TaskEntity) {
        val now = System.currentTimeMillis()

        // Handle uncompleting a single non-repeatable task
        if (task.completed && task.repeatType == RepeatType.NONE) {
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
            val currentDueDate = task.dueDate?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            } ?: LocalDate.now()

            val baseDate = if (currentDueDate.isAfter(LocalDate.now())) currentDueDate else LocalDate.now()
            val nextDueDate = ScheduleEngine.getNextActiveDueDate(task, baseDate)

            // Archive current iteration into repeatHistory JSON
            val existingHistory = task.repeatHistory?.let {
                try { JSONArray(it) } catch (e: Exception) { JSONArray() }
            } ?: JSONArray()

            val iterationRecord = JSONObject().apply {
                put("iterationDate", task.dueDate ?: now)
                put("completedAt", now)
                put("status", "COMPLETED")
                put("subtasksCount", subtasks?.size ?: 0)
                put("subtasksCompleted", subtasks?.count { it.completed } ?: 0)
            }
            existingHistory.put(iterationRecord)

            val resetSubtasks = subtasks?.map { it.copy(completed = false) }

            repository.update(
                task.copy(
                    dueDate = nextDueDate,
                    dueTime = nextDueDate,
                    completed = false,
                    completedAt = null,
                    repeatHistory = existingHistory.toString(),
                    subtasks = resetSubtasks,
                    updatedAt = now
                )
            )
        } else {
            repository.update(task.copy(completed = true, completedAt = now, updatedAt = now))
        }
    }
}
