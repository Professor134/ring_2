package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.AchievementEntity

object AchievementEngine {
    fun calculateProgress(achievement: AchievementEntity, value: Int): Int = value.coerceIn(0, achievement.threshold)
    fun checkUnlock(achievement: AchievementEntity, value: Int): Boolean = !achievement.unlocked && value >= achievement.threshold
}
