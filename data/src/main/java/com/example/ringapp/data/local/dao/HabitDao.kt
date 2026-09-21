package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit WHERE deletedAt IS NULL ORDER BY name")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit WHERE id = :habitId LIMIT 1")
    fun observeById(habitId: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habit_progress WHERE habitId = :habitId AND date BETWEEN :from AND :to ORDER BY date")
    fun observeProgress(habitId: Long, from: Long, to: Long): Flow<List<HabitProgressEntity>>

    @Query("SELECT * FROM habit_progress WHERE date = :date")
    fun observeProgressForDate(date: Long): Flow<List<HabitProgressEntity>>

    @Query("SELECT * FROM habit_progress WHERE date BETWEEN :from AND :to ORDER BY date")
    fun observeProgressRange(from: Long, to: Long): Flow<List<HabitProgressEntity>>

    @Query("SELECT * FROM habit_progress WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun progressForDate(habitId: Long, date: Long): HabitProgressEntity?

    @Query("SELECT * FROM habit_progress WHERE habitId = :habitId")
    suspend fun getProgressForHabit(habitId: Long): List<HabitProgressEntity>

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: HabitProgressEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("UPDATE habit SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :habitId")
    suspend fun softDelete(habitId: Long, deletedAt: Long)
}
