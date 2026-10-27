// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\db\Converters.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.db

import androidx.room.TypeConverter
import com.example.ringapp.data.local.entities.AchievementCategory
import com.example.ringapp.data.local.entities.BackupStatus
import com.example.ringapp.data.local.entities.BackupType
import com.example.ringapp.data.local.entities.HabitType
import com.example.ringapp.data.local.entities.NotificationType
import com.example.ringapp.data.local.entities.RepeatType
import com.example.ringapp.data.local.entities.ScheduleType
import com.example.ringapp.data.local.entities.SubtaskItem
import com.example.ringapp.data.local.entities.TaskPriority
import com.example.ringapp.data.local.entities.ThemeMode
import com.example.ringapp.data.local.entities.TransactionType
import org.json.JSONArray
import org.json.JSONObject

// Handles the converters logic for this component without modifying runtime behavior.
// Represents the converters structure used by the application.
class Converters {
// Manages the from int list flow without changing runtime behavior.
    @TypeConverter
// Manages the from int list flow without changing runtime behavior.
    fun fromIntList(value: List<Int>?): String? = value?.let { JSONArray(it).toString() }

// Manages the to int list flow without changing runtime behavior.
    @TypeConverter
// Manages the to int list flow without changing runtime behavior.
    fun toIntList(value: String?): List<Int>? = value?.let { json ->
        JSONArray(json).let { array -> List(array.length()) { index -> array.getInt(index) } }
    }

// Manages the from subtask list flow without changing runtime behavior.
    @TypeConverter
// Manages the from subtask list flow without changing runtime behavior.
    fun fromSubtaskList(value: List<SubtaskItem>?): String? = value?.let { list ->
        val arr = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("completed", item.completed)
            }
            arr.put(obj)
        }
        arr.toString()
    }

// Manages the to subtask list flow without changing runtime behavior.
    @TypeConverter
// Manages the to subtask list flow without changing runtime behavior.
    fun toSubtaskList(value: String?): List<SubtaskItem>? = value?.let { json ->
        try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                SubtaskItem(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    title = obj.getString("title"),
                    completed = obj.getBoolean("completed")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter fun habitTypeToString(value: HabitType): String = value.name
    @TypeConverter fun stringToHabitType(value: String): HabitType = HabitType.valueOf(value)
    @TypeConverter fun scheduleTypeToString(value: ScheduleType): String = value.name
    @TypeConverter fun stringToScheduleType(value: String): ScheduleType = ScheduleType.valueOf(value)
    @TypeConverter fun priorityToString(value: TaskPriority): String = value.name
    @TypeConverter fun stringToPriority(value: String): TaskPriority = TaskPriority.valueOf(value)
    @TypeConverter fun repeatTypeToString(value: RepeatType): String = value.name
    @TypeConverter fun stringToRepeatType(value: String): RepeatType = RepeatType.valueOf(value)
    @TypeConverter fun transactionTypeToString(value: TransactionType): String = value.name
    @TypeConverter fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
    @TypeConverter fun themeModeToString(value: ThemeMode): String = value.name
    @TypeConverter fun stringToThemeMode(value: String): ThemeMode = ThemeMode.valueOf(value)
    @TypeConverter fun achievementCategoryToString(value: AchievementCategory): String = value.name
    @TypeConverter fun stringToAchievementCategory(value: String): AchievementCategory = AchievementCategory.valueOf(value)
    @TypeConverter fun notificationTypeToString(value: NotificationType): String = value.name
    @TypeConverter fun stringToNotificationType(value: String): NotificationType = NotificationType.valueOf(value)
    @TypeConverter fun backupTypeToString(value: BackupType): String = value.name
    @TypeConverter fun stringToBackupType(value: String): BackupType = BackupType.valueOf(value)
    @TypeConverter fun backupStatusToString(value: BackupStatus): String = value.name
    @TypeConverter fun stringToBackupStatus(value: String): BackupStatus = BackupStatus.valueOf(value)
}
