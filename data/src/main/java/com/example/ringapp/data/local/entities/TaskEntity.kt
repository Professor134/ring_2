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
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)
