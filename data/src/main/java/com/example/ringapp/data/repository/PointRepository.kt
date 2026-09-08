package com.example.ringapp.data.repository

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.dao.PointTransactionDao
import com.example.ringapp.data.local.dao.UserProgressDao
import com.example.ringapp.data.local.entities.*
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class PointRepository @Inject constructor(
    private val database: AppDatabase,
    private val pointDao: PointTransactionDao,
    private val progressDao: UserProgressDao
) {
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
        val inserted = pointDao.insert(PointTransactionEntity(amount = amount, type = type, description = description, habitId = habitId, taskId = taskId, uniqueReference = reference, timestamp = timestamp, createdAt = timestamp))
        if (inserted == -1L) return@withTransaction false
        val lifetime = current.lifetimePoints + amount.coerceAtLeast(0)
        progressDao.insert(current.copy(currentPoints = (current.currentPoints + amount).coerceAtLeast(0), lifetimePoints = lifetime, level = calculateLevel(lifetime), updatedAt = timestamp))
        true
    }

    fun calculateLevel(lifetimeEarned: Int): Int {
        var level = 1
        while (lifetimeEarned >= threshold(level) && level < 1000) level++
        return level
    }

    private fun threshold(level: Int): Int {
        var value = 100.0
        repeat((level - 1).coerceAtLeast(0)) { value *= 1.5 }
        return value.toInt()
    }
}