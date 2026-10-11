// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\AchievementDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the achievement dao logic for this component without modifying runtime behavior.
// Represents the achievement dao structure used by the application.
interface AchievementDao {
// Manages the observe all flow without changing runtime behavior.
    @Query("SELECT * FROM achievement ORDER BY unlocked DESC, category, threshold")
// Manages the observe all flow without changing runtime behavior.
    fun observeAll(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun update(achievement: AchievementEntity)
}