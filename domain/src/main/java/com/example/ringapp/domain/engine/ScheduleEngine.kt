package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.HabitScheduleEntity
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.ScheduleType
import java.time.LocalDate

object ScheduleEngine {
    fun isActiveOnDate(habit: HabitEntity, date: LocalDate): Boolean {
        val start = java.time.Instant.ofEpochMilli(habit.startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        if (date.isBefore(start) || habit.deletedAt != null) return false
        return when (habit.scheduleType) {
            ScheduleType.DAILY -> true
            ScheduleType.ODD_DAYS -> date.dayOfMonth % 2 == 1
            ScheduleType.EVEN_DAYS -> date.dayOfMonth % 2 == 0
            ScheduleType.WEEKLY -> habit.scheduleDays.orEmpty().contains(date.dayOfWeek.value)
            ScheduleType.MONTHLY -> habit.scheduleDays.orEmpty().firstOrNull() == null || habit.scheduleDays.orEmpty().first() == date.dayOfMonth
            ScheduleType.YEARLY -> habit.scheduleDays.orEmpty().let { it.size < 2 || (it[0] == date.monthValue && it[1] == date.dayOfMonth) }
            ScheduleType.CUSTOM -> habit.scheduleDays.orEmpty().firstOrNull()?.let { interval -> interval <= 1 || java.time.temporal.ChronoUnit.DAYS.between(start, date) % interval == 0L } ?: true
        }
    }

    fun isActiveOnDate(schedule: HabitScheduleEntity, date: LocalDate): Boolean {
        if (!schedule.isActive) return false
        return when (schedule.scheduleType) {
            com.example.ringapp.data.local.entities.ScheduleType.DAILY -> true
            com.example.ringapp.data.local.entities.ScheduleType.ODD_DAYS -> date.dayOfMonth % 2 == 1
            com.example.ringapp.data.local.entities.ScheduleType.EVEN_DAYS -> date.dayOfMonth % 2 == 0
            com.example.ringapp.data.local.entities.ScheduleType.WEEKLY -> schedule.daysOfWeek.orEmpty().split(',').filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }.contains(date.dayOfWeek.value)
            com.example.ringapp.data.local.entities.ScheduleType.MONTHLY -> schedule.dayOfMonth == null || schedule.dayOfMonth == date.dayOfMonth
            com.example.ringapp.data.local.entities.ScheduleType.YEARLY -> schedule.month == null || (schedule.dayOfMonth == date.dayOfMonth && schedule.month == date.monthValue)
            com.example.ringapp.data.local.entities.ScheduleType.CUSTOM -> schedule.interval <= 1 || date.dayOfYear % schedule.interval == 0
        }
    }

    fun isActiveOnDate(task: com.example.ringapp.data.local.entities.TaskEntity, date: LocalDate): Boolean {
        if (task.deletedAt != null) return false
        val start = java.time.Instant.ofEpochMilli(task.createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        if (date.isBefore(start)) return false
        
        if (task.repeatType == com.example.ringapp.data.local.entities.RepeatType.NONE) {
            val due = task.dueDate?.let { java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
            return due == null || due == date
        }

        return when (task.repeatType) {
            com.example.ringapp.data.local.entities.RepeatType.DAILY -> true
            com.example.ringapp.data.local.entities.RepeatType.WEEKLY -> {
                val days = task.repeatDaysOfWeek?.split(',')?.filter { it.isNotBlank() }?.mapNotNull { it.toIntOrNull() }
                days.isNullOrEmpty() || days.contains(date.dayOfWeek.value)
            }
            com.example.ringapp.data.local.entities.RepeatType.MONTHLY -> {
                task.repeatDayOfMonth == null || task.repeatDayOfMonth == date.dayOfMonth
            }
            com.example.ringapp.data.local.entities.RepeatType.YEARLY -> {
                (task.repeatMonth == null || task.repeatMonth == date.monthValue) && 
                (task.repeatDayOfMonth == null || task.repeatDayOfMonth == date.dayOfMonth)
            }
            com.example.ringapp.data.local.entities.RepeatType.CUSTOM -> {
                val interval = task.repeatInterval.coerceAtLeast(1)
                java.time.temporal.ChronoUnit.DAYS.between(start, date) % interval == 0L
            }
            else -> false
        }
    }

    fun countActiveDays(habit: HabitEntity, start: LocalDate, end: LocalDate): Int {
        var count = 0
        var current = start
        while (!current.isAfter(end)) {
            if (isActiveOnDate(habit, current)) count++
            current = current.plusDays(1)
        }
        return count
    }
}
