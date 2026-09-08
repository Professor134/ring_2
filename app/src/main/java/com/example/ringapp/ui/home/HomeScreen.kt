package com.example.ringapp.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import java.time.LocalDate
import java.time.ZoneId

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
    if (state.isLoading) { Text("Loading your day...", Modifier.padding(24.dp)); return }
    val error = state.errorMessage
    if (error != null) { Text(error, Modifier.padding(24.dp), color = MaterialTheme.colorScheme.error); return }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 20.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(state.greeting, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold); Text(state.date, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), tonalElevation = 2.dp) { Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("ELITE POINTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(state.points.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }
            }
        }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = onAddHabit, Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("+ Add Habit") }; OutlinedButton(onClick = onAddTask, Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("+ Add Task") } } }
        item { SectionHeader("Today's Habits", onSeeAllHabits) }
        if (state.habits.isEmpty()) item { EmptyState("No habits yet", "Create your first habit and start your RING.", onAddHabit) }
        else items(state.habits, key = { it.id }) { habit ->
            HomeHabitCard(habit, state.categories, state.todayProgress.firstOrNull { it.habitId == habit.id }, onHabitClick = { onHabitClick(habit.id) }, onComplete = { if (habit.type == HabitType.MEASURABLE) measurableHabit = habit else viewModel.onEvent(HomeEvent.CompleteHabit(habit)) })
        }
        item { SectionHeader("Today's Tasks", onSeeAllTasks) }
        if (state.tasks.isEmpty()) item { EmptyState("No tasks for today", null, null) }
        else items(state.tasks, key = { it.id }) { task -> HomeTaskCard(task) { viewModel.onEvent(HomeEvent.ToggleTask(task)) } }
        item { GrowthChart(state.chart) }
    }
    measurableHabit?.let { habit -> ProgressDialog(habit, state.todayProgress.firstOrNull { it.habitId == habit.id }, { measurableHabit = null }) { value, note -> viewModel.onEvent(HomeEvent.RecordProgress(habit, todayTimestamp(), value, note)); measurableHabit = null } }
}

@Composable private fun SectionHeader(title: String, onSeeAll: () -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); TextButton(onClick = onSeeAll) { Text("See all") } } }

@Composable private fun EmptyState(title: String, message: String?, action: (() -> Unit)?) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, fontWeight = FontWeight.SemiBold); if (message != null) Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant); if (action != null) Button(onClick = action, shape = RoundedCornerShape(12.dp)) { Text("+ Add Habit") } } } }

@Composable private fun HomeHabitCard(habit: HabitEntity, categories: List<CategoryEntity>, progress: HabitProgressEntity?, onHabitClick: () -> Unit, onComplete: () -> Unit) { val category = categories.firstOrNull { it.id == habit.categoryId }; val color = category?.color?.let(::Color) ?: Color(habit.color); Card(onClick = onHabitClick) { Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconContainer(category?.name, color); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(habit.name, fontWeight = FontWeight.SemiBold); Text("${category?.name ?: "Personal"}  •  ${habit.currentStreak} days", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); if (habit.type == HabitType.MEASURABLE) { LinearProgressIndicator(progress = { ((progress?.actual ?: 0).toFloat() / habit.target).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().padding(top = 8.dp)); Text("${progress?.actual ?: 0} / ${habit.target} ${habit.unit.orEmpty()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }; IconButton(onClick = onComplete, modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) { Icon(if (habit.type == HabitType.MEASURABLE) Icons.Default.Edit else Icons.Default.Check, "Complete ${habit.name}", tint = MaterialTheme.colorScheme.primary) } } } } }

@Composable private fun IconContainer(category: String?, color: Color) { Surface(color = color.copy(alpha = 0.18f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(categoryIcon(category), null, tint = color) } } }
private fun categoryIcon(category: String?): ImageVector = when (category?.lowercase()) { "gym" -> Icons.Default.FitnessCenter; "finance" -> Icons.Default.Payments; "study" -> Icons.Default.School; "work" -> Icons.Default.Work; "sleep" -> Icons.Default.Bedtime; "yoga" -> Icons.Default.SelfImprovement; "personal" -> Icons.Default.Person; else -> Icons.Default.Flag }

@Composable private fun HomeTaskCard(task: TaskEntity, onToggle: () -> Unit) { Card { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape)); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(task.title, fontWeight = FontWeight.SemiBold); Text(task.priority.name.lowercase().replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }; IconButton(onClick = onToggle, modifier = Modifier.size(38.dp).background(if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape)) { Icon(Icons.Default.Check, null, tint = if (task.completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable private fun ProgressDialog(habit: HabitEntity, progress: HabitProgressEntity?, onDismiss: () -> Unit, onSave: (Int, String?) -> Unit) { var value by remember(progress) { mutableStateOf((progress?.actual ?: 0).toString()) }; var note by remember(progress) { mutableStateOf(progress?.note.orEmpty()) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Today's Progress") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Target: ${habit.target} ${habit.unit.orEmpty()}"); OutlinedTextField(value, { value = it.filter(Char::isDigit) }, label = { Text("Value") }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text("Add Note (optional)") }) } }, confirmButton = { TextButton(onClick = { onSave(value.toIntOrNull() ?: 0, note.ifBlank { null }) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

@Composable private fun GrowthChart(points: List<ChartPoint>) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Overall Growth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (points.all { it.score == 0 }) {
                Text("No stored progress yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(Modifier.fillMaxWidth().height(170.dp)) {
                    val left = 24f
                    val top = 12f
                    val right = size.width - 8f
                    val bottom = size.height - 24f
                    repeat(5) { i ->
                        val y = top + (bottom - top) * i / 4f
                        drawLine(outlineColor, Offset(left, y), Offset(right, y), 1f)
                    }
                    val max = points.size.coerceAtLeast(2) - 1
                    val path = Path()
                    points.forEachIndexed { index, point ->
                        val x = left + (right - left) * index / max
                        val y = bottom - (bottom - top) * point.score / 100f
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(primaryColor, 4f, Offset(x, y))
                    }
                    drawPath(path, primaryColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    points.filterIndexed { index, _ -> index % 3 == 0 }.forEach {
                        Text(it.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun todayTimestamp(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
