package com.example.ringapp.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ringapp.R

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("reminders", "Reminders", NotificationManager.IMPORTANCE_DEFAULT))
        manager.notify(intent.getLongExtra("taskId", 0).toInt(), NotificationCompat.Builder(context, "reminders").setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("RING reminder").setContentText(intent.getStringExtra("title") ?: "Task reminder").setAutoCancel(true).build())
    }
}