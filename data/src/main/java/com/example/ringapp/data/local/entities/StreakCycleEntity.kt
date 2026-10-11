// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\StreakCycleEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streak_cycle",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("habitId")]
)
// Handles the streak cycle entity logic for this component without modifying runtime behavior.
// Represents the streak cycle entity structure used by the application.
data class StreakCycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val cycleNumber: Int,
    val startTime: Long,
    val endTime: Long? = null,
    val currentLength: Int,
    val bestLength: Int,
    val breakReason: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
