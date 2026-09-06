package com.example.ringapp.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val DAILY_MAINTENANCE = "daily_maintenance"

    fun scheduleDailyMaintenance(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyMaintenanceWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_MAINTENANCE,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun scheduleSync(context: Context) = scheduleDailyMaintenance(context)
}