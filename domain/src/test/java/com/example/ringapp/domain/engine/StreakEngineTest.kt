package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.HabitProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakEngineTest {
    private fun progress(date: Long, completed: Boolean, actual: Double = if (completed) 1.0 else 0.0) = 
        HabitProgressEntity(
            habitId = 1, 
            date = date, 
            target = 10000.0, 
            actual = actual, 
            percentage = if (completed) 100 else 0, 
            completed = completed, 
            createdAt = date, 
            updatedAt = date
        )

    @Test 
    fun currentStreakStopsAtFirstMiss() = 
        assertEquals(2, StreakEngine.calculateCurrentStreak(listOf(progress(3, true), progress(2, true), progress(1, false))))

    @Test 
    fun currentStreakIsZeroWhenLatestProgressIsZero() = 
        assertEquals(0, StreakEngine.calculateCurrentStreak(listOf(progress(3, false, 0.0), progress(2, true), progress(1, true))))

    @Test 
    fun currentStreakIsZeroWhenStepsProgressIsZero() = 
        assertEquals(0, StreakEngine.calculateCurrentStreak(listOf(progress(3, false, 0.0), progress(2, true, 10000.0))))

    @Test 
    fun bestStreakFindsLongestRun() = 
        assertEquals(3, StreakEngine.calculateBestStreak(listOf(progress(1, true), progress(2, true), progress(3, false), progress(4, true), progress(5, true), progress(6, true))))
}