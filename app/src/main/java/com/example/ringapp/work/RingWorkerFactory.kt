// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\RingWorkerFactory.kt
// Purpose: Worker factory for initializing WorkManager workers with dependencies.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.ringapp.data.PreferencesDataStore
import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.repository.TaskRepository

class RingWorkerFactory(
    private val preferences: PreferencesDataStore,
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        DailyMaintenanceWorker::class.java.name -> DailyMaintenanceWorker(appContext, workerParameters).also {
            it.preferences = preferences
            it.habitRepository = habitRepository
            it.taskRepository = taskRepository
        }
        MotivationWorker::class.java.name -> MotivationWorker(appContext, workerParameters)
        else -> null
    }
}
