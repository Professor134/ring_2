package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task WHERE deletedAt IS NULL ORDER BY completed, dueDate, title")
    fun observeActive(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE task SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :taskId")
    suspend fun softDelete(taskId: Long, deletedAt: Long)
}
