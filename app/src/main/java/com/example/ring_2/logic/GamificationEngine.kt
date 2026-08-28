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
        while (lifetimeEarned >= calculateThresholdForLevel(level)) {
            level++
        }
        return level
    }

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
