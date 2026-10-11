// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\ProfileEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
// Handles the profile entity logic for this component without modifying runtime behavior.
// Represents the profile entity structure used by the application.
data class ProfileEntity(
    @PrimaryKey val userId: Int = 1,
    val name: String,
    val avatarColor: Int,
    val photoUri: String? = null,
    val dateOfBirth: Long? = null,
    val gender: String? = null,
    val themePreference: ThemeMode,
    val language: String = "en",
    val onboardingComplete: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
