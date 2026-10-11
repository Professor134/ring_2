// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\test\java\com\example\ringapp\domain\engine\StreakEngineTest.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.HabitProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Test

// Handles the streak engine test logic for this component without modifying runtime behavior.
// Represents the streak engine test structure used by the application.
class StreakEngineTest {
// Manages the progress flow without changing runtime behavior.
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

// Manages the current streak stops at first miss flow without changing runtime behavior.
    @Test 
// Manages the current streak stops at first miss flow without changing runtime behavior.
    fun currentStreakStopsAtFirstMiss() = 
        assertEquals(2, StreakEngine.calculateCurrentStreak(listOf(progress(3, true), progress(2, true), progress(1, false))))

// Manages the current streak is zero when latest progress is zero flow without changing runtime behavior.
    @Test 
// Manages the current streak is zero when latest progress is zero flow without changing runtime behavior.
    fun currentStreakIsZeroWhenLatestProgressIsZero() = 
        assertEquals(0, StreakEngine.calculateCurrentStreak(listOf(progress(3, false, 0.0), progress(2, true), progress(1, true))))

// Manages the current streak is zero when steps progress is zero flow without changing runtime behavior.
    @Test 
// Manages the current streak is zero when steps progress is zero flow without changing runtime behavior.
    fun currentStreakIsZeroWhenStepsProgressIsZero() = 
        assertEquals(0, StreakEngine.calculateCurrentStreak(listOf(progress(3, false, 0.0), progress(2, true, 10000.0))))

// Manages the best streak finds longest run flow without changing runtime behavior.
    @Test 
// Manages the best streak finds longest run flow without changing runtime behavior.
    fun bestStreakFindsLongestRun() = 
        assertEquals(3, StreakEngine.calculateBestStreak(listOf(progress(1, true), progress(2, true), progress(3, false), progress(4, true), progress(5, true), progress(6, true))))
}