// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\CreateTaskUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.*
import com.example.ringapp.data.repository.TaskRepository
import javax.inject.Inject

// Handles the create task use case logic for this component without modifying runtime behavior.
// Represents the create task use case structure used by the application.
class CreateTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(title: String, description: String, priority: TaskPriority, dueDate: Long?, dueTime: Long?, repeatType: RepeatType, reminderEnabled: Boolean, repeatInterval: Int = 1, repeatDaysOfWeek: String? = null, repeatDayOfMonth: Int? = null, repeatMonth: Int? = null, subtasks: List<SubtaskItem>? = null): Long {
        val now = System.currentTimeMillis()
        return repository.create(TaskEntity(title = title, description = description.ifBlank { null }, priority = priority, dueDate = dueDate, dueTime = dueTime, repeatType = repeatType, repeatInterval = repeatInterval.coerceAtLeast(1), repeatDaysOfWeek = repeatDaysOfWeek, repeatDayOfMonth = repeatDayOfMonth, repeatMonth = repeatMonth, reminderEnabled = reminderEnabled, subtasks = subtasks, createdAt = now, updatedAt = now))
    }
}
