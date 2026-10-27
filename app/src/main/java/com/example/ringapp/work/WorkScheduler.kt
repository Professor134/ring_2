// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\WorkScheduler.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// Handles the work scheduler logic for this component without modifying runtime behavior.
// Represents the work scheduler structure used by the application.
object WorkScheduler {
    private const val DAILY_MAINTENANCE = "daily_maintenance"
    private const val MOTIVATION_WORK = "motivation_work"

// Manages the schedule daily maintenance flow without changing runtime behavior.
    fun scheduleDailyMaintenance(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyMaintenanceWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_MAINTENANCE,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        val motivationRequest = PeriodicWorkRequestBuilder<MotivationWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MOTIVATION_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            motivationRequest
        )
    }

// Manages the schedule sync flow without changing runtime behavior.
    fun scheduleSync(context: Context) = scheduleDailyMaintenance(context)
}