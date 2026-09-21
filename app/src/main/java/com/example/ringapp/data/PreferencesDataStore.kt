package com.example.ringapp.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("ring_preferences")
    }

    val theme: Flow<String> = dataStore.data.map { it[THEME] ?: "SYSTEM" }
    val language: Flow<String> = dataStore.data.map { it[LANGUAGE] ?: "en" }
    val onboardingComplete: Flow<Boolean> = dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }
    val initializationState: Flow<String> = dataStore.data.map { it[INITIALIZATION_STATE] ?: "NOT_STARTED" }
    val syncEnabled: Flow<Boolean> = dataStore.data.map { it[SYNC_ENABLED] ?: false }
    val lastSyncAt: Flow<Long?> = dataStore.data.map { it[LAST_SYNC_AT] }
    val dynamicColor: Flow<Boolean> = dataStore.data.map { it[DYNAMIC_COLOR] ?: false }
    val stepsBase: Flow<Int> = dataStore.data.map { it[STEPS_BASE] ?: -1 }
    val stepsDay: Flow<String> = dataStore.data.map { it[STEPS_DAY] ?: "" }

    suspend fun setTheme(value: String) = dataStore.edit { it[THEME] = value }
    suspend fun setLanguage(value: String) = dataStore.edit { it[LANGUAGE] = value }
    suspend fun setOnboardingComplete(value: Boolean) = dataStore.edit { it[ONBOARDING_COMPLETE] = value }
    suspend fun setInitializationState(value: String) = dataStore.edit { it[INITIALIZATION_STATE] = value }
    suspend fun setSyncEnabled(value: Boolean) = dataStore.edit { it[SYNC_ENABLED] = value }
    suspend fun setLastSyncAt(value: Long) = dataStore.edit { it[LAST_SYNC_AT] = value }
    suspend fun setDynamicColor(value: Boolean) = dataStore.edit { it[DYNAMIC_COLOR] = value }
    suspend fun setStepsBase(value: Int) = dataStore.edit { it[STEPS_BASE] = value }
    suspend fun setStepsDay(value: String) = dataStore.edit { it[STEPS_DAY] = value }

    private companion object {
        val THEME = stringPreferencesKey("theme_preference")
        val LANGUAGE = stringPreferencesKey("language")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val INITIALIZATION_STATE = stringPreferencesKey("initialization_state")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val LAST_SYNC_AT = androidx.datastore.preferences.core.longPreferencesKey("last_sync_at")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val STEPS_BASE = androidx.datastore.preferences.core.intPreferencesKey("steps_base")
        val STEPS_DAY = stringPreferencesKey("steps_day")
    }
}
