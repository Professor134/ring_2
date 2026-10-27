// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\ReminderRescheduleWorker.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ringapp.data.db.AppDatabase

// Handles the reminder reschedule worker logic for this component without modifying runtime behavior.
// Represents the reminder reschedule worker structure used by the application.
class ReminderRescheduleWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)
        AppDatabase.getDatabase(applicationContext).taskDao().activeReminders().forEach { task ->
            val time = task.reminderTime ?: return@forEach
            val intent = Intent(applicationContext, ReminderAlarmReceiver::class.java).putExtra("taskId", task.id).putExtra("title", task.title)
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
        return Result.success()
    }
}