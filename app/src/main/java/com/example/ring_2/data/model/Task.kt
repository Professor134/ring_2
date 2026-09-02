package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskRepeatType {
    NONE, WEEKLY, MONTHLY, YEARLY
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
    val repeatType: TaskRepeatType = TaskRepeatType.NONE,
    val repeatDayOfWeek: Int? = null, // 1=Sun, 7=Sat
    val repeatDayOfMonth: Int? = null,
    val repeatMonth: Int? = null, // 0-based like Calendar
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val completionCycle: Int = 1,
    val reminderEnabled: Boolean = false,
    val notificationId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
