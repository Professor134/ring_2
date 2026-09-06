package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val userId: Int = 1,
    val name: String,
    val avatarColor: Int,
    val photoUri: String? = null,
    val themePreference: ThemeMode,
    val language: String = "en",
    val onboardingComplete: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
