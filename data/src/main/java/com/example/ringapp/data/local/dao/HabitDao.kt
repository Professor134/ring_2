package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit WHERE deletedAt IS NULL ORDER BY name")
    fun observeActive(): Flow<List<HabitEntity>>

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("UPDATE habit SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :habitId")
    suspend fun softDelete(habitId: Long, deletedAt: Long)
}
