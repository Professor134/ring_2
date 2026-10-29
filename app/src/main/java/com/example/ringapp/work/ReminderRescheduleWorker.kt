// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\ReminderRescheduleWorker.kt
// Purpose: Reschedules active task alarms and 30-day BMI reminders on boot or app restart.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ringapp.data.db.AppDatabase
import org.json.JSONArray

class ReminderRescheduleWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)
        val database = AppDatabase.getDatabase(applicationContext)

        // Reschedule active task reminders
        database.taskDao().activeReminders().forEach { task ->
            val time = task.reminderTime ?: return@forEach
            val intent = Intent(applicationContext, ReminderAlarmReceiver::class.java)
                .putExtra("taskId", task.id)
                .putExtra("title", task.title)
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                task.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }

        // Reschedule BMI 30-day reminder relative to latest measurement date
        val profile = database.profileDao().getCurrent()
        if (profile != null) {
            val measurementsStr = profile.bodyMeasurements
            if (!measurementsStr.isNullOrBlank()) {
                try {
                    val array = JSONArray(measurementsStr)
                    var latestDate = profile.createdAt
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val date = obj.optLong("date", 0L)
                        if (date > latestDate) latestDate = date
                    }
                    val reminderTime = latestDate + (30L * 24 * 60 * 60 * 1000L)
                    BmiReminderScheduler.scheduleBmiReminder(applicationContext, reminderTime)
                } catch (e: Exception) {
                    android.util.Log.e("ReminderRescheduleWorker", "Error processing BMI measurements", e)
                }
            }
        }

        return Result.success()
    }
}
