package com.example.ringapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ringapp.data.local.entities.AchievementEntity
import com.example.ringapp.data.local.entities.BackupMetadataEntity
import com.example.ringapp.data.local.entities.CategoryEntity
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.local.entities.NotificationRecordEntity
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.data.local.entities.StreakCycleEntity
import com.example.ringapp.data.local.entities.StreakMilestoneEntity
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.local.entities.UserProgressEntity
import com.example.ringapp.data.local.entities.AppSettingsEntity
import com.example.ringapp.data.local.entities.HabitScheduleEntity
import com.example.ringapp.data.local.entities.TaskReminderEntity
import com.example.ringapp.data.local.entities.UserAchievementEntity
import com.example.ringapp.data.local.entities.AnalyticsDailyEntity
import com.example.ringapp.data.local.dao.HabitDao
import com.example.ringapp.data.local.dao.TaskDao
import com.example.ringapp.data.local.dao.CategoryDao
import com.example.ringapp.data.local.dao.PointTransactionDao
import com.example.ringapp.data.local.dao.ProfileDao
import com.example.ringapp.data.local.dao.UserProgressDao
import com.example.ringapp.data.local.dao.AchievementDao
import com.example.ringapp.data.local.dao.AppSettingsDao
import com.example.ringapp.data.local.dao.HabitScheduleDao
import com.example.ringapp.data.local.dao.TaskReminderDao
import com.example.ringapp.data.local.dao.UserAchievementDao
import com.example.ringapp.data.local.dao.AnalyticsDailyDao

@Database(
    entities = [
        HabitEntity::class,
        HabitProgressEntity::class,
        StreakCycleEntity::class,
        StreakMilestoneEntity::class,
        TaskEntity::class,
        PointTransactionEntity::class,
        UserProgressEntity::class,
        ProfileEntity::class,
        CategoryEntity::class,
        AchievementEntity::class,
        NotificationRecordEntity::class,
        BackupMetadataEntity::class,
        AppSettingsEntity::class,
        HabitScheduleEntity::class,
        TaskReminderEntity::class,
        UserAchievementEntity::class,
        AnalyticsDailyEntity::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun pointTransactionDao(): PointTransactionDao
    abstract fun profileDao(): ProfileDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun achievementDao(): AchievementDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun habitScheduleDao(): HabitScheduleDao
    abstract fun taskReminderDao(): TaskReminderDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun analyticsDailyDao(): AnalyticsDailyDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ring_database"
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Initial seed can be done here or via a worker
                }
            })
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .build()
            .also { instance = it }
        }
    }
}
