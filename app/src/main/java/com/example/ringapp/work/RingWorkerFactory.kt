// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\RingWorkerFactory.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.ringapp.data.PreferencesDataStore

// Handles the ring worker factory logic for this component without modifying runtime behavior.
// Represents the ring worker factory structure used by the application.
class RingWorkerFactory(
    private val preferences: PreferencesDataStore
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        DailyMaintenanceWorker::class.java.name -> DailyMaintenanceWorker(appContext, workerParameters).also { it.preferences = preferences }
        MotivationWorker::class.java.name -> MotivationWorker(appContext, workerParameters)
        else -> null
    }
}