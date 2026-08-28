package com.example.ring_2.logic

import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitProgressEntity
import java.util.*

object StreakEngine {

    fun calculateCurrentStreak(
        habit: HabitEntity,
        progressList: List<HabitProgressEntity>,
        referenceDate: Long = System.currentTimeMillis()
    ): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = referenceDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val progressMap = progressList.associateBy { it.date }
        var streak = 0
        
        val todayMillis = calendar.timeInMillis
        val todayProgress = progressMap[todayMillis]
        
        if (todayProgress?.completed == true) {
            streak++
        } else if (ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, todayMillis)) {
            // Scheduled today but not done yet, streak is still alive from yesterday
        } else {
            // Not scheduled today, streak continues from yesterday
        }

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        while (calendar.timeInMillis >= habit.startDate) {
            val date = calendar.timeInMillis
            if (ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, date)) {
                val progress = progressMap[date]
                if (progress?.completed == true) {
                    streak++
                } else {
                    break
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return streak
    }
}
