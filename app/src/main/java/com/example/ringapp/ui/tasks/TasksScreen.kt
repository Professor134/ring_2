package com.example.ringapp.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(onAddTask: () -> Unit, onTaskClick: (Long) -> Unit, viewModel: TasksViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); var filter by remember { mutableStateOf("All") }; val today = startOfDay(System.currentTimeMillis())
    val filtered = state.tasks.filter { task -> when (filter) { "Pending" -> !task.completed; "Completed" -> task.completed; "High Priority" -> task.priority == TaskPriority.HIGH || task.priority == TaskPriority.URGENT; "Overdue" -> !task.completed && (task.dueDate ?: Long.MAX_VALUE) < today; "Today" -> task.dueDate?.let { startOfDay(it) == today } == true; "Upcoming" -> (task.dueDate ?: 0) > today; else -> true } }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = onAddTask) { Text("+") } }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 20.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Tasks", style = MaterialTheme.typography.headlineMedium); Text("ELITE  ${state.points}", color = MaterialTheme.colorScheme.primary) } }
            item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Text("Pending ${state.tasks.count { !it.completed }}"); Text("Completed ${state.tasks.count { it.completed }}"); Text("Total ${state.tasks.size}") } }
            item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(listOf("All", "Pending", "Completed", "High Priority", "Overdue", "Today", "Upcoming")) { option -> FilterChip(filter == option, { filter = option }, label = { Text(option) }) } } }
            if (filtered.isEmpty()) item { Card { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("No tasks yet"); Button(onClick = onAddTask) { Text("+ Add Task") } } } }
            else items(filtered, key = { it.id }) { task -> TaskCard(task, { onTaskClick(task.id) }) { viewModel.toggle(task) } }
        }
    }
}

@Composable private fun TaskCard(task: TaskEntity, onClick: () -> Unit, onToggle: () -> Unit) { Card(onClick = onClick) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).background(priorityColor(task.priority), CircleShape)); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(task.title, style = MaterialTheme.typography.titleMedium); Text("${task.priority.name}  •  ${task.dueDate?.let { SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(it)) } ?: "No due date"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Checkbox(task.completed, { onToggle() }) } } }
private fun priorityColor(priority: TaskPriority) = when (priority) { TaskPriority.URGENT -> Color(0xFFD32F2F); TaskPriority.HIGH -> Color(0xFFF57C00); TaskPriority.MEDIUM -> Color(0xFFFBC02D); TaskPriority.LOW -> Color(0xFF388E3C) }
private fun startOfDay(time: Long): Long = Calendar.getInstance().apply { timeInMillis = time; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
