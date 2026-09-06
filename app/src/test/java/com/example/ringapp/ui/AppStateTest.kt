package com.example.ringapp.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStateTest {
    @Test
    fun newUsers_startWithOnboarding() {
        assertFalse(AppState().onboardingComplete)
    }

    @Test
    fun syncStatus_isDisabledByDefault() {
        assertFalse(AppState().syncEnabled)
        assertTrue(AppState(syncEnabled = true).syncEnabled)
    }
}
