package com.example.ringapp.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ringapp.R
import com.example.ringapp.data.PreferencesDataStore
import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.domain.engine.ScheduleEngine
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

class HabitReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    lateinit var preferences: PreferencesDataStore
    lateinit var habitRepository: HabitRepository

    override suspend fun doWork(): Result {
        if (!preferences.habitRemindersEnabled.first()) return Result.success()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val today = LocalDate.now()
        val habits = habitRepository.observeActive().first()
            .filter { ScheduleEngine.isActiveOnDate(it, today) }
        val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val progressByHabit = habitRepository.observeProgressForDate(dayStart).first().associateBy { it.habitId }
        val remaining = habits.count { habit ->
            val target = habit.getTargetForDate(today)
            val actual = progressByHabit[habit.id]?.actual ?: 0.0
            actual < target
        }
        if (remaining == 0) return Result.success()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Habit reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("A quick habit check-in")
                .setContentText("$remaining scheduled ${if (remaining == 1) "habit is" else "habits are"} still in progress today.")
                .setAutoCancel(true)
                .build()
        )
        return Result.success()
    }

    private companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val NOTIFICATION_ID = 3001
    }
}
