package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringapp.data.local.entities.TaskReminderEntity

@Dao
interface TaskReminderDao {
    @Query("SELECT * FROM task_reminders WHERE isEnabled = 1 AND triggerAt >= :now ORDER BY triggerAt")
    suspend fun activeFrom(now: Long): List<TaskReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: TaskReminderEntity): Long

    @Query("UPDATE task_reminders SET isEnabled = 0, updatedAt = :updatedAt WHERE taskId = :taskId")
    suspend fun disableForTask(taskId: Long, updatedAt: Long)
}
