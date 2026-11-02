// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\engine\AchievementEngine.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.AchievementEntity

enum class AchievementTier(val label: String) {
    NEWBIE("Newbie"),
    EXPERIENCED("Experienced"),
    EXPERT("Expert"),
    ELITE("Elite")
}

data class AchievementTierProgress(
    val tier: AchievementTier,
    val current: Int,
    val target: Int
) {
    val progress: Float
        get() = if (target > 0) (current.toFloat() / target).coerceIn(0f, 1f) else 0f
}

data class AchievementProgress(
    val title: String,
    val description: String,
    val value: Int,
    val tiers: List<AchievementTierProgress>
)

// Handles the achievement engine logic for this component without modifying runtime behavior.
// Represents the achievement engine structure used by the application.
object AchievementEngine {
// Manages the calculate progress flow without changing runtime behavior.
    fun calculateProgress(achievement: AchievementEntity, value: Int): Int = value.coerceIn(0, achievement.threshold)
// Manages the check unlock flow without changing runtime behavior.
    fun checkUnlock(achievement: AchievementEntity, value: Int): Boolean = !achievement.unlocked && value >= achievement.threshold

    fun buildProfileProgress(
        longestStreak: Int,
        habitsCreated: Int,
        completedTasks: Int,
        lifetimePoints: Int,
        level: Int,
        habitCompletions: Int
    ): List<AchievementProgress> = listOf(
        track("Consistency", "Keep a habit streak going.", longestStreak, listOf(1, 3, 7, 30)),
        track("Habit builder", "Create habits that support your routine.", habitsCreated, listOf(1, 5, 15, 30)),
        track("Task finisher", "Complete tasks on your list.", completedTasks, listOf(1, 10, 50, 100)),
        track("Point collector", "Earn points through consistent progress.", lifetimePoints, listOf(100, 500, 2_000, 10_000)),
        track("Level up", "Grow your profile level.", level, listOf(2, 5, 10, 25)),
        track("Habit practice", "Record completed habit goals.", habitCompletions, listOf(1, 20, 100, 500))
    )

    private fun track(
        title: String,
        description: String,
        value: Int,
        thresholds: List<Int>
    ) = AchievementProgress(
        title = title,
        description = description,
        value = value.coerceAtLeast(0),
        tiers = thresholds.mapIndexed { index, target ->
            AchievementTierProgress(
                tier = AchievementTier.entries[index],
                current = value.coerceAtLeast(0).coerceAtMost(target),
                target = target
            )
        }
    )
}
