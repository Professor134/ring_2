package com.example.ringapp.navigation

import androidx.compose.ui.graphics.Color

enum class NavigationDestination(
    val route: String,
    val label: String,
    val marker: String
) {
    HOME("home", "Home", "H"),
    HABITS("habits", "Habits", "B"),
    TASKS("tasks", "Tasks", "T"),
    INSIGHTS("insights", "Insights", "I"),
    PROFILE("profile", "Profile", "P")
}