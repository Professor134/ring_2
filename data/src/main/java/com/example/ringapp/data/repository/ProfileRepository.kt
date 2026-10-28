// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\ProfileRepository.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.dao.ProfileDao
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.local.entities.HabitType
import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.data.local.entities.ScheduleType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

// Handles the profile repository logic for this component without modifying runtime behavior.
// Represents the profile repository structure used by the application.
class ProfileRepository @Inject constructor(
    private val dao: ProfileDao,
    private val database: AppDatabase
) {
// Manages the observe current flow without changing runtime behavior.
    fun observeCurrent(): Flow<ProfileEntity?> = dao.observeCurrent()
    suspend fun update(profile: ProfileEntity) = dao.insert(profile)

    suspend fun completeOnboarding(
        name: String,
        dateOfBirth: Long,
        weightKg: Double,
        heightCm: Double,
        gender: String
    ) = database.withTransaction {
        val now = System.currentTimeMillis()
        val profile = dao.getCurrent()
            ?: ProfileEntity(
                name = name.trim(),
                avatarColor = 0xFF6750A4.toInt(),
                themePreference = com.example.ringapp.data.local.entities.ThemeMode.DARK,
                createdAt = now,
                updatedAt = now
            ).also { dao.insert(it) }
        if (profile.onboardingComplete) return@withTransaction
        val bmi = weightKg / (heightCm / 100.0 * (heightCm / 100.0))
        val measurements = profile.bodyMeasurements?.let { JSONArray(it) } ?: JSONArray()
        measurements.put(
            JSONObject().apply {
                put("date", now)
                put("weightKg", weightKg)
                put("heightCm", heightCm)
                put("bmi", bmi)
            }
        )

        val habitDao = database.habitDao()
        val bmiHabit = habitDao.observeActive().first().firstOrNull { it.isBmiHabit() }
            ?: HabitEntity(
                id = HabitEntity.BMI_HABIT_ID,
                name = HabitEntity.BMI_HABIT_NAME,
                categoryId = null,
                type = HabitType.MEASURABLE,
                target = 25.0,
                unit = "BMI",
                scheduleType = ScheduleType.DAILY,
                startDate = now,
                color = HabitEntity.PLATINUM_COLOR,
                createdAt = now,
                updatedAt = now
            ).also { habitDao.insert(it) }
        val dayStart = java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val existing = habitDao.progressForDate(bmiHabit.id, dayStart)
        habitDao.upsertProgress(
            HabitProgressEntity(
                id = existing?.id ?: 0L,
                habitId = bmiHabit.id,
                date = dayStart,
                target = bmiHabit.target,
                actual = bmi,
                percentage = (bmi / bmiHabit.target * 100).toInt().coerceIn(0, 100),
                completed = true,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
        dao.insert(
            profile.copy(
                name = name.trim(),
                dateOfBirth = dateOfBirth,
                gender = gender,
                weightKg = weightKg,
                heightCm = heightCm,
                bodyMeasurements = measurements.toString(),
                onboardingComplete = true,
                updatedAt = now
            )
        )
    }
}