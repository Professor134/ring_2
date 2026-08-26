package com.example.ring_2.data

import android.content.Context
import androidx.room.*
import com.example.ring_2.data.dao.*
import com.example.ring_2.data.model.*

@Database(
    entities = [
        Habit::class,
        HabitRecord::class,
        Task::class,
        Category::class,
        PointTransaction::class,
        UserProfile::class,
        Achievement::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ring_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
