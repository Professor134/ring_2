// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\PointRepository.kt
// Purpose: Manages Elite Points balance, level calculations, and transaction records.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.dao.PointTransactionDao
import com.example.ringapp.data.local.dao.UserProgressDao
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.TransactionType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class PointRepository @Inject constructor(
    private val database: AppDatabase,
    private val pointDao: PointTransactionDao,
    private val progressDao: UserProgressDao
) {
    fun observeAll(): Flow<List<PointTransactionEntity>> = pointDao.observeAll()

    suspend fun hasTransaction(reference: String): Boolean {
        return pointDao.findByReference(reference) != null
    }

    suspend fun record(
        type: TransactionType,
        amount: Int,
        reference: String,
        description: String,
        habitId: Long? = null,
        taskId: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean = database.withTransaction {
        if (pointDao.findByReference(reference) != null) return@withTransaction false
        val current = progressDao.getCurrent() ?: return@withTransaction false

        // Balance cannot fall below zero. Fail cleanly if insufficient points.
        if (amount < 0 && current.currentPoints < -amount) {
            val cost = -amount
            throw IllegalStateException("Not enough Elite Points. You need $cost points for this action.")
        }

        val lifetimeToAdd = amount.coerceAtLeast(0)
        val newLifetime = current.lifetimePoints + lifetimeToAdd
        val oldLevel = current.level
        val newLevel = calculateLevel(newLifetime)

        val inserted = pointDao.insert(
            PointTransactionEntity(
                amount = amount,
                type = type,
                description = description,
                habitId = habitId,
                taskId = taskId,
                uniqueReference = reference,
                timestamp = timestamp,
                createdAt = timestamp
            )
        )
        if (inserted == -1L) return@withTransaction false

        val levelRewardPoints = (oldLevel + 1..newLevel).sumOf { reachedLevel ->
            val rewardInserted = pointDao.insert(
                PointTransactionEntity(
                    amount = LEVEL_UP_REWARD_POINTS,
                    type = TransactionType.LEVEL_UP_REWARD,
                    description = "Reached level $reachedLevel",
                    habitId = habitId,
                    taskId = taskId,
                    uniqueReference = "level-up-$reachedLevel",
                    timestamp = timestamp,
                    createdAt = timestamp
                )
            )
            if (rewardInserted == -1L) 0 else LEVEL_UP_REWARD_POINTS
        }
        val updatedCurrentPoints = (current.currentPoints + amount + levelRewardPoints).coerceAtLeast(0)
        progressDao.insert(
            current.copy(
                currentPoints = updatedCurrentPoints,
                lifetimePoints = newLifetime + levelRewardPoints,
                level = newLevel,
                updatedAt = timestamp
            )
        )
        true
    }

    fun calculateLevel(lifetimeEarned: Int): Int {
        var level = 1
        while (threshold(level + 1) <= lifetimeEarned && level < 1000) level++
        return level
    }

    private fun threshold(level: Int): Long {
        var total = 0.0
        var currentNeeded = 100.0
        for (i in 1 until level) {
            total += currentNeeded
            currentNeeded *= 1.25
        }
        return total.toLong()
    }

    private companion object {
        const val LEVEL_UP_REWARD_POINTS = 15
    }
}
