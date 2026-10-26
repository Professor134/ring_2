package com.example.ringapp.domain.engine

object AnalyticsEngine {
    fun calculateDailyScore(habitScore: Int, taskScore: Int, consistencyScore: Int, streakScore: Int): Int =
        (habitScore * 0.45 + taskScore * 0.20 + consistencyScore * 0.20 + streakScore * 0.15).toInt().coerceIn(0, 100)

    fun calculateCompletionRate(completed: Int, total: Int): Int = if (total == 0) 0 else (completed * 100 / total).coerceIn(0, 100)
    fun calculateGrowth(todayScore: Int, previousComparableScore: Int): Int = todayScore - previousComparableScore
    fun calculateCategoryScore(completed: Int, scheduled: Int): Int = calculateCompletionRate(completed, scheduled)
}
