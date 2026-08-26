package com.example.ring_2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.Habit
import com.example.ring_2.data.model.Task
import com.example.ring_2.data.model.TaskPriority

@Composable
fun HabitCard(habit: Habit, onClick: () -> Unit, onComplete: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF1E1E1E),
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
                    Text(habit.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Health", color = Color(0xFF00E676), fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("🔥 ${habit.currentStreak} days", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("• daily", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onComplete: () -> Unit) {
    Surface(
        color = Color(0xFF1E1E1E),
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
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
            )
            Spacer(Modifier.width(16.dp))
            Text(task.title, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = onComplete) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}
