package com.example.ring_2.data.dao

import androidx.room.*
import com.example.ring_2.data.model.Habit
import com.example.ring_2.data.model.HabitRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND date = :date")
    suspend fun getRecord(habitId: Long, date: Long): HabitRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HabitRecord)

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId")
    fun getRecordsForHabit(habitId: Long): Flow<List<HabitRecord>>
    
    @Query("SELECT * FROM habit_records WHERE date = :date")
    fun getRecordsForDate(date: Long): Flow<List<HabitRecord>>
}
