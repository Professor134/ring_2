// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\TaskReminderEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_reminders",
    foreignKeys = [ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("taskId"), Index("triggerAt")]
)
// Handles the task reminder entity logic for this component without modifying runtime behavior.
// Represents the task reminder entity structure used by the application.
data class TaskReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val triggerAt: Long,
    val reminderType: NotificationType = NotificationType.REMINDER,
    val isEnabled: Boolean = true,
    val alarmId: Int? = null,
    val createdAt: Long,
    val updatedAt: Long
)
