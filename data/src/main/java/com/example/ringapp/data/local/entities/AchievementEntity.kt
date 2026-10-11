// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\AchievementEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
// Handles the achievement entity logic for this component without modifying runtime behavior.
// Represents the achievement entity structure used by the application.
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: AchievementCategory,
    val threshold: Int,
    val description: String,
    val icon: String? = null,
    val pointsAwarded: Int,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val currentProgress: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
