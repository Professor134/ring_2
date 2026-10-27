// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\data\ResetUserDataService.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data

import javax.inject.Inject
import javax.inject.Singleton
import com.example.ringapp.data.repository.UserDataRepository

@Singleton
// Handles the reset user data service logic for this component without modifying runtime behavior.
// Represents the reset user data service structure used by the application.
class ResetUserDataService @Inject constructor(
    private val preferences: PreferencesDataStore,
    private val initializer: FirstLaunchInitializer,
    private val userDataRepository: UserDataRepository
) {
    suspend fun reset() {
        userDataRepository.clearUserData()
        preferences.setInitializationState(InitializationState.NOT_STARTED.value)
        preferences.setOnboardingComplete(false)
        initializer.initialize()
    }
}
