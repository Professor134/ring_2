// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\PointRepository.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.dao.PointTransactionDao
import com.example.ringapp.data.local.dao.UserProgressDao
import com.example.ringapp.data.local.entities.*
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

// Handles the point repository logic for this component without modifying runtime behavior.
// Represents the point repository structure used by the application.
class PointRepository @Inject constructor(
    private val database: AppDatabase,
    private val pointDao: PointTransactionDao,
    private val progressDao: UserProgressDao
) {
// Manages the observe all flow without changing runtime behavior.
    fun observeAll(): Flow<List<PointTransactionEntity>> = pointDao.observeAll()
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
        check(amount >= 0 || current.currentPoints >= -amount) { "Not enough Elite Points." }
        
        val lifetimeToAdd = amount.coerceAtLeast(0)
        val newLifetime = current.lifetimePoints + lifetimeToAdd
        val oldLevel = current.level
        val newLevel = calculateLevel(newLifetime)
        
        var totalPointsDelta = amount
        var finalLifetime = newLifetime
        
        if (newLevel > oldLevel) {
            val levelDiff = newLevel - oldLevel
            val rewardPoints = levelDiff * 15
            totalPointsDelta += rewardPoints
            finalLifetime += rewardPoints
        }

        val inserted = pointDao.insert(PointTransactionEntity(amount = totalPointsDelta, type = type, description = description, habitId = habitId, taskId = taskId, uniqueReference = reference, timestamp = timestamp, createdAt = timestamp))
        if (inserted == -1L) return@withTransaction false
        
        progressDao.insert(current.copy(currentPoints = (current.currentPoints + totalPointsDelta).coerceAtLeast(0), lifetimePoints = finalLifetime, level = newLevel, updatedAt = timestamp))
        true
    }

// Manages the calculate level flow without changing runtime behavior.
    fun calculateLevel(lifetimeEarned: Int): Int {
        var level = 1
        while (threshold(level + 1) <= lifetimeEarned && level < 1000) level++
        return level
    }

// Manages the threshold flow without changing runtime behavior.
    private fun threshold(level: Int): Long {
        var total = 0.0
        var currentNeeded = 100.0
        for (i in 1 until level) {
            total += currentNeeded
            currentNeeded *= 1.25
        }
        return total.toLong()
    }
}
