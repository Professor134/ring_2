package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ringapp.data.local.entities.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = 1 LIMIT 1")
    suspend fun getCurrent(): UserProgressEntity?

    @Query("SELECT * FROM user_progress WHERE userId = 1 LIMIT 1")
    fun observeCurrent(): Flow<UserProgressEntity?>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgressEntity)
}