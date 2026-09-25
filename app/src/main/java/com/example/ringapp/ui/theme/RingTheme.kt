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
    surface = CardBackground,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E222D),
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
    surface = Color.White,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDEE5DD),
    onSurfaceVariant = Color(0xFF424942)
)

@Composable
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
