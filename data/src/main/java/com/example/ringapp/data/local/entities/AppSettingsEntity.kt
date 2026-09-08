package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
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
