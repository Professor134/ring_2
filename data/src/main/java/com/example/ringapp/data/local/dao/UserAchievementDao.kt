package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAchievementDao {
    @Query("SELECT * FROM user_achievements ORDER BY unlockedAt DESC")
    fun observeAll(): Flow<List<UserAchievementEntity>>

    @Query("SELECT * FROM user_achievements WHERE achievementId = :achievementId LIMIT 1")
    suspend fun find(achievementId: Long): UserAchievementEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(state: UserAchievementEntity): Long

    @Update
    suspend fun update(state: UserAchievementEntity)
}
