package com.example.ringapp.ui.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.ui.theme.CyberNeonGreen
import com.example.ringapp.ui.theme.SpaceBlack
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(onAddTask: () -> Unit, onTaskClick: (Long) -> Unit, viewModel: TasksViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf("All") }
    val today = startOfDay(System.currentTimeMillis())
    val filtered = state.tasks.filter { task -> 
        when (filter) { 
            "Pending" -> !task.completed
            "Completed" -> task.completed
            "High" -> task.priority == TaskPriority.HIGH
            "Overdue" -> !task.completed && (task.dueDate ?: Long.MAX_VALUE) < today
            "Today" -> task.dueDate?.let { startOfDay(it) == today } == true
            else -> true 
        } 
    }
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Scaffold(
            topBar = {
                Column(Modifier.statusBarsPadding().padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TASKS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 2.sp)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text("ELITE ${state.points}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("All", "Pending", "Completed", "High", "Overdue", "Today")) { option ->
                            FilterChip(
                                selected = filter == option,
                                onClick = { filter = option },
                                label = { Text(option.uppercase()) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(labelColor = textColor.copy(alpha = 0.6f), selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTask,
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) { Icon(Icons.Default.Add, null, tint = SpaceBlack) }
            },
            containerColor = bgColor
        ) { padding ->
            if (filtered.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("NO TASKS IN QUEUE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.2f)); Button(onClick = onAddTask, shape = RoundedCornerShape(12.dp)) { Text("ADD TASK") } } }
            else {
                val pending = filtered.filter { !it.completed }
                val completed = filtered.filter { it.completed }
                LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (pending.isNotEmpty()) {
                        item { Text("PENDING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.5f), letterSpacing = 2.sp) }
                        items(pending, key = { it.id }) { task -> TaskCard(task, textColor, { onTaskClick(task.id) }) { viewModel.toggle(task) } }
                    }
                    if (completed.isNotEmpty()) {
                        item { Text("COMPLETED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.3f), modifier = Modifier.padding(top = 16.dp), letterSpacing = 2.sp) }
                        items(completed, key = { it.id }) { task -> TaskCard(task, textColor, { onTaskClick(task.id) }) { viewModel.toggle(task) } }
                    }
                }
            }
        }
    }
}

@Composable private fun TaskCard(task: TaskEntity, textColor: Color, onClick: () -> Unit, onToggle: () -> Unit) {
    val dueString = task.dueDate?.let { date ->
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        task.dueTime?.let { time ->
            val timeCal = Calendar.getInstance().apply { timeInMillis = time }
            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
        }
        SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(calendar.time)
    } ?: "Indefinite"

    val isDark = isSystemInDarkTheme()
    val priorityColor = priorityColor(task.priority)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161920) else Color.White),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(priorityColor))
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                Text("${task.priority.name} PROTOCOL  •  $dueString", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = onToggle, 
                modifier = Modifier.size(40.dp).background(if (task.completed) CyberNeonGreen else textColor.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.Default.Check, null, tint = if (task.completed) SpaceBlack else textColor.copy(alpha = 0.2f))
            }
        }
    }
}
private fun priorityColor(priority: TaskPriority) = when (priority) { TaskPriority.HIGH -> Color(0xFFFF3131); TaskPriority.MEDIUM -> Color(0xFFFFD600); TaskPriority.LOW -> CyberNeonGreen }
private fun startOfDay(time: Long): Long = Calendar.getInstance().apply { timeInMillis = time; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
