// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\TaskReminderScheduler.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.ringapp.data.local.entities.TaskEntity
import java.util.Calendar

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Handles the task reminder scheduler logic for this component without modifying runtime behavior.
// Represents the task reminder scheduler structure used by the application.
object TaskReminderScheduler {
    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.
// Manages the sync flow without changing runtime behavior.
    fun sync(context: Context, task: TaskEntity, remindersEnabled: Boolean) {
        if (!remindersEnabled || !task.reminderEnabled || task.completed) {
            cancel(context, task.id)
            return
        }

        val reminderTime = task.reminderTime
        if (reminderTime == null || reminderTime <= System.currentTimeMillis()) {
            cancel(context, task.id)
            return
        }

        val intent = reminderIntent(context, task.id).putExtra("title", task.title)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the cancel flow without changing runtime behavior.
    fun cancel(context: Context, taskId: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            reminderIntent(context, taskId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        pendingIntent.cancel()
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the reminder time flow without changing runtime behavior.
    fun reminderTime(date: Long?, time: Long?): Long? {
        if (date == null || time == null) return null
        val dateCalendar = Calendar.getInstance().apply { timeInMillis = date }
        val timeCalendar = Calendar.getInstance().apply { timeInMillis = time }
        return dateCalendar.apply {
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the reminder intent flow without changing runtime behavior.
    private fun reminderIntent(context: Context, taskId: Long) =
        Intent(context, ReminderAlarmReceiver::class.java).putExtra("taskId", taskId)
}
