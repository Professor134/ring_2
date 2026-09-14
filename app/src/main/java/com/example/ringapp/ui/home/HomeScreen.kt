package com.example.ringapp.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.engine.ScheduleEngine
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
fun HomeScreen(
    onHabitClick: (Long) -> Unit = {},
    onAddHabit: () -> Unit = {},
    onAddTask: () -> Unit = {},
    onSeeAllHabits: () -> Unit = {},
    onSeeAllTasks: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var measurableHabit by remember { mutableStateOf<HabitEntity?>(null) }
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    if (state.isLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Loading your day...", color = textColor) }; return }
    val error = state.errorMessage
    if (error != null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error, color = MaterialTheme.colorScheme.error) }; return }

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 48.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(state.greeting, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = textColor)
                        Text(state.date, color = textColor.copy(alpha = 0.6f))
                    }
                    Surface(color = if (isDark) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), tonalElevation = 2.dp) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ELITE POINTS", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f))
                            Text(state.points.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = onAddHabit, Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("+ Add Habit") }; OutlinedButton(onClick = onAddTask, Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("+ Add Task", color = textColor) } } }
            item { SectionHeader("Today's Habits", onSeeAllHabits, textColor) }
            if (state.habits.isEmpty()) item { EmptyState("No habits yet", "Create your first habit and start your RING.", onAddHabit, textColor) }
            else items(state.habits, key = { "habit_${it.id}" }) { habit ->
                HomeHabitCard(
                    habit = habit, 
                    categories = state.categories, 
                    progress = state.todayProgress.firstOrNull { it.habitId == habit.id }, 
                    history = state.allProgress.filter { it.habitId == habit.id },
                    onHabitClick = { onHabitClick(habit.id) }, 
                    onComplete = { if (habit.type == HabitType.MEASURABLE) measurableHabit = habit else viewModel.onEvent(HomeEvent.CompleteHabit(habit)) }, 
                    textColor = textColor
                )
            }
            item { SectionHeader("Today's Tasks", onSeeAllTasks, textColor) }
            if (state.tasks.isEmpty()) item { EmptyState("No tasks for today", null, null, textColor) }
            else items(state.tasks, key = { "task_${it.id}" }) { task -> HomeTaskCard(task, textColor) { viewModel.onEvent(HomeEvent.ToggleTask(task)) } }
        }
    }
    measurableHabit?.let { habit -> ProgressDialog(habit, state.todayProgress.firstOrNull { it.habitId == habit.id }, { measurableHabit = null }) { value, note -> viewModel.onEvent(HomeEvent.RecordProgress(habit, todayTimestamp(), value, note)); measurableHabit = null } }
}

@Composable private fun SectionHeader(title: String, onSeeAll: () -> Unit, textColor: Color) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor); TextButton(onClick = onSeeAll) { Text("See all") } } }

@Composable private fun EmptyState(title: String, message: String?, action: (() -> Unit)?, textColor: Color) { Card(colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, fontWeight = FontWeight.SemiBold, color = textColor); if (message != null) Text(message, color = textColor.copy(alpha = 0.6f)); if (action != null) Button(onClick = action, shape = RoundedCornerShape(12.dp)) { Text("+ Add Habit") } } } }

@Composable private fun HomeHabitCard(
    habit: HabitEntity, 
    categories: List<CategoryEntity>, 
    progress: HabitProgressEntity?, 
    history: List<HabitProgressEntity>,
    onHabitClick: () -> Unit, 
    onComplete: () -> Unit, 
    textColor: Color
) { 
    val category = categories.firstOrNull { it.id == habit.categoryId }
    val baseColor = category?.color?.let(::Color) ?: Color(habit.color)
    val isCompleted = progress?.completed == true
    val isPartial = (progress?.actual ?: 0.0) > 0.0 && !isCompleted
    
    val buttonColor = when {
        isCompleted -> baseColor
        isPartial -> baseColor.copy(alpha = 0.5f)
        else -> if (isSystemInDarkTheme()) Color(0xFF333333) else MaterialTheme.colorScheme.surfaceVariant
    }
    val iconTint = if (isCompleted || isPartial) Color.White else baseColor

    Card(onClick = onHabitClick, modifier = Modifier.fillMaxWidth()) { 
        Column(Modifier.padding(16.dp)) { 
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { 
                Surface(color = baseColor.copy(alpha = 0.18f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) { 
                    Box(contentAlignment = Alignment.Center) { Icon(categoryIcon(category?.icon ?: "flag"), null, tint = baseColor) } 
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { 
                    Text(habit.name, fontWeight = FontWeight.SemiBold, color = textColor)
                    Text("${category?.name ?: "Personal"}  •  ${habit.currentStreak} 🔥", color = textColor.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                    val progressValue = if (habit.type == HabitType.MEASURABLE) {
                        ((progress?.actual ?: 0.0) / habit.target.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                    } else {
                        if (isCompleted) 1f else 0f
                    }
                    LinearProgressIndicator(
                        progress = { progressValue }, 
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = baseColor,
                        trackColor = baseColor.copy(alpha = 0.2f)
                    )
                }
                IconButton(
                    onClick = onComplete, 
                    modifier = Modifier.size(44.dp).background(buttonColor, CircleShape)
                ) { 
                    Icon(
                        if (habit.type == HabitType.MEASURABLE) Icons.Default.Edit else if (isCompleted) Icons.Default.Check else Icons.Default.Add, 
                        "Complete ${habit.name}", 
                        tint = iconTint
                    ) 
                } 
            }
            
            // Previous 5 Active Days History
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Last 5 active:", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                
                val today = LocalDate.now()
                var activeDaysFound = 0
                var dayOffset = 1
                
                while (activeDaysFound < 5 && dayOffset < 30) {
                    val date = today.minusDays(dayOffset.toLong())
                    if (ScheduleEngine.isActiveOnDate(habit, date)) {
                        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val record = history.firstOrNull { it.date == timestamp }
                        
                        val isRecordCompleted = record?.completed == true
                        val isRecordPartial = record != null && record.actual > 0 && !isRecordCompleted
                        
                        Surface(
                            color = when {
                                isRecordCompleted -> baseColor.copy(alpha = 0.2f)
                                isRecordPartial -> baseColor.copy(alpha = 0.1f)
                                record != null -> Color.Red.copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (habit.type == HabitType.MEASURABLE) {
                                    Text(
                                        text = record?.actual?.toString() ?: "0",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (record != null) baseColor else textColor.copy(alpha = 0.3f),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isRecordCompleted) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isRecordCompleted) baseColor else if (record != null) Color.Red else textColor.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                        activeDaysFound++
                    }
                    dayOffset++
                }
                if (activeDaysFound == 0) {
                    Text("No history", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.3f))
                }
            }
        } 
    } 
}

@Composable private fun IconContainer(iconName: String, color: Color) { Surface(color = color.copy(alpha = 0.18f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(categoryIcon(iconName), null, tint = color) } } }
private fun categoryIcon(iconName: String): ImageVector = when (iconName.lowercase()) { "fitness" -> Icons.Default.FitnessCenter; "payments" -> Icons.Default.Payments; "school" -> Icons.Default.School; "work" -> Icons.Default.Work; "bedtime" -> Icons.Default.Bedtime; "self" -> Icons.Default.SelfImprovement; "person" -> Icons.Default.Person; "book" -> Icons.AutoMirrored.Filled.MenuBook; else -> Icons.Default.Flag }

@Composable private fun HomeTaskCard(task: TaskEntity, textColor: Color, onToggle: () -> Unit) { 
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFD32F2F)
        TaskPriority.MEDIUM -> Color(0xFFFBC02D)
        TaskPriority.LOW -> Color(0xFF388E3C)
    }
    Card { 
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { 
            Box(Modifier.size(10.dp).background(priorityColor, CircleShape))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { 
                Text(task.title, fontWeight = FontWeight.SemiBold, color = textColor)
                Text(task.priority.name.lowercase().replaceFirstChar { it.uppercase() }, color = textColor.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall) 
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(38.dp).background(if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape)) { 
                Icon(Icons.Default.Check, null, tint = if (task.completed) Color.White else textColor.copy(alpha = 0.6f)) 
            }
        } 
    } 
}

@Composable private fun ProgressDialog(habit: HabitEntity, progress: HabitProgressEntity?, onDismiss: () -> Unit, onSave: (Double, String?) -> Unit) { var value by remember(progress) { mutableStateOf((progress?.actual ?: 0.0).toString()) }; var note by remember(progress) { mutableStateOf(progress?.note.orEmpty()) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Today's Progress") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Target: ${habit.target} ${habit.unit.orEmpty()}"); OutlinedTextField(value, { value = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Value") }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text("Add Note (optional)") }) } }, confirmButton = { TextButton(onClick = { onSave(value.toDoubleOrNull() ?: 0.0, note.ifBlank { null }) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

private fun todayTimestamp(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
