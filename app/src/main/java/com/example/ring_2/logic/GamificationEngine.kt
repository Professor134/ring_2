package com.example.ring_2.logic

import com.example.ring_2.data.model.PointTransaction
import com.example.ring_2.data.model.TransactionType
import com.example.ring_2.data.model.UserProfile
import java.util.*
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

    fun calculateNextLevelThreshold(currentLevel: Int): Int {
        var threshold = 100.0
        repeat(currentLevel - 1) {
            threshold *= 1.5
        }
        return threshold.roundToInt()
    }

    fun createTransaction(
        type: TransactionType,
        amount: Int,
        description: String,
        relatedId: Long? = null
    ): PointTransaction {
        return PointTransaction(
            timestamp = System.currentTimeMillis(),
            amount = amount,
            type = type,
            description = description,
            relatedId = relatedId,
            reference = UUID.randomUUID().toString()
        )
    }

    fun applyTransaction(profile: UserProfile, transaction: PointTransaction): UserProfile {
        val newPoints = profile.elitePoints + transaction.amount
        val newAccumulated = if (transaction.amount > 0) {
            profile.accumulatedPoints + transaction.amount
        } else {
            profile.accumulatedPoints
        }

        var currentLevel = profile.level
        var threshold = calculateNextLevelThreshold(currentLevel)
        
        var tempAccumulated = newAccumulated
        // Level up if accumulated points reach threshold
        // Note: The requirement says "Level increases only when the user's accumulated Elite Point progress reaches the next threshold."
        // And "Level is permanent upward progress."
        while (tempAccumulated >= threshold) {
            currentLevel++
            threshold = calculateNextLevelThreshold(currentLevel)
        }

        return profile.copy(
            elitePoints = newPoints.coerceAtLeast(0),
            level = currentLevel,
            accumulatedPoints = newAccumulated
        )
    }
}
