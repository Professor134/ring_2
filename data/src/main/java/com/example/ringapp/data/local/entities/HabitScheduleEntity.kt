// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\HabitScheduleEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_schedules",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("habitId")]
)
// Handles the habit schedule entity logic for this component without modifying runtime behavior.
// Represents the habit schedule entity structure used by the application.
data class HabitScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val scheduleType: ScheduleType,
    val interval: Int = 1,
    val daysOfWeek: String? = null,
    val dayOfMonth: Int? = null,
    val month: Int? = null,
    val dayOfYear: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
