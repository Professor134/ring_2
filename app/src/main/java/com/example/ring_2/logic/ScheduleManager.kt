package com.example.ring_2.logic

import com.example.ring_2.data.model.HabitSchedule
import java.util.*

object ScheduleManager {

    fun isHabitActiveOnDate(schedule: HabitSchedule, date: Long): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // 1-based
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat

        return when (schedule) {
            is HabitSchedule.Daily -> true
            is HabitSchedule.OddDays -> dayOfMonth % 2 != 0
            is HabitSchedule.EvenDays -> dayOfMonth % 2 == 0
            is HabitSchedule.Weekly -> schedule.daysOfWeek.contains(dayOfWeek)
            is HabitSchedule.Monthly -> {
                // If the month doesn't have the day (e.g. 31st), handle it safely
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                if (schedule.dayOfMonth > maxDay) {
                    dayOfMonth == maxDay
                } else {
                    dayOfMonth == schedule.dayOfMonth
                }
            }
            is HabitSchedule.Yearly -> dayOfMonth == schedule.dayOfMonth && month == schedule.month
            is HabitSchedule.Custom -> {
                // Simplified custom: every X days from start date
                // This would need the habit's start date which we don't have here
                // For now, let's assume Custom is like Daily if not fully specified
                true
            }
        }
    }
    
    fun isHabitActiveOnDate(schedule: HabitSchedule, habitStartDate: Long, date: Long): Boolean {
        if (schedule is HabitSchedule.Custom) {
            val diff = date - habitStartDate
            val days = diff / (1000 * 60 * 60 * 24)
            return days % schedule.intervalDays == 0L
        }
        return isHabitActiveOnDate(schedule, date)
    }
}
