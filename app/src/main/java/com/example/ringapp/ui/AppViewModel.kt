// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\AppViewModel.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
// Handles the app view model logic for this component without modifying runtime behavior.
// Represents the app view model structure used by the application.
class AppViewModel @Inject constructor(
    private val preferences: PreferencesDataStore
) : ViewModel() {
    val state: StateFlow<AppState> = combine(
        preferences.onboardingComplete,
        preferences.initializationState,
        preferences.theme,
        preferences.syncEnabled,
        preferences.lastSyncAt
    ) { onboardingComplete, initializationState, theme, syncEnabled, lastSyncAt ->
        AppState(onboardingComplete, initializationState, theme, syncEnabled, lastSyncAt)
    }.combine(preferences.dynamicColor) { appState, dynamicColor -> appState.copy(dynamicColor = dynamicColor) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState())

// Manages the complete onboarding flow without changing runtime behavior.
    fun completeOnboarding() = viewModelScope.launch {
        preferences.setOnboardingComplete(true)
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
    val dynamicColor: Boolean = false
)
