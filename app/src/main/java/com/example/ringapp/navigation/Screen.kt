// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\navigation\Screen.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.navigation

// Handles the screen logic for this component without modifying runtime behavior.
// Represents the screen structure used by the application.
object Screen {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val HabitDetail = "habit/{habitId}"
    const val HabitAnalytics = "habitAnalytics/{habitId}"
    const val AddHabit = "habit/add"
    const val EditHabit = "habit/edit/{habitId}"
    const val AddTask = "task/add"
    const val EditTask = "task/edit/{taskId}"
    const val TaskDetail = "taskDetail/{taskId}"
    const val EditProfile = "profile/edit"
    const val Appearance = "profile/appearance"
    const val PointHistory = "profile/points"
    const val Notifications = "profile/notifications"
    const val Backup = "profile/backup"
    const val PersonalAnalytics = "insights/personal"

// Manages the habit detail flow without changing runtime behavior.
    fun habitDetail(habitId: Long) = "habit/$habitId"
// Manages the habit analytics flow without changing runtime behavior.
    fun habitAnalytics(habitId: Long) = "habitAnalytics/$habitId"
// Manages the edit habit flow without changing runtime behavior.
    fun editHabit(habitId: Long) = "habit/edit/$habitId"
// Manages the edit task flow without changing runtime behavior.
    fun editTask(taskId: Long) = "task/edit/$taskId"
// Manages the task detail flow without changing runtime behavior.
    fun taskDetail(taskId: Long) = "taskDetail/$taskId"
}
