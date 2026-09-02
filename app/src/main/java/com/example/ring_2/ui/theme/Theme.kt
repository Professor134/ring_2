package com.example.ring_2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.example.ring_2.R

@Composable
fun RingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colorResource(R.color.primary),
            secondary = colorResource(R.color.secondary),
            tertiary = colorResource(R.color.tertiary),
            background = colorResource(R.color.background_dark),
            surface = colorResource(R.color.surface_dark),
            onPrimary = colorResource(R.color.on_primary_dark),
            onSecondary = colorResource(R.color.on_primary_dark),
            onTertiary = colorResource(R.color.on_primary_dark),
            onBackground = colorResource(R.color.on_background_dark),
            onSurface = colorResource(R.color.on_surface_dark),
            surfaceVariant = colorResource(R.color.surface_variant_dark),
            onSurfaceVariant = colorResource(R.color.light_gray),
            error = colorResource(R.color.error)
        )
    } else {
        lightColorScheme(
            primary = colorResource(R.color.primary),
            secondary = colorResource(R.color.secondary),
            tertiary = colorResource(R.color.tertiary),
            background = colorResource(R.color.background_light),
            surface = colorResource(R.color.surface_light),
            onPrimary = colorResource(R.color.on_primary_light),
            onSecondary = colorResource(R.color.on_primary_light),
            onTertiary = colorResource(R.color.on_primary_light),
            onBackground = colorResource(R.color.on_background_light),
            onSurface = colorResource(R.color.on_surface_light),
            surfaceVariant = colorResource(R.color.surface_variant_light),
            onSurfaceVariant = colorResource(R.color.dark_gray),
            error = colorResource(R.color.error)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
