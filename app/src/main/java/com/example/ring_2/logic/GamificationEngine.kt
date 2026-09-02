package com.example.ring_2.logic

import com.example.ring_2.data.model.PointTransactionEntity
import com.example.ring_2.data.model.TransactionType
import com.example.ring_2.data.model.UserProgressEntity
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

object GamificationEngine {

    const val STARTING_POINTS = 500
    const val HABIT_CREATION_COST = -25
    const val HABIT_COMPLETION_REWARD = 4
    const val STREAK_10_DAY_REWARD = 8
    const val STREAK_20_DAY_REWARD = 15
    const val TARGET_MISSED_PENALTY = -5
    const val CHANGE_TARGET_COST = -10
    const val TASK_CREATION_COST = -1
    const val TASK_COMPLETION_REWARD = 2
    const val DELETE_TASK_COST = -5
    const val DELETE_HABIT_COST = -50

    /**
     * Calculates the lifetime points required to reach the next level.
     * Level 1 -> 100 points to reach Level 2
     * Level 2 -> 150 points to reach Level 3
     * Level 3 -> 225 points to reach Level 4
     */
    fun calculateThresholdForLevel(level: Int): Int {
        if (level < 1) return 0
        return (100.0 * 1.5.pow(level - 1)).roundToInt()
    }

    fun calculateLevelFromPoints(lifetimeEarned: Int): Int {
        var level = 1
        var threshold = 1000
        while (lifetimeEarned >= threshold) {
            level++
            threshold += (1000 * 1.5.pow(level - 1)).roundToInt()
        }
        return level
    }

    fun getLevelProgress(lifetimeEarned: Int): Triple<Int, Int, Float> {
        var level = 1
        var currentThreshold = 0
        var nextThreshold = 1000
        
        while (lifetimeEarned >= nextThreshold) {
            level++
            currentThreshold = nextThreshold
            nextThreshold += (1000 * 1.5.pow(level - 1)).roundToInt()
        }
        
        val progressInLevel = lifetimeEarned - currentThreshold
        val range = nextThreshold - currentThreshold
        val percentage = if (range > 0) progressInLevel.toFloat() / range else 0f
        val needed = nextThreshold - lifetimeEarned
        
        return Triple(level, needed, percentage)
    }

    /**
     * Achievement Levels
     */
    enum class AchievementLevel { BRONZE, SILVER, GOLD }

    data class AchievementTier(
        val category: String,
        val level: AchievementLevel,
        val requirement: Int,
        val title: String
    )

    val ACHIEVEMENTS = listOf(
        // Streak based
        AchievementTier("Streak", AchievementLevel.BRONZE, 7, "Consistent (Bronze)"),
        AchievementTier("Streak", AchievementLevel.SILVER, 15, "Dedicated (Silver)"),
        AchievementTier("Streak", AchievementLevel.GOLD, 30, "Unstoppable (Gold)"),
        // Point based
        AchievementTier("Points", AchievementLevel.BRONZE, 1000, "Elite Starter"),
        AchievementTier("Points", AchievementLevel.SILVER, 3000, "Elite Pro"),
        AchievementTier("Points", AchievementLevel.GOLD, 5000, "Elite Legend"),
        // Completion based
        AchievementTier("Habits", AchievementLevel.BRONZE, 5, "Habit Starter"),
        AchievementTier("Habits", AchievementLevel.SILVER, 10, "Habit Master"),
        AchievementTier("Habits", AchievementLevel.GOLD, 20, "Habit God")
    )

    fun createTransaction(
        type: TransactionType,
        amount: Int,
        description: String,
        habitId: Long? = null,
        taskId: Long? = null,
        streakCycleId: Long? = null,
        uniqueReference: String? = null
    ): PointTransactionEntity {
        return PointTransactionEntity(
            amount = amount,
            type = type,
            description = description,
            habitId = habitId,
            taskId = taskId,
            streakCycleId = streakCycleId,
            uniqueReference = uniqueReference ?: UUID.randomUUID().toString()
        )
    }

    fun applyTransaction(progress: UserProgressEntity, transaction: PointTransactionEntity): UserProgressEntity {
        val newPoints = progress.currentPoints + transaction.amount
        
        // Positive transactions increase lifetime earned, negative do not decrease it.
        val newLifetimeEarned = if (transaction.amount > 0) {
            progress.lifetimeEarnedPoints + transaction.amount
        } else {
            progress.lifetimeEarnedPoints
        }

        val newLevel = calculateLevelFromPoints(newLifetimeEarned)

        return progress.copy(
            currentPoints = newPoints.coerceAtLeast(0),
            level = newLevel,
            lifetimeEarnedPoints = newLifetimeEarned,
            updatedAt = System.currentTimeMillis()
        )
    }
}
