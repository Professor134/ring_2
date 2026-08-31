package com.example.ring_2.data.dao

import androidx.room.*
import com.example.ring_2.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Query("SELECT * FROM achievements WHERE title = :title LIMIT 1")
    suspend fun getAchievementByTitle(title: String): Achievement?
}
