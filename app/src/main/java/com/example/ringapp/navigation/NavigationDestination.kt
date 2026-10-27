// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\navigation\NavigationDestination.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector

// Handles the navigation destination logic for this component without modifying runtime behavior.
// Represents the navigation destination structure used by the application.
enum class NavigationDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Default.Home),
    HABITS("habits", "Habits", Icons.Default.EventNote),
    TASKS("tasks", "Tasks", Icons.Default.Task),
    INSIGHTS("insights", "Insights", Icons.Default.Assessment),
    PROFILE("profile", "Profile", Icons.Default.AccountCircle)
}