// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\TaskEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["parentTaskId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("parentTaskId")]
)
// Handles the task entity logic for this component without modifying runtime behavior.
// Represents the task entity structure used by the application.
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: TaskPriority,
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val repeatType: RepeatType = RepeatType.NONE,
    val repeatInterval: Int = 1,
    val repeatDaysOfWeek: String? = null,
    val repeatDayOfMonth: Int? = null,
    val repeatMonth: Int? = null,
    val repeatEndDate: Long? = null,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val reminderEnabled: Boolean = false,
    val reminderTime: Long? = null,
    val parentTaskId: Long? = null,
    val subtasks: List<SubtaskItem>? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)
