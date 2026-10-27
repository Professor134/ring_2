// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\AchievementRepository.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.AchievementDao
import com.example.ringapp.data.local.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Handles the achievement repository logic for this component without modifying runtime behavior.
// Represents the achievement repository structure used by the application.
class AchievementRepository @Inject constructor(private val dao: AchievementDao) {
// Manages the observe all flow without changing runtime behavior.
    fun observeAll(): Flow<List<AchievementEntity>> = dao.observeAll()
    suspend fun seed(achievements: List<AchievementEntity>) = dao.insertAll(achievements)
    suspend fun update(achievement: AchievementEntity) = dao.update(achievement)
}