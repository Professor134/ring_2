// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\ReminderRescheduleWorker.kt
// Purpose: Reschedules active task alarms and 30-day BMI reminders on boot or app restart.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ringapp.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import org.json.JSONArray

class ReminderRescheduleWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    lateinit var preferences: com.example.ringapp.data.PreferencesDataStore

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)

        val taskRemindersEnabled = preferences.taskRemindersEnabled.first()
        database.taskDao().activeReminders().forEach { task ->
            TaskReminderScheduler.sync(applicationContext, task, taskRemindersEnabled)
        }

        // Reschedule BMI 30-day reminder relative to latest measurement date
        val profile = database.profileDao().getCurrent()
        val bmiRemindersEnabled = preferences.bmiRemindersEnabled.first()
        if (!bmiRemindersEnabled || profile == null || profile.bodyMeasurements.isNullOrBlank()) {
            BmiReminderScheduler.cancel(applicationContext)
        } else {
            val latestDate = try {
                val array = JSONArray(profile.bodyMeasurements)
                var latest = profile.createdAt
                for (i in 0 until array.length()) {
                    val date = array.getJSONObject(i).optLong("date", 0L)
                    if (date > latest) latest = date
                }
                latest
            } catch (error: Exception) {
                android.util.Log.e("ReminderRescheduleWorker", "Error processing BMI measurements", error)
                BmiReminderScheduler.cancel(applicationContext)
                return Result.failure()
            }
            val reminderTime = latestDate + (30L * 24 * 60 * 60 * 1000L)
            BmiReminderScheduler.scheduleBmiReminder(applicationContext, reminderTime)
        }

        return Result.success()
    }
}
