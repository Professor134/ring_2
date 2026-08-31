package com.example.ring_2.data.dao

import androidx.room.*
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitProgressEntity
import com.example.ring_2.data.model.StreakCycleEntity
import com.example.ring_2.data.model.StreakMilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE deletedAt IS NULL")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteHabit(id: Long, timestamp: Long)

    // Progress
    @Query("SELECT * FROM habit_progress WHERE habitId = :habitId AND date = :date")
    suspend fun getProgress(habitId: Long, date: Long): HabitProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: HabitProgressEntity)

    @Query("SELECT * FROM habit_progress WHERE habitId = :habitId ORDER BY date DESC")
    fun getProgressForHabit(habitId: Long): Flow<List<HabitProgressEntity>>
    
    @Query("SELECT * FROM habit_progress WHERE date = :date")
    fun getProgressForDateFlow(date: Long): Flow<List<HabitProgressEntity>>

    @Query("SELECT * FROM habit_progress WHERE date >= :startDate ORDER BY date DESC")
    fun getRecentProgress(startDate: Long): Flow<List<HabitProgressEntity>>

    // Streaks
    @Query("SELECT * FROM streak_cycles WHERE habitId = :habitId AND isActive = 1 LIMIT 1")
    suspend fun getActiveStreakCycle(habitId: Long): StreakCycleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreakCycle(cycle: StreakCycleEntity): Long

    @Update
    suspend fun updateStreakCycle(cycle: StreakCycleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: StreakMilestoneEntity)

    @Query("SELECT * FROM streak_milestones WHERE streakCycleId = :cycleId AND milestone = :milestone")
    suspend fun getMilestone(cycleId: Long, milestone: Int): StreakMilestoneEntity?

    @Query("DELETE FROM habit_progress")
    suspend fun clearAllProgress()

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()
}
