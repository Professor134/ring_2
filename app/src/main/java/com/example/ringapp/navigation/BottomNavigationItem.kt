package com.example.ringapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon

@Composable
fun NavigationMarker(destination: NavigationDestination) {
    Icon(destination.icon, contentDescription = destination.label)
}