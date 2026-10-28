// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\RingApplication.kt
// Purpose: Application class configuring WorkManager and initializing startup services.
// -----------------------------------------------------------------------------

package com.example.ringapp

import android.app.Application
import androidx.work.Configuration
import com.example.ringapp.data.PreferencesDataStore
import com.example.ringapp.data.FirstLaunchInitializer
import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.repository.TaskRepository
import dagger.hilt.android.HiltAndroidApp
import com.example.ringapp.work.WorkScheduler
import com.example.ringapp.work.RingWorkerFactory
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class RingApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var preferences: PreferencesDataStore
    @Inject
    lateinit var firstLaunchInitializer: FirstLaunchInitializer
    @Inject
    lateinit var habitRepository: HabitRepository
    @Inject
    lateinit var taskRepository: TaskRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(RingWorkerFactory(preferences, habitRepository, taskRepository))
            .build()

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            firstLaunchInitializer.initialize()
        }
        WorkScheduler.scheduleDailyMaintenance(this)
    }
}
