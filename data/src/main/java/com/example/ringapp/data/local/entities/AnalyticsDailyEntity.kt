// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\AnalyticsDailyEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_daily", indices = [Index(value = ["date"], unique = true)])
// Handles the analytics daily entity logic for this component without modifying runtime behavior.
// Represents the analytics daily entity structure used by the application.
data class AnalyticsDailyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val habitScore: Int,
    val taskScore: Int,
    val consistencyScore: Int,
    val streakScore: Int,
    val productivityScore: Int,
    val completionRate: Int,
    val habitCompletions: Int,
    val taskCompletions: Int,
    val activeHabits: Int,
    val activeTasks: Int,
    val createdAt: Long,
    val updatedAt: Long
)
