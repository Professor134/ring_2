package com.example.ringapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RingDarkColors = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF00391B),
    primaryContainer = Color(0xFF006B32),
    onPrimaryContainer = Color(0xFF8AFFB5),
    secondary = Color(0xFF4FC3F7),
    tertiary = Color(0xFFFFD54F),
    error = Color(0xFFFF5252),
    background = Color(0xFF090D0B),
    onBackground = Color.White,
    surface = Color(0xFF111814),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF18231D),
    onSurfaceVariant = Color(0xFFB8C2BC)
)

private val RingLightColors = lightColorScheme(
    primary = Color(0xFF00A84F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8AFFB5),
    onPrimaryContainer = Color(0xFF00391B),
    secondary = Color(0xFF0288D1),
    tertiary = Color(0xFFF4B400),
    error = Color(0xFFD32F2F),
    background = Color(0xFFF5F8F6),
    onBackground = Color(0xFF101512),
    surface = Color.White,
    onSurface = Color(0xFF101512),
    surfaceVariant = Color(0xFFE8F1EB),
    onSurfaceVariant = Color(0xFF5D6861)
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
