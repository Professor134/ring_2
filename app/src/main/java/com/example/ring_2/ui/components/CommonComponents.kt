package com.example.ring_2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitProgressEntity
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.data.model.TaskEntity
import com.example.ring_2.data.model.TaskPriority
import com.example.ring_2.logic.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitCard(
    habit: HabitEntity,
    categoryName: String = "Category",
    todayProgress: HabitProgressEntity? = null,
    history: List<HabitProgressEntity> = emptyList(),
    onClick: () -> Unit,
    onComplete: () -> Unit,
    showHistory: Boolean = false,
    isActiveToday: Boolean = true
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(habit.color).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = Color(habit.color), fontSize = 24.sp)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(categoryName, color = Color(habit.color), fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("🔥 ${habit.currentStreak} days", color = Color.Gray, fontSize = 12.sp)
                        if (!isActiveToday) {
                            Spacer(Modifier.width(8.dp))
                            Text("• Inactive today", color = Color.Red.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                }
                
                if (isActiveToday) {
                    if (habit.type == HabitType.YES_NO) {
                        val isDone = todayProgress?.completed == true
                        IconButton(
                            onClick = onComplete,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Color(0xFF00E676) else Color.Gray.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                if (isDone) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isDone) Color.Black else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Surface(
                            onClick = onComplete,
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            
            if (habit.type == HabitType.MEASURABLE) {
                Spacer(Modifier.height(12.dp))
                val current = todayProgress?.actualValue ?: 0.0
                val progress = (current / habit.target).toFloat().coerceIn(0f, 1f)
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(habit.color),
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
                if (isActiveToday) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${current.toInt()} / ${habit.target.toInt()} ${habit.unit}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            if (showHistory) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dates = getPreviousOccurrenceTimestamps(habit, 5)
                    for (date in dates) {
                        val progress = history.find { it.date == date }
                        val dayLabel = SimpleDateFormat("d MMM", Locale.getDefault()).format(java.util.Date(date))
                        
                        HistoryDot(
                            day = dayLabel,
                            isDone = progress?.completed == true,
                            value = if (habit.type == HabitType.MEASURABLE) {
                                if (progress != null) "${progress.actualValue.toInt()}" else "—"
                            } else ""
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll, contentPadding = PaddingValues(0.dp)) {
                Text("See all", color = Color(0xFF00E676), fontSize = 14.sp)
            }
        }
    }
}

private fun getPreviousOccurrenceTimestamps(habit: HabitEntity, count: Int): List<Long> {
    val dates = mutableListOf<Long>()
    val cal = Calendar.getInstance()
    var checked = 0
    var iterations = 0
    // Increased iteration limit to support yearly habits (5 occurrences = ~5 years)
    while (checked < count && iterations < 2000) {
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val timestamp = DateTimeUtils.getMidnightTimestamp(cal.timeInMillis)
        if (com.example.ring_2.logic.ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, timestamp)) {
            dates.add(timestamp)
            checked++
        }
        iterations++
    }
    return dates.reversed()
}

@Composable
fun HistoryDot(day: String, isDone: Boolean, value: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, fontSize = 10.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Surface(
            color = if (isDone) Color(0xFF00E676) else Color.Gray.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp, 30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (value.isNotEmpty() && value != "—") {
                    Text(value, fontSize = 10.sp, color = if (isDone) Color.Black else Color.Gray)
                } else {
                    Icon(
                        if (isDone) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isDone) Color.Black else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskEntity, onClick: () -> Unit, onComplete: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when(task.priority) {
                            TaskPriority.HIGH -> Color.Red
                            TaskPriority.MEDIUM -> Color.Yellow
                            TaskPriority.LOW -> Color.Cyan
                            TaskPriority.URGENT -> Color(0xFFFF5722)
                        },
                        shape = CircleShape
                    )
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    color = if (task.completed) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        task.priority.name,
                        color = when(task.priority) {
                            TaskPriority.URGENT -> Color(0xFFFF5722)
                            TaskPriority.HIGH -> Color.Red
                            TaskPriority.MEDIUM -> Color.Yellow
                            TaskPriority.LOW -> Color.Cyan
                        },
                        fontSize = 11.sp
                    )
                    if (task.dueDate != null) {
                        Text(" • ", color = Color.DarkGray)
                        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
                        Text(
                            sdf.format(Date(task.dueDate)),
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            IconButton(
                onClick = onComplete,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (task.completed) Color(0xFF00E676) else Color.Gray.copy(alpha = 0.2f))
            ) {
                Icon(
                    if (task.completed) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (task.completed) Color.Black else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    name: String,
    avatarColor: Color,
    size: androidx.compose.ui.unit.Dp = 100.dp,
    photoUri: String? = null
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        color = avatarColor.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(2.dp, avatarColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                name.take(1).uppercase().ifEmpty { "U" },
                fontSize = (size.value * 0.4).sp,
                fontWeight = FontWeight.Bold,
                color = avatarColor
            )
        }
    }
}
