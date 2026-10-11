// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\DailyMaintenanceWorker.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ringapp.data.PreferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// Handles the daily maintenance worker logic for this component without modifying runtime behavior.
// Represents the daily maintenance worker structure used by the application.
class DailyMaintenanceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    @Inject
    lateinit var preferences: PreferencesDataStore

    override suspend fun doWork(): Result {
        if (preferences.syncEnabled.first()) {
            preferences.setLastSyncAt(System.currentTimeMillis())
        }
        return Result.success()
    }
}
