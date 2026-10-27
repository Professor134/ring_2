// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\TaskReminderDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringapp.data.local.entities.TaskReminderEntity

@Dao
// Handles the task reminder dao logic for this component without modifying runtime behavior.
// Represents the task reminder dao structure used by the application.
interface TaskReminderDao {
    @Query("SELECT * FROM task_reminders WHERE isEnabled = 1 AND triggerAt >= :now ORDER BY triggerAt")
    suspend fun activeFrom(now: Long): List<TaskReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: TaskReminderEntity): Long

    @Query("UPDATE task_reminders SET isEnabled = 0, updatedAt = :updatedAt WHERE taskId = :taskId")
    suspend fun disableForTask(taskId: Long, updatedAt: Long)
}
