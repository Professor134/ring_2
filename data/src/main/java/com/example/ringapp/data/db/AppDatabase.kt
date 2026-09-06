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
import com.example.ringapp.data.local.dao.HabitDao
import com.example.ringapp.data.local.dao.TaskDao

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
        BackupMetadataEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ring_database"
            ).addMigrations(MIGRATION_4_5).build().also { instance = it }
        }
    }
}
