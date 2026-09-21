package com.example.ringapp.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ringapp.R
import kotlin.random.Random

class MotivationWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val quotes = listOf(
            "\"I never go back on my word. That’s my ninja way.\" – Naruto Uzumaki",
            "\"Hard work can overcome natural talent.\" – Rock Lee",
            "\"A little hard work every day can create incredible results.\" – Might Guy",
            "\"If you don’t like your destiny, don’t accept it. Change it.\" – Naruto Uzumaki",
            "\"I’m going to become the Pirate King!\" – Monkey D. Luffy",
            "\"Nothing happened.\" – Roronoa Zoro",
            "\"You need to accept the pain and become stronger.\" – Roronoa Zoro",
            "\"Sometimes you have to push yourself beyond your limits.\" – Izuku Midoriya",
            "\"A hero can always find a way to overcome the impossible.\" – All Might",
            "\"100 push-ups, 100 sit-ups, 100 squats, and a 10 km run.\" – Saitama",
            "\"I will continue training until I become stronger.\" – Genos",
            "\"If you don’t fight, you can’t win.\" – Eren Yeager",
            "\"My soldiers, rage! My soldiers, scream! My soldiers, fight!\" – Erwin Smith",
            "\"Choose. Don’t regret the choice you make.\" – Levi Ackerman",
            "\"No matter how many people you lose, you have no choice but to go on living.\" – Tanjiro Kamado",
            "\"Set your heart ablaze.\" – Kyojuro Rengoku",
            "\"Go forward. Don’t look back.\" – Kyojuro Rengoku",
            "\"Don’t give up. Keep moving forward.\" – Giyu Tomioka",
            "\"My magic is never giving up!\" – Asta",
            "\"Surpass your limits. Right here. Right now.\" – Yami Sukehiro",
            "\"Failure doesn’t mean you’ll never succeed.\" – Jiraiya",
            "\"Push yourself beyond your limits.\" – Vegeta",
            "\"Power comes from continuously challenging yourself.\" – Goku",
            "\"A lesson without pain is meaningless.\" – Edward Elric",
            "\"Nothing is gained without sacrifice.\" – Roy Mustang",
            "\"The future belongs to those who believe they can reach it.\" – Hinata Shoyo",
            "\"The next time you face me, I’ll be stronger.\" – Kageyama Tobio"
        )
        val quote = quotes[Random.nextInt(quotes.size)]

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel("motivation", "Daily Motivation", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val notificationId = 1001 // Fixed ID to prevent duplicate notifications
        val notification = NotificationCompat.Builder(applicationContext, "motivation")
            .setSmallIcon(R.drawable.ic_launcher_foreground2)
            .setContentTitle("Daily Motivation")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
        return Result.success()
    }
}
