// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\HabitDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

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
// Handles the habit dao logic for this component without modifying runtime behavior.
// Represents the habit dao structure used by the application.
interface HabitDao {
// Manages the observe active flow without changing runtime behavior.
    @Query("SELECT * FROM habit WHERE deletedAt IS NULL ORDER BY name")
// Manages the observe active flow without changing runtime behavior.
    fun observeActive(): Flow<List<HabitEntity>>

// Manages the observe by id flow without changing runtime behavior.
    @Query("SELECT * FROM habit WHERE id = :habitId LIMIT 1")
// Manages the observe by id flow without changing runtime behavior.
    fun observeById(habitId: Long): Flow<HabitEntity?>

// Manages the observe progress flow without changing runtime behavior.
    @Query("SELECT * FROM habit_progress WHERE habitId = :habitId AND date BETWEEN :from AND :to ORDER BY date")
// Manages the observe progress flow without changing runtime behavior.
    fun observeProgress(habitId: Long, from: Long, to: Long): Flow<List<HabitProgressEntity>>

// Manages the observe progress for date flow without changing runtime behavior.
    @Query("SELECT * FROM habit_progress WHERE date = :date")
// Manages the observe progress for date flow without changing runtime behavior.
    fun observeProgressForDate(date: Long): Flow<List<HabitProgressEntity>>

// Manages the observe progress range flow without changing runtime behavior.
    @Query("SELECT * FROM habit_progress WHERE date BETWEEN :from AND :to ORDER BY date")
// Manages the observe progress range flow without changing runtime behavior.
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
