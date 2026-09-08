package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.TaskRepository
import com.example.ringapp.data.local.entities.TaskEntity
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: TaskEntity) {
        if (task.completed) {
            repository.update(task.copy(completed = false, completedAt = null, updatedAt = System.currentTimeMillis()))
            return
        }
        val now = System.currentTimeMillis()
        repository.update(task.copy(completed = true, completedAt = now, updatedAt = now))
        repository.awardTaskPoints(task.id, now)
        if (task.repeatType != com.example.ringapp.data.local.entities.RepeatType.NONE) {
            repository.create(task.copy(id = 0, parentTaskId = task.id, dueDate = nextDate(task.dueDate, task.repeatType, task.repeatInterval), completed = false, completedAt = null, createdAt = now, updatedAt = now))
        }
    }

    private fun nextDate(date: Long?, repeat: com.example.ringapp.data.local.entities.RepeatType, interval: Int): Long? {
        val current = java.time.Instant.ofEpochMilli(date ?: System.currentTimeMillis())
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val next = when (repeat) {
            com.example.ringapp.data.local.entities.RepeatType.DAILY -> current.plusDays(1)
            com.example.ringapp.data.local.entities.RepeatType.WEEKLY -> current.plusWeeks(1)
            com.example.ringapp.data.local.entities.RepeatType.MONTHLY -> current.plusMonths(1)
            com.example.ringapp.data.local.entities.RepeatType.YEARLY -> current.plusYears(1)
            com.example.ringapp.data.local.entities.RepeatType.CUSTOM -> current.plusDays(interval.coerceAtLeast(1).toLong())
            else -> current
        }
        return next.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}