// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\theme\AppBackground.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import com.example.ringapp.R
@Composable

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

@Composable
// Handles the app background logic for this component without modifying runtime behavior.
// Manages the app background flow without changing runtime behavior.
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF0F2F5)

    Surface(modifier = modifier.fillMaxSize(), color = bgColor) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            content()
        }
    }
}
