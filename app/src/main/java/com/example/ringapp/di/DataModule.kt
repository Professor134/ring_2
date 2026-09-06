package com.example.ringapp.di

import android.content.Context
import androidx.room.Room
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.dao.HabitDao
import com.example.ringapp.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "ring_database")
            .addMigrations(com.example.ringapp.data.db.MIGRATION_4_5)
            .build()

    @Provides fun provideHabitDao(database: AppDatabase): HabitDao = database.habitDao()
    @Provides fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()
}
