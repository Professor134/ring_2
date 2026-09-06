package com.example.ringapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ringapp.data.PreferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

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
