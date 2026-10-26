package com.example.ringapp.data

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.entities.CategoryConstants
import com.example.ringapp.data.local.entities.CategoryEntity
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.data.local.entities.ThemeMode
import com.example.ringapp.data.local.entities.AchievementCategory
import com.example.ringapp.data.local.entities.AchievementEntity
import com.example.ringapp.data.local.entities.TransactionType
import com.example.ringapp.data.local.entities.UserProgressEntity
import com.example.ringapp.data.local.entities.AppSettingsEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class FirstLaunchInitializer @Inject constructor(
    private val database: AppDatabase,
    private val preferences: PreferencesDataStore
) {
    suspend fun initialize() {
        if (preferences.initializationState.first() == InitializationState.COMPLETE.value) return

        preferences.setInitializationState(InitializationState.IN_PROGRESS.value)
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.profileDao().insert(
                ProfileEntity(
                    name = "",
                    avatarColor = 0xFF6750A4.toInt(),
                    themePreference = ThemeMode.DARK,
                    createdAt = now,
                    updatedAt = now
                )
            )
            database.userProgressDao().insert(
                UserProgressEntity(
                    currentPoints = 500,
                    lifetimePoints = 0,
                    level = 1,
                    createdAt = now,
                    updatedAt = now
                )
            )
            database.appSettingsDao().upsert(AppSettingsEntity(onboardingCompleted = false, updatedAt = now))
            database.categoryDao().insertAll(
                CategoryConstants.ALL_CATEGORIES.map { cat ->
                    CategoryEntity(name = cat.name, color = cat.color, icon = cat.icon, createdAt = now, updatedAt = now)
                }
            )
            // Add Default Habit: Steps
            database.habitDao().insert(
                com.example.ringapp.data.local.entities.HabitEntity(
                    id = com.example.ringapp.data.local.entities.HabitEntity.STEPS_HABIT_ID,
                    name = com.example.ringapp.data.local.entities.HabitEntity.STEPS_HABIT_NAME,
                    description = "Track your daily activity automatically.",
                    categoryId = null,
                    type = com.example.ringapp.data.local.entities.HabitType.MEASURABLE,
                    target = 10000.0,
                    unit = "Steps",
                    scheduleType = com.example.ringapp.data.local.entities.ScheduleType.DAILY,
                    scheduleDays = emptyList(),
                    startDate = now,
                    color = com.example.ringapp.data.local.entities.HabitEntity.PLATINUM_COLOR,
                    createdAt = now,
                    updatedAt = now
                )
            )
            database.achievementDao().insertAll(
                listOf(
                    AchievementCategory.STREAK to 7,
                    AchievementCategory.STREAK to 30,
                    AchievementCategory.HABITS to 100,
                    AchievementCategory.POINTS to 500,
                    AchievementCategory.LEVELS to 10,
                    AchievementCategory.HABITS to 10
                ).mapIndexed { index, (category, threshold) ->
                    AchievementEntity(id = index + 1L, category = category, threshold = threshold, description = when (index) {
                        0 -> "7 Day Streak"; 1 -> "30 Day Streak"; 2 -> "100 Habit Completions"; 3 -> "500 Points Earned"; 4 -> "Level 10"; else -> "10 Habits Created"
                    }, pointsAwarded = 25, createdAt = now, updatedAt = now)
                }
            )
            database.pointTransactionDao().insert(
                PointTransactionEntity(
                    amount = 500,
                    type = TransactionType.STARTING_POINTS,
                    description = "Starting points",
                    uniqueReference = "initial-points-v1",
                    timestamp = now,
                    createdAt = now
                )
            )
        }
        preferences.setTheme(ThemeMode.DARK.name)
        preferences.setSyncEnabled(false)
        preferences.setOnboardingComplete(false)
        preferences.setInitializationState(InitializationState.COMPLETE.value)
    }
}

enum class InitializationState(val value: String) {
    NOT_STARTED("NOT_STARTED"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETE("COMPLETE")
}