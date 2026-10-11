// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\di\DataModule.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.di

import android.content.Context
import androidx.room.Room
import com.example.ringapp.data.db.AppDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
// Handles the data module logic for this component without modifying runtime behavior.
// Represents the data module structure used by the application.
object DataModule {
    @Provides
// Manages the provide database flow without changing runtime behavior.
    @Singleton
// Manages the provide database flow without changing runtime behavior.
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "ring_database")
            .addMigrations(com.example.ringapp.data.db.MIGRATION_4_5, com.example.ringapp.data.db.MIGRATION_5_6, com.example.ringapp.data.db.MIGRATION_6_7)
            .build()

    @Provides fun provideHabitDao(database: AppDatabase): HabitDao = database.habitDao()
    @Provides fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()
    @Provides fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()
    @Provides fun providePointTransactionDao(database: AppDatabase): PointTransactionDao = database.pointTransactionDao()
    @Provides fun provideProfileDao(database: AppDatabase): ProfileDao = database.profileDao()
    @Provides fun provideUserProgressDao(database: AppDatabase): UserProgressDao = database.userProgressDao()
    @Provides fun provideAchievementDao(database: AppDatabase): AchievementDao = database.achievementDao()
    @Provides fun provideAppSettingsDao(database: AppDatabase): AppSettingsDao = database.appSettingsDao()
    @Provides fun provideHabitScheduleDao(database: AppDatabase): HabitScheduleDao = database.habitScheduleDao()
    @Provides fun provideTaskReminderDao(database: AppDatabase): TaskReminderDao = database.taskReminderDao()
    @Provides fun provideUserAchievementDao(database: AppDatabase): UserAchievementDao = database.userAchievementDao()
    @Provides fun provideAnalyticsDailyDao(database: AppDatabase): AnalyticsDailyDao = database.analyticsDailyDao()
}
