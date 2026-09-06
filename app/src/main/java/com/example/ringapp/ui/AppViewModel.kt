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
class AppViewModel @Inject constructor(
    private val preferences: PreferencesDataStore
) : ViewModel() {
    val state: StateFlow<AppState> = combine(
        preferences.onboardingComplete,
        preferences.theme,
        preferences.syncEnabled,
        preferences.lastSyncAt
    ) { onboardingComplete, theme, syncEnabled, lastSyncAt ->
        AppState(onboardingComplete, theme, syncEnabled, lastSyncAt)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState())

    fun completeOnboarding() = viewModelScope.launch {
        preferences.setOnboardingComplete(true)
    }

    fun setSyncEnabled(enabled: Boolean) = viewModelScope.launch {
        preferences.setSyncEnabled(enabled)
    }
}

data class AppState(
    val onboardingComplete: Boolean = false,
    val theme: String = "SYSTEM",
    val syncEnabled: Boolean = false,
    val lastSyncAt: Long? = null
)
