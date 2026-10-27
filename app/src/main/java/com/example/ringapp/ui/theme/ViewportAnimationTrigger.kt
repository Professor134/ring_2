// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\theme\ViewportAnimationTrigger.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Handles the viewport animation trigger logic for this component without modifying runtime behavior.
// Represents the viewport animation trigger structure used by the application.
data class ViewportAnimationTrigger(
    val hasEnteredViewport: Boolean,
    val modifier: Modifier
)
@Composable

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

@Composable
// Handles the remember viewport animation trigger logic for this component without modifying runtime behavior.
// Manages the remember viewport animation trigger flow without changing runtime behavior.
fun rememberViewportAnimationTrigger(): ViewportAnimationTrigger {
    var hasEnteredViewport by remember { mutableStateOf(false) }
    val view = LocalView.current
    val modifier = Modifier.onGloballyPositioned { coordinates ->
        val bounds = coordinates.boundsInWindow()
        if (bounds.bottom > 0f && bounds.top < view.height.toFloat()) {
            hasEnteredViewport = true
        }
    }
    return ViewportAnimationTrigger(hasEnteredViewport, modifier)
}
