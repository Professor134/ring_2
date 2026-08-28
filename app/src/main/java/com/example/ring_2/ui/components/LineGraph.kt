package com.example.ring_2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LineGraph(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    labels: List<String> = emptyList(),
    color: Color
) {
    if (dataPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }
    
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.Gray)

    Box(modifier = modifier.padding(start = 32.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dataPoints) {
                    detectTapGestures { offset ->
                        if (dataPoints.size > 1) {
                            val stepX = size.width / (dataPoints.size - 1)
                            val index = (offset.x / stepX + 0.5f).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = index
                        } else {
                            selectedIndex = 0
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val maxVal = 100f
            
            // Draw Axis
            drawLine(Color.DarkGray, Offset(0f, 0f), Offset(0f, height), strokeWidth = 1.dp.toPx())
            drawLine(Color.DarkGray, Offset(0f, height), Offset(width, height), strokeWidth = 1.dp.toPx())

            // Draw Y-Axis labels
            listOf(0, 50, 100).forEach { valY ->
                val y = height - (valY.toFloat() / maxVal * height)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$valY%",
                    topLeft = Offset(-28.dp.toPx(), y - 6.dp.toPx()),
                    style = labelStyle
                )
            }

            if (dataPoints.size > 1) {
                val path = Path()
                val stepX = width / (dataPoints.size - 1)
                
                dataPoints.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height).coerceIn(0f, height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 3.dp.toPx())
                )
                
                // Draw points and labels
                dataPoints.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height).coerceIn(0f, height)
                    
                    drawCircle(
                        color = if (selectedIndex == index) Color.White else color,
                        radius = (if (selectedIndex == index) 6.dp else 4.dp).toPx(),
                        center = Offset(x, y)
                    )
                    
                    if (labels.size > index) {
                        // X-axis label
                        if (index % ((labels.size / 5).coerceAtLeast(1)) == 0) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = labels[index],
                                topLeft = Offset(x - 10.dp.toPx(), height + 8.dp.toPx()),
                                style = labelStyle
                            )
                        }
                    }
                }
            } else {
                val x = width / 2
                val y = height - (dataPoints[0] / maxVal * height).coerceIn(0f, height)
                drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
            }
        }
        
        // Tooltip
        selectedIndex?.let { index ->
            if (index < dataPoints.size) {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "${labels.getOrNull(index) ?: ""} : ${dataPoints[index].toInt()}%",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun MultiLineGraph(
    data: Map<String, List<Float>>,
    modifier: Modifier = Modifier,
    labels: List<String> = emptyList(),
    colors: Map<String, Color>
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No category data", color = Color.Gray)
        }
        return
    }
    
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.Gray)

    Box(modifier = modifier.padding(start = 32.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val firstList = data.values.firstOrNull() ?: return@detectTapGestures
                        if (firstList.size > 1) {
                            val stepX = size.width / (firstList.size - 1)
                            val index = (offset.x / stepX + 0.5f).toInt().coerceIn(0, firstList.size - 1)
                            selectedIndex = index
                        } else {
                            selectedIndex = 0
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val maxVal = 100f
            
            drawLine(Color.DarkGray, Offset(0f, 0f), Offset(0f, height), strokeWidth = 1.dp.toPx())
            drawLine(Color.DarkGray, Offset(0f, height), Offset(width, height), strokeWidth = 1.dp.toPx())

            // Y-Axis labels
            listOf(0, 50, 100).forEach { valY ->
                val y = height - (valY.toFloat() / maxVal * height)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$valY%",
                    topLeft = Offset(-28.dp.toPx(), y - 6.dp.toPx()),
                    style = labelStyle
                )
            }

            data.forEach { (label, points) ->
                if (points.size < 2) return@forEach
                
                val color = colors[label] ?: Color.White
                val path = Path()
                val stepX = width / (points.size - 1)
                
                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height).coerceIn(0f, height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.dp.toPx())
                )
                
                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxVal * height).coerceIn(0f, height)
                    drawCircle(
                        color = if (selectedIndex == index) Color.White else color,
                        radius = (if (selectedIndex == index) 4.dp else 3.dp).toPx(),
                        center = Offset(x, y)
                    )
                }
            }
            
            // X-Axis labels
            if (labels.isNotEmpty()) {
                val firstList = data.values.firstOrNull() ?: emptyList()
                val stepX = if (firstList.size > 1) width / (firstList.size - 1) else 0f
                labels.forEachIndexed { index, label ->
                    if (index % ((labels.size / 5).coerceAtLeast(1)) == 0) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = label,
                            topLeft = Offset(index * stepX - 10.dp.toPx(), height + 8.dp.toPx()),
                            style = labelStyle
                        )
                    }
                }
            }
        }
        
        // Multi-point Tooltip
        selectedIndex?.let { index ->
            Surface(
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(labels.getOrNull(index) ?: "", color = Color.Gray, fontSize = 10.sp)
                    data.forEach { (label, points) ->
                        if (index < points.size) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(colors[label] ?: Color.White, CircleShape))
                                Spacer(Modifier.width(4.dp))
                                Text("$label: ${points[index].toInt()}%", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
