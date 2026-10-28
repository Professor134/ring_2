// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\TaskRepository.kt
// Purpose: Handles task operations and applies points rules for task lifecycle events.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.dao.TaskDao
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.local.entities.TransactionType
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskRepository @Inject constructor(
    private val dao: TaskDao,
    private val database: AppDatabase,
    private val pointRepository: PointRepository
) {
    fun observeActive(): Flow<List<TaskEntity>> = dao.observeActive()
    fun observeById(taskId: Long): Flow<TaskEntity?> = dao.observeById(taskId)
    fun observeToday(start: Long, end: Long): Flow<List<TaskEntity>> = dao.observeToday(start, end)

    suspend fun create(task: TaskEntity): Long = database.withTransaction {
        val taskId = dao.insert(task)
        pointRepository.record(
            type = TransactionType.TASK_CREATE,
            amount = -1,
            reference = "task-create-$taskId",
            description = "Created task: ${task.title}",
            taskId = taskId
        )
        taskId
    }

    suspend fun update(task: TaskEntity) = dao.update(task)

    suspend fun delete(taskId: Long, deletedAt: Long) = database.withTransaction {
        val task = dao.observeById(taskId).first() ?: return@withTransaction
        dao.softDelete(taskId, deletedAt)
        if (!task.completed) {
            pointRepository.record(
                type = TransactionType.TASK_DELETE,
                amount = -5,
                reference = "task-delete-$taskId-$deletedAt",
                description = "Deleted incomplete task",
                taskId = taskId,
                timestamp = deletedAt
            )
        }
    }

    suspend fun activeReminders(): List<TaskEntity> = dao.activeReminders()

    suspend fun awardTaskPoints(taskId: Long, timestamp: Long): Boolean {
        return pointRepository.record(
            type = TransactionType.TASK_COMPLETE,
            amount = 3,
            reference = "task-complete-$taskId-$timestamp",
            description = "Task completed",
            taskId = taskId,
            timestamp = timestamp
        )
    }

    suspend fun revokeTaskPoints(taskId: Long, timestamp: Long): Boolean {
        return pointRepository.record(
            type = TransactionType.TASK_UNCOMPLETE,
            amount = -3,
            reference = "task-uncomplete-$taskId-$timestamp",
            description = "Task completion reverted",
            taskId = taskId,
            timestamp = timestamp
        )
    }

    suspend fun checkAndApplyOverdueTaskPenalties() = database.withTransaction {
        val startOfToday = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val activeTasks = dao.observeActive().first()
        activeTasks.filter { task ->
            !task.completed && task.dueDate != null && task.dueDate < startOfToday
        }.forEach { task ->
            pointRepository.record(
                type = TransactionType.MISSED_TARGET,
                amount = -2,
                reference = "task-overdue-penalty-${task.id}",
                description = "Overdue task penalty for ${task.title}",
                taskId = task.id
            )
        }
    }

    suspend fun cleanupOldTasks() {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        dao.deleteCompletedBefore(weekAgo)
    }
}
