package com.example.ring_2.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object Habits : Screen("habits", "Habits")
    data object Tasks : Screen("tasks", "Tasks")
    data object Profile : Screen("profile", "Profile")
    data object HabitDetail : Screen("habit_detail/{habitId}", "Habit Detail") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
}
