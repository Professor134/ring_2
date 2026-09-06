package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.TaskDao
import com.example.ringapp.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val dao: TaskDao
) {
    fun observeActive(): Flow<List<TaskEntity>> = dao.observeActive()
    suspend fun create(task: TaskEntity): Long = dao.insert(task)
    suspend fun update(task: TaskEntity) = dao.update(task)
    suspend fun delete(taskId: Long, deletedAt: Long) = dao.softDelete(taskId, deletedAt)
}
