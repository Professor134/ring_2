// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\theme\RingTheme.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RingDarkColors = darkColorScheme(
    primary = CyberNeonGreen,
    onPrimary = SpaceBlack,
    primaryContainer = Color(0xFF003D1A),
    onPrimaryContainer = CyberNeonGreen,
    secondary = CyberNeonPurple,
    onSecondary = Color.White,
    tertiary = CyberNeonBlue,
    error = RingError,
    background = MidnightNavy,
    onBackground = Color.White,
    surface = CardBackground.copy(alpha = 0.14f),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E222D).copy(alpha = 0.14f),
    surfaceContainerLowest = Color(0xFF11141A).copy(alpha = 0.12f),
    surfaceContainerLow = Color(0xFF171B22).copy(alpha = 0.14f),
    surfaceContainer = Color(0xFF1E222D).copy(alpha = 0.14f),
    surfaceContainerHigh = Color(0xFF262B36).copy(alpha = 0.16f),
    surfaceContainerHighest = Color(0xFF303642).copy(alpha = 0.18f),
    onSurfaceVariant = Color(0xFFB0B3C1)
)

private val RingLightColors = lightColorScheme(
    primary = Color(0xFF008C3A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0FFD9),
    onPrimaryContainer = Color(0xFF00210B),
    secondary = Color(0xFF6A0091),
    tertiary = Color(0xFF006875),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFF0F5F2),
    onBackground = Color(0xFF191C1E),
    surface = Color.White.copy(alpha = 0.14f),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDEE5DD).copy(alpha = 0.14f),
    surfaceContainerLowest = Color.White.copy(alpha = 0.12f),
    surfaceContainerLow = Color(0xFFF9FCF8).copy(alpha = 0.14f),
    surfaceContainer = Color(0xFFF0F5F2).copy(alpha = 0.14f),
    surfaceContainerHigh = Color(0xFFE7ECE7).copy(alpha = 0.16f),
    surfaceContainerHighest = Color(0xFFDEE5DD).copy(alpha = 0.18f),
    onSurfaceVariant = Color(0xFF424942)
)

@Composable
// Handles the ring theme logic for this component without modifying runtime behavior.
// Manages the ring theme flow without changing runtime behavior.
fun RingTheme(theme: String, content: @Composable () -> Unit) {
    val dark = when (theme) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) RingDarkColors else RingLightColors,
        typography = RingTypography,
        shapes = RingShapes,
        content = content
    )
}
