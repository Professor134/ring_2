package com.example.ring_2.logic

import com.example.ring_2.data.model.HabitSchedule
import java.util.*

object ScheduleEngine {

    fun isHabitActiveOnDate(schedule: HabitSchedule, startDate: Long, date: Long): Boolean {
        val startMidnight = DateTimeUtils.getMidnightTimestamp(startDate)
        val targetMidnight = DateTimeUtils.getMidnightTimestamp(date)
        
        if (targetMidnight < startMidnight) return false
        
        val calendar = Calendar.getInstance().apply { timeInMillis = targetMidnight }
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) // 0-based
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat

        return when (schedule) {
            is HabitSchedule.Daily -> true
            is HabitSchedule.OddDays -> dayOfMonth % 2 != 0
            is HabitSchedule.EvenDays -> dayOfMonth % 2 == 0
            is HabitSchedule.Weekly -> schedule.daysOfWeek.contains(dayOfWeek)
            is HabitSchedule.Monthly -> {
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                if (schedule.dayOfMonth > maxDay) {
                    dayOfMonth == maxDay
                } else {
                    dayOfMonth == schedule.dayOfMonth
                }
            }
            is HabitSchedule.Yearly -> dayOfMonth == schedule.dayOfMonth && (month + 1) == schedule.month
            is HabitSchedule.Custom -> {
                val diffMillis = targetMidnight - startMidnight
                val diffDays = diffMillis / (1000 * 60 * 60 * 24)
                diffDays % schedule.intervalDays == 0L
            }
        }
    }
}
