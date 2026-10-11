// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\navigation\BottomNavigationItem.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon

@Composable
// Handles the navigation marker logic for this component without modifying runtime behavior.
// Manages the navigation marker flow without changing runtime behavior.
fun NavigationMarker(destination: NavigationDestination) {
    Icon(destination.icon, contentDescription = destination.label)
}