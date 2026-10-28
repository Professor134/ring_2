package com.example.ringapp.ui.onboarding

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.ringapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

@Composable
fun RingSplashScreen(
    isLoading: Boolean,
    onFinished: (() -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val rotation = remember { Animatable(0f) }
    val shownAt = remember { SystemClock.elapsedRealtime() }
    val accent = MaterialTheme.colorScheme.primary

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                rotation.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(durationMillis = 1_800, easing = LinearEasing)
                )
                rotation.snapTo(0f)
            }
        }
    }

    LaunchedEffect(isLoading, onFinished) {
        if (!isLoading && onFinished != null) {
            val remainingMillis = (900L - (SystemClock.elapsedRealtime() - shownAt)).coerceAtLeast(0L)
            delay(remainingMillis)
            onFinished()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(156.dp)) {
                    Canvas(
                        modifier = Modifier
                            .size(148.dp)
                            .rotate(rotation.value)
                    ) {
                        drawArc(
                            color = accent.copy(alpha = 0.2f),
                            startAngle = 0f,
                            sweepAngle = 300f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = accent,
                            startAngle = 305f,
                            sweepAngle = 42f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "RING logo",
                        modifier = Modifier.size(104.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    letterSpacing = 5.sp
                )
                Text(
                    text = if (isLoading) "PREPARING YOUR SPACE" else "BUILD A RHYTHM THAT LASTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
