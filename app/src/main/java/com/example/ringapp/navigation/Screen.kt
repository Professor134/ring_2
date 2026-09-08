package com.example.ringapp.navigation

object Screen {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val HabitDetail = "habit/{habitId}"
    const val HabitAnalytics = "habitAnalytics/{habitId}"
    const val AddHabit = "habit/add"
    const val EditHabit = "habit/edit/{habitId}"
    const val AddTask = "task/add"
    const val EditTask = "task/edit/{taskId}"
    const val EditProfile = "profile/edit"
    const val Appearance = "profile/appearance"
    const val PointHistory = "profile/points"
    const val Notifications = "profile/notifications"
    const val Backup = "profile/backup"
    const val PersonalAnalytics = "insights/personal"

    fun habitDetail(habitId: Long) = "habit/$habitId"
    fun habitAnalytics(habitId: Long) = "habitAnalytics/$habitId"
    fun editHabit(habitId: Long) = "habit/edit/$habitId"
    fun editTask(taskId: Long) = "task/edit/$taskId"
}