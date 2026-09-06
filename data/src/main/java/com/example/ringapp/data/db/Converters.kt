package com.example.ringapp.data.db

import androidx.room.TypeConverter
import com.example.ringapp.data.local.entities.AchievementCategory
import com.example.ringapp.data.local.entities.BackupStatus
import com.example.ringapp.data.local.entities.BackupType
import com.example.ringapp.data.local.entities.HabitType
import com.example.ringapp.data.local.entities.NotificationType
import com.example.ringapp.data.local.entities.RepeatType
import com.example.ringapp.data.local.entities.ScheduleType
import com.example.ringapp.data.local.entities.TaskPriority
import com.example.ringapp.data.local.entities.ThemeMode
import com.example.ringapp.data.local.entities.TransactionType
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? = value?.let { JSONArray(it).toString() }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? = value?.let { json ->
        JSONArray(json).let { array -> List(array.length()) { index -> array.getInt(index) } }
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
