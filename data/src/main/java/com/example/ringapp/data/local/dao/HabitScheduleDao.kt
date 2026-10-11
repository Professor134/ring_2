// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\HabitScheduleDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.HabitScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the habit schedule dao logic for this component without modifying runtime behavior.
// Represents the habit schedule dao structure used by the application.
interface HabitScheduleDao {
// Manages the observe active flow without changing runtime behavior.
    @Query("SELECT * FROM habit_schedules WHERE habitId = :habitId AND isActive = 1")
// Manages the observe active flow without changing runtime behavior.
    fun observeActive(habitId: Long): Flow<List<HabitScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: HabitScheduleEntity): Long

    @Update
    suspend fun update(schedule: HabitScheduleEntity)
}
