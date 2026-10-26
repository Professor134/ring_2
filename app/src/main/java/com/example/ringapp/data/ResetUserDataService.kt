package com.example.ringapp.data

import javax.inject.Inject
import javax.inject.Singleton
import com.example.ringapp.data.repository.UserDataRepository

@Singleton
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
