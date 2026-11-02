// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\engine\AchievementEngine.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.engine

import com.example.ringapp.data.local.entities.AchievementEntity
import java.util.Locale

enum class AchievementTier(val label: String) {
    NEWBIE("Newbie"),
    EXPERIENCED("Experienced"),
    EXPERT("Expert"),
    ELITE("Elite")
}

data class AchievementTierProgress(
    val tier: AchievementTier,
    val current: Int,
    val target: Int,
    val previousTarget: Int,
    val unlocked: Boolean,
    val earned: Boolean,
    val rewardPoints: Int,
    val rewardReference: String
) {
    val progress: Float
        get() = if (unlocked && target > previousTarget) {
            ((current - previousTarget).toFloat() / (target - previousTarget)).coerceIn(0f, 1f)
        } else 0f
}

data class AchievementProgress(
    val id: String,
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
        habitCompletions: Int,
        earnedTierReferences: Set<String> = emptySet()
    ): List<AchievementProgress> = listOf(
        track("consistency", "Consistency", "Keep a habit streak going.", longestStreak, listOf(3, 7, 21, 60), earnedTierReferences),
        track("habit-builder", "Habit builder", "Create habits that support your routine.", habitsCreated, listOf(20, 50, 100, 200), earnedTierReferences),
        track("task-finisher", "Task finisher", "Complete tasks on your list.", completedTasks, listOf(5, 25, 100, 250), earnedTierReferences),
        track("point-collector", "Point collector", "Earn points through your personal habits and tasks.", lifetimePoints, listOf(500, 1_500, 5_000, 15_000), earnedTierReferences),
        track("level-up", "Level up", "Build levels through your personal habits and tasks.", level, listOf(3, 8, 15, 30), earnedTierReferences),
        track("habit-practice", "Habit practice", "Record completed habit goals.", habitCompletions, listOf(20, 100, 500, 1_500), earnedTierReferences)
    )

    private fun track(
        id: String,
        title: String,
        description: String,
        value: Int,
        thresholds: List<Int>,
        earnedTierReferences: Set<String>
    ) = AchievementProgress(
        id = id,
        title = title,
        description = description,
        value = value.coerceAtLeast(0),
        tiers = thresholds.mapIndexed { index, target ->
            val rewardReference = "achievement-$id-${AchievementTier.entries[index].name.lowercase(Locale.ROOT)}"
            val earned = rewardReference in earnedTierReferences
            val previousTarget = thresholds.getOrElse(index - 1) { 0 }
            AchievementTierProgress(
                tier = AchievementTier.entries[index],
                current = if (earned) target else value.coerceAtLeast(0).coerceAtMost(target),
                target = target,
                previousTarget = previousTarget,
                unlocked = index == 0 || earnedTierReferences.contains(
                    "achievement-$id-${AchievementTier.entries[index - 1].name.lowercase(Locale.ROOT)}"
                ) || value >= previousTarget,
                earned = earned,
                rewardPoints = listOf(10, 25, 50, 100)[index],
                rewardReference = rewardReference
            )
        }
    )
}
