// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\UserProgressEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
// Handles the user progress entity logic for this component without modifying runtime behavior.
// Represents the user progress entity structure used by the application.
data class UserProgressEntity(
    @PrimaryKey val userId: Int = 1,
    val currentPoints: Int,
    val lifetimePoints: Int,
    val level: Int,
    val createdAt: Long,
    val updatedAt: Long
)
