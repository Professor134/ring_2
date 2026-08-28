package com.example.ring_2.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("task_title") ?: "Task"
        RingNotificationManager.showNotification(
            context,
            "RING",
            "Complete your task: $taskTitle"
        )
    }
}
