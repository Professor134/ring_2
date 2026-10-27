// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\RingApplication.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp

import android.app.Application
import androidx.work.Configuration
import com.example.ringapp.data.PreferencesDataStore
import com.example.ringapp.data.FirstLaunchInitializer
import dagger.hilt.android.HiltAndroidApp
import com.example.ringapp.work.WorkScheduler
import com.example.ringapp.work.RingWorkerFactory
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
// Handles the ring application logic for this component without modifying runtime behavior.
// Represents the ring application structure used by the application.
class RingApplication : Application(), Configuration.Provider {
	@Inject
	lateinit var preferences: PreferencesDataStore
	@Inject
	lateinit var firstLaunchInitializer: FirstLaunchInitializer

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setWorkerFactory(RingWorkerFactory(preferences))
			.build()

	override fun onCreate() {
		super.onCreate()
		CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
			firstLaunchInitializer.initialize()
		}
		WorkScheduler.scheduleDailyMaintenance(this)
	}
}
