// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\DailyMaintenanceWorker.kt
// Purpose: Daily maintenance worker for sync, skipped day penalties, and overdue tasks.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ringapp.data.PreferencesDataStore
import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.first

class DailyMaintenanceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    lateinit var preferences: PreferencesDataStore
    lateinit var habitRepository: HabitRepository
    lateinit var taskRepository: TaskRepository

    override suspend fun doWork(): Result {
        if (::preferences.isInitialized && preferences.syncEnabled.first()) {
            preferences.setLastSyncAt(System.currentTimeMillis())
        }
        if (::habitRepository.isInitialized) {
            habitRepository.processSkippedDayPenalties()
        }
        if (::taskRepository.isInitialized) {
            taskRepository.checkAndApplyOverdueTaskPenalties()
            taskRepository.cleanupOldTasks()
        }
        return Result.success()
    }
}
