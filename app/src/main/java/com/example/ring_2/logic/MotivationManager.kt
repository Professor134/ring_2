package com.example.ring_2.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object MotivationManager {
    private val quotes = listOf(
        "Hungry for progress? Check off a habit now! 🚀",
        "Your streaks are getting cold! Better heat them up. 🔥",
        "Craving that 'Done' feeling? Tap to complete a task. ✅",
        "Fresh motivation delivered! Time to build your future. 🏗️",
        "Consistency is on the menu today. Don't skip! 🍱",
        "Elite Points are waiting for you. Come and get 'em! 💎",
        "Build the life you want, one step at a time. 👣",
        "Small habits, big impact. Keep the momentum! 📈",
        "You're doing great! Don't break the chain. 🔗",
        "Today's special: Productivity with a side of success. 🌟"
    )

    fun scheduleDailyMotivation(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9) // 9 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, MotivationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun getRandomQuote(): String = quotes.random()
}
