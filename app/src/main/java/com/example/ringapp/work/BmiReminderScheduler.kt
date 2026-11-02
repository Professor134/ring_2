// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\BmiReminderScheduler.kt
// Purpose: Schedules exact RTC alarm for 30-day BMI measurement update reminders.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object BmiReminderScheduler {
    const val BMI_REMINDER_ID = 9999

    fun scheduleBmiReminder(context: Context, reminderTime: Long) {
        val intent = Intent(context, BmiReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            BMI_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }

    fun cancel(context: Context) {
        val intent = Intent(context, BmiReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            BMI_REMINDER_ID,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
