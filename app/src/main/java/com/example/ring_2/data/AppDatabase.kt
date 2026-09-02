package com.example.ring_2.data

import android.content.Context
import androidx.room.*
import com.example.ring_2.data.dao.*
import com.example.ring_2.data.model.*

@Database(
    entities = [
        HabitEntity::class,
        HabitProgressEntity::class,
        StreakCycleEntity::class,
        StreakMilestoneEntity::class,
        PointTransactionEntity::class,
        UserProgressEntity::class,
        ProfileEntity::class,
        TaskEntity::class,
        Category::class,
        Achievement::class,
        NotificationEntity::class
    ],
    version = 4, // Incremented version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun notificationDao(): NotificationDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ring_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Optional: Prepopulate default categories here via a coroutine
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
