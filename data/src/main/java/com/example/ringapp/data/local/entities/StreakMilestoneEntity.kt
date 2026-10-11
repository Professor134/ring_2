// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\StreakMilestoneEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streak_milestone",
    foreignKeys = [
        ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = StreakCycleEntity::class, parentColumns = ["id"], childColumns = ["cycleId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("habitId"),
        Index("cycleId"),
        Index(value = ["habitId", "cycleId", "milestoneLength"], unique = true)
    ]
)
// Handles the streak milestone entity logic for this component without modifying runtime behavior.
// Represents the streak milestone entity structure used by the application.
data class StreakMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val cycleId: Long,
    val milestoneLength: Int,
    val achievedAt: Long,
    val pointsAwarded: Int,
    val createdAt: Long,
    val updatedAt: Long
)
