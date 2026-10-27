// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\engine\AchievementEngine.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.AchievementEntity

// Handles the achievement engine logic for this component without modifying runtime behavior.
// Represents the achievement engine structure used by the application.
object AchievementEngine {
// Manages the calculate progress flow without changing runtime behavior.
    fun calculateProgress(achievement: AchievementEntity, value: Int): Int = value.coerceIn(0, achievement.threshold)
// Manages the check unlock flow without changing runtime behavior.
    fun checkUnlock(achievement: AchievementEntity, value: Int): Boolean = !achievement.unlocked && value >= achievement.threshold
}
