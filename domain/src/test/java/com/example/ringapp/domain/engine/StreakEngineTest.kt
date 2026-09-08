package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.HabitProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakEngineTest {
    private fun progress(date: Long, completed: Boolean) = HabitProgressEntity(habitId = 1, date = date, target = 1, actual = if (completed) 1 else 0, percentage = if (completed) 100 else 0, completed = completed, createdAt = date, updatedAt = date)

    @Test fun currentStreakStopsAtFirstMiss() = assertEquals(2, StreakEngine.calculateCurrentStreak(listOf(progress(3, true), progress(2, true), progress(1, false))))
    @Test fun bestStreakFindsLongestRun() = assertEquals(3, StreakEngine.calculateBestStreak(listOf(progress(1, true), progress(2, true), progress(3, false), progress(4, true), progress(5, true), progress(6, true))))
}