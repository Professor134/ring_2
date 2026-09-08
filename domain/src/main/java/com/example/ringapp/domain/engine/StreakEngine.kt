package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.HabitProgressEntity

object StreakEngine {
    fun calculateCurrentStreak(progress: List<HabitProgressEntity>): Int {
        var streak = 0
        progress.sortedByDescending { it.date }.forEach { if (it.completed) streak++ else return streak }
        return streak
    }

    fun calculateBestStreak(progress: List<HabitProgressEntity>): Int {
        var best = 0
        var current = 0
        progress.sortedBy { it.date }.forEach { if (it.completed) { current++; best = maxOf(best, current) } else current = 0 }
        return best
    }
}
