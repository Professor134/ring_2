// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\test\java\com\example\ringapp\domain\engine\ScheduleEngineTest.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.HabitScheduleEntity
import com.example.ringapp.data.local.entities.ScheduleType
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Handles the schedule engine test logic for this component without modifying runtime behavior.
// Represents the schedule engine test structure used by the application.
class ScheduleEngineTest {
    private val weekly = HabitScheduleEntity(habitId = 1, scheduleType = ScheduleType.WEEKLY, daysOfWeek = "1,3", createdAt = 0, updatedAt = 0)

    @Test fun weeklyScheduleMatchesConfiguredDays() {
        assertTrue(ScheduleEngine.isActiveOnDate(weekly, LocalDate.of(2026, 9, 7)))
        assertFalse(ScheduleEngine.isActiveOnDate(weekly, LocalDate.of(2026, 9, 8)))
    }

    @Test fun nextOccurrenceFindsFollowingConfiguredDay() = assertTrue(ScheduleEngine. getNextOccurrence(weekly, LocalDate.of(2026, 9, 7)) == LocalDate.of(2026, 9, 9))
}