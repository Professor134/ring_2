// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\HabitProgressEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_progress",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
// Handles the habit progress entity logic for this component without modifying runtime behavior.
// Represents the habit progress entity structure used by the application.
data class HabitProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: Long,
    val target: Double,
    val actual: Double,
    val percentage: Int,
    val completed: Boolean,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
