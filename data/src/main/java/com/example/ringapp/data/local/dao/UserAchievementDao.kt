// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\UserAchievementDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the user achievement dao logic for this component without modifying runtime behavior.
// Represents the user achievement dao structure used by the application.
interface UserAchievementDao {
// Manages the observe all flow without changing runtime behavior.
    @Query("SELECT * FROM user_achievements ORDER BY unlockedAt DESC")
// Manages the observe all flow without changing runtime behavior.
    fun observeAll(): Flow<List<UserAchievementEntity>>

    @Query("SELECT * FROM user_achievements WHERE achievementId = :achievementId LIMIT 1")
    suspend fun find(achievementId: Long): UserAchievementEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(state: UserAchievementEntity): Long

    @Update
    suspend fun update(state: UserAchievementEntity)
}
