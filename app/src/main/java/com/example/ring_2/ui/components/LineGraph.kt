package com.example.ring_2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LineGraph(
    dataPoints: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(16.dp)) {
        if (dataPoints.size < 2) return@Canvas
        
        val width = size.width
        val height = size.height
        val maxVal = dataPoints.maxOrNull()?.coerceAtLeast(100f) ?: 100f
        
        val path = Path()
        val stepX = width / (dataPoints.size - 1)
        
        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal * height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw points
        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal * height)
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}
