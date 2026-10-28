// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\AppViewModel.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.PreferencesDataStore
import com.example.ringapp.data.repository.ProfileRepository
import com.example.ringapp.ui.onboarding.OnboardingDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
// Handles the app view model logic for this component without modifying runtime behavior.
// Represents the app view model structure used by the application.
class AppViewModel @Inject constructor(
    private val preferences: PreferencesDataStore,
    private val profileRepository: ProfileRepository,
    private val database: com.example.ringapp.data.db.AppDatabase
) : ViewModel() {
    val state: StateFlow<AppState> = combine(
        combine(preferences.onboardingComplete, database.profileDao().observeCurrent()) { complete, profile ->
            complete || profile?.onboardingComplete == true
        },
        preferences.initializationState,
        preferences.theme,
        preferences.syncEnabled,
        preferences.lastSyncAt
    ) { onboardingComplete, initializationState, theme, syncEnabled, lastSyncAt ->
        AppState(
            onboardingComplete = onboardingComplete,
            initializationState = initializationState,
            theme = theme,
            syncEnabled = syncEnabled,
            lastSyncAt = lastSyncAt,
            ready = initializationState == "COMPLETE"
        )
    }
        .combine(preferences.firstLaunchSplashSeen) { appState, splashSeen ->
            appState.copy(firstLaunchSplashSeen = splashSeen)
        }
        .combine(preferences.dynamicColor) { appState, dynamicColor ->
            appState.copy(dynamicColor = dynamicColor)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState())

    fun markFirstLaunchSplashSeen() = viewModelScope.launch {
        preferences.setFirstLaunchSplashSeen(true)
    }

    fun completeOnboarding(
        details: OnboardingDetails,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            require(details.name.isNotBlank()) { "Enter your name to continue." }
            require(details.weightKg in 1.0..500.0) { "Weight must be between 1 and 500 kg." }
            require(details.heightCm in 30.0..300.0) { "Height must be between 30 and 300 cm." }
            require(details.gender.isNotBlank()) { "Choose your gender to continue." }
            val dateOfBirth = Instant.ofEpochMilli(details.dateOfBirth)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            require(!dateOfBirth.isAfter(LocalDate.now())) { "Date of birth cannot be in the future." }

            profileRepository.completeOnboarding(
                name = details.name,
                dateOfBirth = details.dateOfBirth,
                weightKg = details.weightKg,
                heightCm = details.heightCm,
                gender = details.gender
            )
            preferences.setOnboardingComplete(true)
            preferences.setFirstLaunchSplashSeen(true)
            onComplete()
        } catch (error: Exception) {
            Log.e("AppViewModel", "Unable to complete first-run onboarding", error)
            onError(error.message ?: "Unable to save your profile. Please try again.")
        }
    }

// Manages the set sync enabled flow without changing runtime behavior.
    fun setSyncEnabled(enabled: Boolean) = viewModelScope.launch {
        preferences.setSyncEnabled(enabled)
    }

// Manages the set theme flow without changing runtime behavior.
    fun setTheme(theme: String) = viewModelScope.launch { preferences.setTheme(theme) }
// Manages the set dynamic color flow without changing runtime behavior.
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { preferences.setDynamicColor(enabled) }
}

// Handles the app state logic for this component without modifying runtime behavior.
// Represents the app state structure used by the application.
data class AppState(
    val onboardingComplete: Boolean = false,
    val initializationState: String = "NOT_STARTED",
    val theme: String = "SYSTEM",
    val syncEnabled: Boolean = false,
    val lastSyncAt: Long? = null,
    val dynamicColor: Boolean = false,
    val ready: Boolean = false,
    val firstLaunchSplashSeen: Boolean = false
)
