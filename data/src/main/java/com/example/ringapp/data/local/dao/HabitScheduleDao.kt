package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.HabitScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitScheduleDao {
    @Query("SELECT * FROM habit_schedules WHERE habitId = :habitId AND isActive = 1")
    fun observeActive(habitId: Long): Flow<List<HabitScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: HabitScheduleEntity): Long

    @Update
    suspend fun update(schedule: HabitScheduleEntity)
}
