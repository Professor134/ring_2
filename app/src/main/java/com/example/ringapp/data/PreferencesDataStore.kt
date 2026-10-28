// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\data\PreferencesDataStore.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey

@Singleton
// Handles the preferences data store logic for this component without modifying runtime behavior.
// Represents the preferences data store structure used by the application.
class PreferencesDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("ring_preferences")
    }

    val theme: Flow<String> = dataStore.data.map { it[THEME] ?: "SYSTEM" }
    val language: Flow<String> = dataStore.data.map { it[LANGUAGE] ?: "en" }
    val onboardingComplete: Flow<Boolean> = dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }
    val firstLaunchSplashSeen: Flow<Boolean> = dataStore.data.map { it[FIRST_LAUNCH_SPLASH_SEEN] ?: false }
    val initializationState: Flow<String> = dataStore.data.map { it[INITIALIZATION_STATE] ?: "NOT_STARTED" }
    val syncEnabled: Flow<Boolean> = dataStore.data.map { it[SYNC_ENABLED] ?: false }
    val lastSyncAt: Flow<Long?> = dataStore.data.map { it[LAST_SYNC_AT] }
    val dynamicColor: Flow<Boolean> = dataStore.data.map { it[DYNAMIC_COLOR] ?: false }
    val stepsBase: Flow<Int> = dataStore.data.map { it[STEPS_BASE] ?: -1 }
    val stepsDay: Flow<String> = dataStore.data.map { it[STEPS_DAY] ?: "" }
    val taskRemindersEnabled: Flow<Boolean> = dataStore.data.map { it[TASK_REMINDERS_ENABLED] ?: true }

    suspend fun setTheme(value: String) = dataStore.edit { it[THEME] = value }
    suspend fun setLanguage(value: String) = dataStore.edit { it[LANGUAGE] = value }
    suspend fun setOnboardingComplete(value: Boolean) = dataStore.edit { it[ONBOARDING_COMPLETE] = value }
    suspend fun setFirstLaunchSplashSeen(value: Boolean) = dataStore.edit { it[FIRST_LAUNCH_SPLASH_SEEN] = value }
    suspend fun setInitializationState(value: String) = dataStore.edit { it[INITIALIZATION_STATE] = value }
    suspend fun setSyncEnabled(value: Boolean) = dataStore.edit { it[SYNC_ENABLED] = value }
    suspend fun setLastSyncAt(value: Long) = dataStore.edit { it[LAST_SYNC_AT] = value }
    suspend fun setDynamicColor(value: Boolean) = dataStore.edit { it[DYNAMIC_COLOR] = value }
    suspend fun setStepsBase(value: Int) = dataStore.edit { it[STEPS_BASE] = value }
    suspend fun setStepsDay(value: String) = dataStore.edit { it[STEPS_DAY] = value }
    suspend fun setTaskRemindersEnabled(value: Boolean) = dataStore.edit { it[TASK_REMINDERS_ENABLED] = value }

    suspend fun exportSnapshot(): Map<String, Any> {
        val snapshot = linkedMapOf<String, Any>()
        dataStore.data.first().asMap().forEach { (key, value) ->
            if (key != INITIALIZATION_STATE) snapshot[key.name] = value
        }
        return snapshot
    }

    suspend fun replaceSnapshot(snapshot: Map<String, Any>) {
        val knownKeys = setOf(
            THEME.name,
            LANGUAGE.name,
            ONBOARDING_COMPLETE.name,
            FIRST_LAUNCH_SPLASH_SEEN.name,
            SYNC_ENABLED.name,
            LAST_SYNC_AT.name,
            DYNAMIC_COLOR.name,
            STEPS_BASE.name,
            STEPS_DAY.name,
            TASK_REMINDERS_ENABLED.name
        )
        require(snapshot.keys.all { it in knownKeys }) { "The backup contains an unsupported app setting." }

        dataStore.edit { preferences ->
            val initializationState = preferences[INITIALIZATION_STATE]
            preferences.clear()
            initializationState?.let { preferences[INITIALIZATION_STATE] = it }
            snapshot.forEach { (key, value) ->
                when (key) {
                    THEME.name -> preferences[THEME] = value as? String
                        ?: throw IllegalArgumentException("Invalid theme setting in backup.")
                    LANGUAGE.name -> preferences[LANGUAGE] = value as? String
                        ?: throw IllegalArgumentException("Invalid language setting in backup.")
                    ONBOARDING_COMPLETE.name -> preferences[ONBOARDING_COMPLETE] = value as? Boolean
                        ?: throw IllegalArgumentException("Invalid onboarding setting in backup.")
                    FIRST_LAUNCH_SPLASH_SEEN.name -> preferences[FIRST_LAUNCH_SPLASH_SEEN] = value as? Boolean
                        ?: throw IllegalArgumentException("Invalid splash setting in backup.")
                    SYNC_ENABLED.name -> preferences[SYNC_ENABLED] = value as? Boolean
                        ?: throw IllegalArgumentException("Invalid sync setting in backup.")
                    LAST_SYNC_AT.name -> preferences[LAST_SYNC_AT] = value as? Long
                        ?: (value as? Int)?.toLong()
                        ?: throw IllegalArgumentException("Invalid sync timestamp in backup.")
                    DYNAMIC_COLOR.name -> preferences[DYNAMIC_COLOR] = value as? Boolean
                        ?: throw IllegalArgumentException("Invalid color setting in backup.")
                    STEPS_BASE.name -> preferences[STEPS_BASE] = value as? Int
                        ?: throw IllegalArgumentException("Invalid step setting in backup.")
                    STEPS_DAY.name -> preferences[STEPS_DAY] = value as? String
                        ?: throw IllegalArgumentException("Invalid step date in backup.")
                    TASK_REMINDERS_ENABLED.name -> preferences[TASK_REMINDERS_ENABLED] = value as? Boolean
                        ?: throw IllegalArgumentException("Invalid reminder setting in backup.")
                }
            }
        }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_preference")
        val LANGUAGE = stringPreferencesKey("language")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val FIRST_LAUNCH_SPLASH_SEEN = booleanPreferencesKey("first_launch_splash_seen")
        val INITIALIZATION_STATE = stringPreferencesKey("initialization_state")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val LAST_SYNC_AT = androidx.datastore.preferences.core.longPreferencesKey("last_sync_at")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val STEPS_BASE = androidx.datastore.preferences.core.intPreferencesKey("steps_base")
        val STEPS_DAY = stringPreferencesKey("steps_day")
        val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
    }
}
