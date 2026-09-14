package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.TaskDao
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.UserProgressEntity
import com.example.ringapp.data.db.AppDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val dao: TaskDao,
    private val pointDao: com.example.ringapp.data.local.dao.PointTransactionDao,
    private val progressDao: com.example.ringapp.data.local.dao.UserProgressDao,
    private val database: AppDatabase,
    private val pointRepository: PointRepository
) {
    fun observeActive(): Flow<List<TaskEntity>> = dao.observeActive()
    fun observeById(taskId: Long): Flow<TaskEntity?> = dao.observeById(taskId)
    fun observeToday(start: Long, end: Long): Flow<List<TaskEntity>> = dao.observeToday(start, end)
    suspend fun create(task: TaskEntity): Long = database.withTransaction {
        val taskId = dao.insert(task)
        pointRepository.record(com.example.ringapp.data.local.entities.TransactionType.TASK_CREATE, -1, "task-create-$taskId", "Task created", taskId = taskId)
        taskId
    }
    suspend fun update(task: TaskEntity) = dao.update(task)
    suspend fun delete(taskId: Long, deletedAt: Long) = database.withTransaction {
        dao.softDelete(taskId, deletedAt)
        pointRepository.record(com.example.ringapp.data.local.entities.TransactionType.TASK_DELETE, -5, "task-delete-$taskId", "Task deleted", taskId = taskId)
    }
    suspend fun activeReminders(): List<TaskEntity> = dao.activeReminders()
    suspend fun awardTaskPoints(taskId: Long, timestamp: Long): Boolean {
        return pointRepository.record(com.example.ringapp.data.local.entities.TransactionType.TASK_COMPLETE, 4, "task-complete-$taskId-$timestamp", "Task completed", taskId = taskId, timestamp = timestamp)
    }
    suspend fun revokeTaskPoints(taskId: Long, timestamp: Long): Boolean {
        return pointRepository.record(com.example.ringapp.data.local.entities.TransactionType.TASK_UNCOMPLETE, -4, "task-uncomplete-$taskId-$timestamp", "Task completion reverted", taskId = taskId, timestamp = timestamp)
    }
    suspend fun cleanupOldTasks() {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        dao.deleteCompletedBefore(weekAgo)
    }
}
