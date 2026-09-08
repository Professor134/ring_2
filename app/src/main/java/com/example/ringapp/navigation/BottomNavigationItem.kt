package com.example.ringapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text

@Composable
fun NavigationMarker(destination: NavigationDestination) {
    Text(destination.marker)
}