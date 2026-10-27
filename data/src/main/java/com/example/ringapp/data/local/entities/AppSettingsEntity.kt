// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\AppSettingsEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
// Handles the app settings entity logic for this component without modifying runtime behavior.
// Represents the app settings entity structure used by the application.
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val onboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val habitRemindersEnabled: Boolean = true,
    val motivationEnabled: Boolean = true,
    val achievementNotificationsEnabled: Boolean = true,
    val streakNotificationsEnabled: Boolean = true,
    val morningMotivationTime: Long? = null,
    val eveningMotivationTime: Long? = null,
    val weekStartsOn: Int = 1,
    val firstDayOfMonth: Int = 1,
    val updatedAt: Long
)
