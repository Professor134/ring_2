package com.example.ringapp.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(onAddTask: () -> Unit, onTaskClick: (Long) -> Unit, viewModel: TasksViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); var filter by remember { mutableStateOf("All") }; val today = startOfDay(System.currentTimeMillis())
    val filtered = state.tasks.filter { task -> when (filter) { "Pending" -> !task.completed; "Completed" -> task.completed; "High Priority" -> task.priority == TaskPriority.HIGH; "Overdue" -> !task.completed && (task.dueDate ?: Long.MAX_VALUE) < today; "Today" -> task.dueDate?.let { startOfDay(it) == today } == true; "Upcoming" -> (task.dueDate ?: 0) > today; else -> true } }
    
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Scaffold(
            topBar = {
                Column(Modifier.padding(horizontal = 16.dp).padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                        Text("Tasks", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = textColor)
                        Text("ELITE  ${state.points}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                        Text("Pending ${state.tasks.count { !it.completed }}", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f))
                        Text("Completed ${state.tasks.count { it.completed }}", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f)) 
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                        items(listOf("All", "Pending", "Completed", "High Priority", "Overdue", "Today", "Upcoming")) { option -> 
                            FilterChip(filter == option, { filter = option }, label = { Text(option) }, colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary))
                        } 
                    }
                    Spacer(Modifier.height(8.dp))
                }
            },
            floatingActionButton = { FloatingActionButton(onClick = onAddTask, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, null) } },
            containerColor = bgColor
        ) { padding ->
            if (filtered.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("No tasks found", color = textColor); Button(onClick = onAddTask) { Text("+ Add Task") } } }
            else LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filtered, key = { it.id }) { task -> TaskCard(task, textColor, { onTaskClick(task.id) }) { viewModel.toggle(task) } }
            }
        }
    }
}

@Composable private fun TaskCard(task: TaskEntity, textColor: Color, onClick: () -> Unit, onToggle: () -> Unit) { 
    Card(onClick = onClick) { 
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { 
            Box(Modifier.size(10.dp).background(priorityColor(task.priority), CircleShape))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { 
                Text(task.title, style = MaterialTheme.typography.titleMedium, color = textColor)
                Text("${task.priority.name}  •  ${task.dueDate?.let { SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(it)) } ?: "No due date"}", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.6f)) 
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(38.dp).background(if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                Icon(Icons.Default.Check, null, tint = if (task.completed) Color.White else textColor.copy(alpha = 0.6f))
            }
        } 
    } 
}
private fun priorityColor(priority: TaskPriority) = when (priority) { TaskPriority.HIGH -> Color(0xFFD32F2F); TaskPriority.MEDIUM -> Color(0xFFFBC02D); TaskPriority.LOW -> Color(0xFF388E3C) }
private fun startOfDay(time: Long): Long = Calendar.getInstance().apply { timeInMillis = time; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
