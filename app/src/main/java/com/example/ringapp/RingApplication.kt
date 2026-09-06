package com.example.ringapp

import android.app.Application
import androidx.work.Configuration
import com.example.ringapp.data.PreferencesDataStore
import dagger.hilt.android.HiltAndroidApp
import com.example.ringapp.work.WorkScheduler
import com.example.ringapp.work.RingWorkerFactory
import javax.inject.Inject

@HiltAndroidApp
class RingApplication : Application(), Configuration.Provider {
	@Inject
	lateinit var preferences: PreferencesDataStore

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setWorkerFactory(RingWorkerFactory(preferences))
			.build()

	override fun onCreate() {
		super.onCreate()
		WorkScheduler.scheduleDailyMaintenance(this)
	}
}
