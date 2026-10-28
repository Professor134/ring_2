// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\test\java\com\example\ringapp\ui\analytics\AggregateDailyTest.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.analytics

import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.local.entities.HabitType
import com.example.ringapp.data.local.entities.ScheduleType
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Handles the aggregate daily test logic for this component without modifying runtime behavior.
// Represents the aggregate daily test structure used by the application.
class AggregateDailyTest {
        // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.
// Manages the excludes bmi from habit score and totals flow without changing runtime behavior.
    @Test
// Manages the excludes bmi from habit score and totals flow without changing runtime behavior.
    fun excludesBmiFromHabitScoreAndTotals() {
        val date = LocalDate.of(2026, 9, 7)
        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val regularHabit = habit(id = 1, target = 10.0)
        val bmiHabit = habit(
            id = HabitEntity.BMI_HABIT_ID,
            target = 1.0,
            name = HabitEntity.BMI_HABIT_NAME
        )
        val progress = listOf(
            progress(habitId = regularHabit.id, timestamp = timestamp, target = 10.0, actual = 5.0, percentage = 50),
            progress(habitId = bmiHabit.id, timestamp = timestamp, target = 25.0, actual = 25.0, percentage = 100)
        )

        val point = aggregateDaily(listOf(regularHabit, bmiHabit), progress, date)

        assertNotNull(point)
        assertEquals(50, point!!.score)
        assertEquals(5.0, point.actual, 0.0)
        assertEquals(10.0, point.target, 0.0)
    }
    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the excludes inactive weekdays even when a progress record exists flow without changing runtime behavior.
    @Test
// Manages the excludes inactive weekdays even when a progress record exists flow without changing runtime behavior.
    fun excludesInactiveWeekdaysEvenWhenAProgressRecordExists() {
        val date = LocalDate.of(2026, 9, 8)
        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val weeklyHabit = habit(id = 3, target = 10.0).copy(
            scheduleType = ScheduleType.WEEKLY,
            scheduleDays = listOf(1, 3)
        )
        val record = progress(
            habitId = weeklyHabit.id,
            timestamp = timestamp,
            target = 10.0,
            actual = 10.0,
            percentage = 100
        )

        assertNull(aggregateDaily(listOf(weeklyHabit), listOf(record), date))
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the habit flow without changing runtime behavior.
    private fun habit(id: Long, target: Double, name: String = "Habit") = HabitEntity(
        id = id,
        name = name,
        categoryId = null,
        type = HabitType.MEASURABLE,
        target = target,
        scheduleType = ScheduleType.DAILY,
        startDate = LocalDate.of(2026, 9, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        color = 0,
        createdAt = 0,
        updatedAt = 0
    )

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the progress flow without changing runtime behavior.
    private fun progress(
        habitId: Long,
        timestamp: Long,
        target: Double,
        actual: Double,
        percentage: Int
    ) = HabitProgressEntity(
        habitId = habitId,
        date = timestamp,
        target = target,
        actual = actual,
        percentage = percentage,
        completed = percentage >= 100,
        createdAt = timestamp,
        updatedAt = timestamp
    )
}
