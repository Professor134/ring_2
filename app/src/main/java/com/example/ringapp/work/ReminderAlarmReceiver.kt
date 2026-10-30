// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\work\ReminderAlarmReceiver.kt
// Purpose: BroadcastReceiver for task reminders with custom notification tone support.
// -----------------------------------------------------------------------------

package com.example.ringapp.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ringapp.R

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val soundResId = context.resources.getIdentifier("ring_notification", "raw", context.packageName)
        val soundUri = if (soundResId != 0) {
            Uri.parse("android.resource://${context.packageName}/$soundResId")
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel("reminders", "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(soundUri, attributes)
            manager.createNotificationChannel(channel)
        }

        val taskId = intent.getLongExtra("taskId", 0)
        val title = intent.getStringExtra("title") ?: "Task reminder"

        val notification = NotificationCompat.Builder(context, "reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("RING Task Reminder")
            .setContentText(title)
            .setSound(soundUri)
            .setAutoCancel(true)
            .build()

        manager.notify(taskId.toInt(), notification)
    }
}
