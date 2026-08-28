package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: TaskPriority,
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val completionCycle: Int = 1,
    val reminderEnabled: Boolean = false,
    val notificationId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
