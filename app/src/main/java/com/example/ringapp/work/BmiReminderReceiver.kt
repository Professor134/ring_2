// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\BmiReminderReceiver.kt
// Purpose: Broadcast receiver for 30-day BMI update reminder notification.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ringapp.R

class BmiReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel("bmi_reminders", "BMI Updates", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val notification = NotificationCompat.Builder(context, "bmi_reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Update BMI Measurements")
            .setContentText("It's been 30 days since your last BMI record. Tap to update your weight & height.")
            .setAutoCancel(true)
            .build()

        manager.notify(2001, notification)
    }
}
