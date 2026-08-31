package com.example.ring_2.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MotivationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val quote = MotivationManager.getRandomQuote()
        RingNotificationManager.showNotification(
            context,
            "Daily Motivation",
            quote
        )
    }
}
