// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\TaskDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ringapp.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the task dao logic for this component without modifying runtime behavior.
// Represents the task dao structure used by the application.
interface TaskDao {
// Manages the observe active flow without changing runtime behavior.
    @Query("SELECT * FROM task WHERE deletedAt IS NULL ORDER BY completed, dueDate, title")
// Manages the observe active flow without changing runtime behavior.
    fun observeActive(): Flow<List<TaskEntity>>

// Manages the observe by id flow without changing runtime behavior.
    @Query("SELECT * FROM task WHERE id = :taskId LIMIT 1")
// Manages the observe by id flow without changing runtime behavior.
    fun observeById(taskId: Long): Flow<TaskEntity?>

// Manages the observe today flow without changing runtime behavior.
    @Query("SELECT * FROM task WHERE deletedAt IS NULL AND (dueDate IS NULL OR dueDate BETWEEN :start AND :end) ORDER BY completed, dueDate, title")
// Manages the observe today flow without changing runtime behavior.
    fun observeToday(start: Long, end: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE deletedAt IS NULL AND reminderEnabled = 1 AND reminderTime IS NOT NULL AND completed = 0")
    suspend fun activeReminders(): List<TaskEntity>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE task SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :taskId")
    suspend fun softDelete(taskId: Long, deletedAt: Long)

    @Query("DELETE FROM task WHERE completed = 1 AND completedAt < :threshold")
    suspend fun deleteCompletedBefore(threshold: Long)
}
