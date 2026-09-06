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
    val syncEnabled: Flow<Boolean> = dataStore.data.map { it[SYNC_ENABLED] ?: false }
    val lastSyncAt: Flow<Long?> = dataStore.data.map { it[LAST_SYNC_AT] }

    suspend fun setTheme(value: String) = dataStore.edit { it[THEME] = value }
    suspend fun setLanguage(value: String) = dataStore.edit { it[LANGUAGE] = value }
    suspend fun setOnboardingComplete(value: Boolean) = dataStore.edit { it[ONBOARDING_COMPLETE] = value }
    suspend fun setSyncEnabled(value: Boolean) = dataStore.edit { it[SYNC_ENABLED] = value }
    suspend fun setLastSyncAt(value: Long) = dataStore.edit { it[LAST_SYNC_AT] = value }

    private companion object {
        val THEME = stringPreferencesKey("theme_preference")
        val LANGUAGE = stringPreferencesKey("language")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val LAST_SYNC_AT = androidx.datastore.preferences.core.longPreferencesKey("last_sync_at")
    }
}
