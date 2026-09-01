package com.example.ring_2.logic

import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitProgressEntity
import com.example.ring_2.data.model.HabitType
import java.text.SimpleDateFormat
import java.util.*

data class GrowthPoint(
    val dateLabel: String,
    val fullDate: String,
    val dailyAggregate: Float,
    val prevAggregate: Float,
    val difference: Float,
    val averageStreak: Float,
    val growthValue: Float
)

object GrowthCalculator {

    fun calculateOverallGrowth(
        allProgress: List<HabitProgressEntity>,
        habits: List<HabitEntity>,
        filter: String
    ): List<GrowthPoint> {
        if (habits.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()
        val todayMidnight = DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis())
        
        // Calculate from start of tracking (or start of first habit)
        val earliestProgress = allProgress.minByOrNull { it.date }?.date 
            ?: habits.minOfOrNull { it.startDate } 
            ?: todayMidnight
            
        val startDate = DateTimeUtils.getMidnightTimestamp(earliestProgress)
        
        val allDates = mutableListOf<Long>()
        var curr = startDate
        while (curr <= todayMidnight) {
            allDates.add(curr)
            calendar.timeInMillis = curr
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            curr = calendar.timeInMillis
        }

        val dailyGrowthPoints = mutableListOf<GrowthPoint>()
        var previousGrowth = 0f
        var previousAggregate = 0f

        val df = SimpleDateFormat("d MMM", Locale.getDefault())
        val fullDf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

        allDates.forEachIndexed { index, date ->
            val activeHabits = habits.filter { habit ->
                ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, date)
            }

            if (activeHabits.isEmpty()) {
                // If no habits active, growth logic: maintaining (diff=0) + streak (0)
                // However, we should still store a point for consistency
                val currentGrowth = if (index == 0) 0f else previousGrowth 
                dailyGrowthPoints.add(
                    GrowthPoint(
                        dateLabel = df.format(Date(date)),
                        fullDate = fullDf.format(Date(date)),
                        dailyAggregate = 0f,
                        prevAggregate = previousAggregate,
                        difference = -previousAggregate,
                        averageStreak = 0f,
                        growthValue = currentGrowth
                    )
                )
                previousAggregate = 0f
                previousGrowth = currentGrowth
                return@forEachIndexed 
            }

            var sumEffectiveProgress = 0.0
            var sumTargets = 0.0
            var sumStreaks = 0.0

            activeHabits.forEach { habit ->
                val prog = allProgress.find { (it.habitId == habit.id) && (it.date == date) }
                val actual = prog?.actualValue ?: 0.0
                
                val effective = if (habit.type == HabitType.MEASURABLE) {
                    minOf(actual, habit.target)
                } else {
                    if (prog?.completed == true) habit.target else 0.0
                }
                
                sumEffectiveProgress += effective
                sumTargets += habit.target
                sumStreaks += calculateHistoricalStreak(allProgress, habit, date)
            }

            val dailyAggregate = if (sumTargets > 0) (sumEffectiveProgress / sumTargets * 100).toFloat() else 0f
            val averageStreak = (sumStreaks / activeHabits.size).toFloat()
            
            val currentGrowth: Float
            val difference: Float

            if (index == 0) {
                currentGrowth = 0f
                difference = 0f
            } else {
                difference = dailyAggregate - previousAggregate
                
                currentGrowth = when {
                    difference > 0 -> previousGrowth + (difference / 2f) + averageStreak
                    difference < 0 -> previousGrowth + (difference / 2f)
                    else -> previousGrowth + averageStreak // difference == 0
                }
            }

            dailyGrowthPoints.add(
                GrowthPoint(
                    dateLabel = df.format(Date(date)),
                    fullDate = fullDf.format(Date(date)),
                    dailyAggregate = dailyAggregate,
                    prevAggregate = previousAggregate,
                    difference = difference,
                    averageStreak = averageStreak,
                    growthValue = currentGrowth
                )
            )

            previousGrowth = currentGrowth
            previousAggregate = dailyAggregate
        }

        return when (filter) {
            "Days" -> dailyGrowthPoints.takeLast(30)
            "Weeks" -> aggregateGrowthByPeriod(dailyGrowthPoints, Calendar.WEEK_OF_YEAR, "Week")
            "Months" -> aggregateGrowthByPeriod(dailyGrowthPoints, Calendar.MONTH, "Month")
            "Year" -> aggregateGrowthByPeriod(dailyGrowthPoints, Calendar.MONTH, "Month") // Year shows months
            else -> dailyGrowthPoints.takeLast(7)
        }
    }

    private fun calculateHistoricalStreak(
        allProgress: List<HabitProgressEntity>,
        habit: HabitEntity,
        endDate: Long
    ): Int {
        var streak = 0
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = endDate
        
        var currentCheck = endDate
        while (true) {
            val isActive = ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, currentCheck)
            if (isActive) {
                val prog = allProgress.find { it.habitId == habit.id && it.date == currentCheck }
                if (prog?.completed == true) {
                    streak++
                } else {
                    break
                }
            }
            
            calendar.timeInMillis = currentCheck
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            currentCheck = calendar.timeInMillis
            
            if (currentCheck < habit.startDate) break
        }
        return streak
    }

    private fun aggregateGrowthByPeriod(points: List<GrowthPoint>, periodField: Int, labelPrefix: String): List<GrowthPoint> {
        if (points.isEmpty()) return emptyList()
        val calendar = Calendar.getInstance()
        val fullDf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        
        return points.groupBy { point ->
            calendar.timeInMillis = fullDf.parse(point.fullDate)?.time ?: 0L
            val year = calendar.get(Calendar.YEAR)
            val period = calendar.get(periodField)
            "$year-$period"
        }.map { (key, periodPoints) ->
            val last = periodPoints.last()
            val label = if (periodField == Calendar.MONTH) {
                SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(fullDf.parse(last.fullDate)!!)
            } else {
                "$labelPrefix ${key.split("-")[1]}, ${key.split("-")[0]}"
            }
            
            last.copy(
                dateLabel = label
            )
        }
    }
}
