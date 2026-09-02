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

    fun getLevelProgress(lifetimeEarned: Int): Quadruple<Int, Int, Int, Float> {
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
        
        return Quadruple(level, progressInLevel, range, percentage)
    }

    data class Quadruple<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

    /**
     * Achievement Levels
     */
    enum class AchievementLevel { BRONZE, SILVER, GOLD }

    data class AchievementTier(
        val category: String,
        val level: AchievementLevel,
        val requirement: Int,
        val title: String,
        val description: String
    )

    val ACHIEVEMENTS_MAP = mapOf(
        "Streak" to listOf(
            AchievementTier("Streak", AchievementLevel.BRONZE, 7, "Consistent", "7 days streak"),
            AchievementTier("Streak", AchievementLevel.SILVER, 15, "Dedicated", "15 days streak"),
            AchievementTier("Streak", AchievementLevel.GOLD, 30, "Unstoppable", "30 days streak"),
            AchievementTier("Streak", AchievementLevel.GOLD, 31, "Flow State", "30+ days streak") // Max state
        ),
        "CreateHabit" to listOf(
            AchievementTier("CreateHabit", AchievementLevel.BRONZE, 1, "Pioneer", "Create 1 habit"),
            AchievementTier("CreateHabit", AchievementLevel.SILVER, 5, "Architect", "Create 5 habits"),
            AchievementTier("CreateHabit", AchievementLevel.GOLD, 10, "Mastermind", "Create 10 habits"),
            AchievementTier("CreateHabit", AchievementLevel.GOLD, 11, "Overlord", "Create 10+ habits")
        ),
        "CompleteHabit" to listOf(
            AchievementTier("CompleteHabit", AchievementLevel.BRONZE, 10, "Novice", "Complete 10 habits"),
            AchievementTier("CompleteHabit", AchievementLevel.SILVER, 50, "Expert", "Complete 50 habits"),
            AchievementTier("CompleteHabit", AchievementLevel.GOLD, 100, "Elite", "Complete 100 habits"),
            AchievementTier("CompleteHabit", AchievementLevel.GOLD, 101, "Grandmaster", "Complete 100+ habits")
        ),
        "CreateTask" to listOf(
            AchievementTier("CreateTask", AchievementLevel.BRONZE, 5, "Planner", "Create 5 tasks"),
            AchievementTier("CreateTask", AchievementLevel.SILVER, 20, "Organizer", "Create 20 tasks"),
            AchievementTier("CreateTask", AchievementLevel.GOLD, 50, "Strategist", "Create 50 tasks"),
            AchievementTier("CreateTask", AchievementLevel.GOLD, 51, "Tactician", "Create 50+ tasks")
        ),
        "CompleteTask" to listOf(
            AchievementTier("CompleteTask", AchievementLevel.BRONZE, 5, "Achiever", "Complete 5 tasks"),
            AchievementTier("CompleteTask", AchievementLevel.SILVER, 20, "Productive", "Complete 20 tasks"),
            AchievementTier("CompleteTask", AchievementLevel.GOLD, 50, "Unstoppable", "Complete 50 tasks"),
            AchievementTier("CompleteTask", AchievementLevel.GOLD, 51, "Legendary", "Complete 50+ tasks")
        ),
        "Points" to listOf(
            AchievementTier("Points", AchievementLevel.BRONZE, 1000, "Saver", "Reach 1000 points"),
            AchievementTier("Points", AchievementLevel.SILVER, 3000, "Investor", "Reach 3000 points"),
            AchievementTier("Points", AchievementLevel.GOLD, 5000, "Millionaire", "Reach 5000 points"),
            AchievementTier("Points", AchievementLevel.GOLD, 5001, "Wealthy", "Reach 5000+ points")
        ),
        "Level" to listOf(
            AchievementTier("Level", AchievementLevel.BRONZE, 5, "Student", "Reach Level 5"),
            AchievementTier("Level", AchievementLevel.SILVER, 10, "Scholar", "Reach Level 10"),
            AchievementTier("Level", AchievementLevel.GOLD, 20, "Sage", "Reach Level 20"),
            AchievementTier("Level", AchievementLevel.GOLD, 21, "Oracle", "Reach Level 20+")
        )
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
