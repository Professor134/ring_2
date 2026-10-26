package com.example.ringapp.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.ringapp.data.PreferencesDataStore

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