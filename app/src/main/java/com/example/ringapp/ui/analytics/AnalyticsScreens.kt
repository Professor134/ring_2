package com.example.ringapp.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.engine.ScheduleEngine
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
private fun dayStart(date: LocalDate) = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

@Composable
fun MainAnalyticsScreen(onPersonal: () -> Unit = {}, viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); var range by remember { mutableStateOf("Days") }; var category by remember { mutableStateOf<Long?>(null) }
    val days = when (range) { "Weeks" -> 28; "Months" -> 90; "Year" -> 365; else -> 14 }
    val dates = (days - 1 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
    val points = dates.mapNotNull { aggregateDaily(state.habits, state.progress, it) }
    val categoryNames = listOf(null to "All Categories") + state.categories.map { it.id to it.name }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 20.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("ELITE  ${state.points}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Today's Score", "${points.lastOrNull()?.score ?: 0}%", Modifier.weight(1f)); Metric("Best Streak", "${state.bestStreak}", Modifier.weight(1f)); Metric("Total Done", state.habits.sumOf { it.totalCompletions }.toString(), Modifier.weight(1f)) } }
        item { TimeFilters(range) { range = it } }
        item { Text("Overall Growth", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { AnalyticsLineChart(points.map { LinePoint(it.date.format(dateFormatter), it.score) }, Modifier.fillMaxWidth().height(220.dp)) }
        item { Text("Category Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(categoryNames.size) { index -> val item = categoryNames[index]; FilterChip(category == item.first, { category = item.first }, label = { Text(item.second) }) } } }
        item { CategoryChart(state, category, dates, Modifier.fillMaxWidth().height(240.dp)) }
        item { TaskSummary(state.tasks) }
    }
}

@Composable
fun PersonalAnalyticsScreen(onBack: () -> Unit = {}, onEdit: (Long) -> Unit = {}, viewModel: PersonalAnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); var range by remember { mutableStateOf("Days") }; var selected by remember { mutableStateOf<LinePoint?>(null) }; var confirmDelete by remember { mutableStateOf(false) }; val habit = state.habit
    if (state.loading || habit == null) { Text(state.error ?: "Loading habit analytics...", Modifier.padding(24.dp)); return }
    val days = when (range) { "Weeks" -> 28; "Months" -> 90; "Year" -> 365; else -> 14 }; val dates = (days - 1 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }; val points = dates.map { date -> val record = state.progress.firstOrNull { it.date == dayStart(date) }; LinePoint(date.format(dateFormatter), record?.percentage ?: 0, record?.actual ?: 0, habit.target, record?.completed == true) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text(habit.name, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Elite Points", color = MaterialTheme.colorScheme.primary); IconButton(onClick = { onEdit(habit.id) }) { Icon(Icons.Default.Edit, "Edit") } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Current Streak", habit.currentStreak.toString(), Modifier.weight(1f)); Metric("Best Streak", habit.bestStreak.toString(), Modifier.weight(1f)); Metric("Total Done", habit.totalCompletions.toString(), Modifier.weight(1f)); Metric("Success Rate", "${if (state.progress.isEmpty()) 0 else state.progress.count { it.completed } * 100 / state.progress.size}%", Modifier.weight(1f)) } }
        item { Targets(habit) }
        item { Text(habit.description ?: "No note added.", color = if (habit.description.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface) }
        item { TimeFilters(range) { range = it } }
        item { AnalyticsLineChart(points, Modifier.fillMaxWidth().height(220.dp), onPointTap = { selected = it }) }
        if (selected != null) item { val point = selected!!; Card { Column(Modifier.padding(14.dp)) { Text(point.label, fontWeight = FontWeight.Bold); Text("${point.actual} / ${point.target} ${habit.unit.orEmpty()}"); Text("${point.score}%  ${if (point.completed) "Complete" else if (point.actual > 0) "Partial" else "Missed"}") } } }
        item { StreakSection(habit) }
        item { HabitCalendar(habit, state.progress) }
        item { Text("Progress Notes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        state.progress.sortedByDescending { it.date }.forEach { record -> item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(java.text.SimpleDateFormat("d MMM", Locale.getDefault()).format(record.date)); Text(record.note ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("${record.actual} / ${record.target} ${habit.unit.orEmpty()}") } } }
        item { PointSummary(state.transactions) }
        item { Button(onClick = { confirmDelete = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Delete Habit", color = MaterialTheme.colorScheme.error) } }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete habit?") }, text = { Text("The habit will be hidden, but historical analytics remain available.") }, confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}

@Composable private fun Targets(habit: HabitEntity) { val unit = habit.unit.orEmpty(); Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Targets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); listOf("Daily Target" to habit.target, "Weekly Target" to habit.target * 7, "Monthly Target" to habit.target * 30, "Yearly Target" to habit.target * 365).forEach { (label, value) -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(if (habit.type == HabitType.MEASURABLE) "$value $unit" else "$value scheduled occurrences", fontWeight = FontWeight.Bold) } } } }
@Composable private fun StreakSection(habit: HabitEntity) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Streak", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Current Streak: ${habit.currentStreak} days"); Text("Best Streak: ${habit.bestStreak} days"); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { listOf(10, 20).forEach { milestone -> Text("$milestone days: ${if (habit.bestStreak >= milestone) "Achieved" else "Not yet"}", color = if (habit.bestStreak >= milestone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) } } } }
@Composable private fun HabitCalendar(habit: HabitEntity, progress: List<HabitProgressEntity>) { val month = remember { LocalDate.now().withDayOfMonth(1) }; Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); (0 until month.lengthOfMonth()).chunked(7).forEach { week -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { week.forEach { offset -> val date = month.plusDays(offset.toLong()); val record = progress.firstOrNull { it.date == dayStart(date) }; val active = ScheduleEngine.isActiveOnDate(habit, date); val color = when { date.isAfter(LocalDate.now()) -> MaterialTheme.colorScheme.surfaceVariant; !active -> Color.Transparent; record?.completed == true -> MaterialTheme.colorScheme.primary; record != null -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.errorContainer }; Box(Modifier.size(34.dp).background(color, CircleShape), contentAlignment = Alignment.Center) { Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall) } } } } } }
@Composable private fun PointSummary(transactions: List<PointTransactionEntity>) { val earned = transactions.filter { it.amount > 0 }.sumOf { it.amount }; val lost = transactions.filter { it.amount < 0 }.sumOf { it.amount }; Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Point Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Elite Points Earned: +$earned"); Text("Elite Points Lost: $lost"); Text("Net: ${earned + lost}") } }
@Composable private fun TimeFilters(current: String, onChange: (String) -> Unit) { SingleChoiceSegmentedButtonRow { listOf("Days", "Weeks", "Months", "Year").forEachIndexed { index, value -> SegmentedButton(current == value, { onChange(value) }, shape = SegmentedButtonDefaults.itemShape(index, 4)) { Text(value) } } } }
@Composable private fun Metric(label: String, value: String, modifier: Modifier) { Card(modifier) { Column(Modifier.padding(12.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) } } }

data class LinePoint(val label: String, val score: Int, val actual: Int = 0, val target: Int = 0, val completed: Boolean = false)
@Composable private fun AnalyticsLineChart(points: List<LinePoint>, modifier: Modifier, onPointTap: (LinePoint) -> Unit = {}) {
    if (points.isEmpty() || points.all { it.score == 0 }) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("No stored data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier.pointerInput(points) {
        detectTapGestures { position ->
            val index = ((position.x / size.width) * points.size).toInt().coerceIn(0, points.lastIndex)
            onPointTap(points[index])
        }
    }) {
        val left = 20f
        val bottom = size.height - 24f
        val top = 12f
        val width = size.width - left - 8f
        repeat(5) { i ->
            val y = top + (bottom - top) * i / 4f
            drawLine(Color.LightGray.copy(alpha = .5f), Offset(left, y), Offset(size.width, y), 1f)
        }
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = left + width * index / points.lastIndex.coerceAtLeast(1)
            val y = bottom - (bottom - top) * point.score / 100f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(primaryColor, 5f, Offset(x, y))
        }
        drawPath(path, primaryColor, style = androidx.compose.ui.graphics.drawscope.Stroke(4f))
    }
}
@Composable private fun CategoryChart(state: AnalyticsUiState, selected: Long?, dates: List<LocalDate>, modifier: Modifier) { val categories = if (selected == null) state.categories else state.categories.filter { it.id == selected }; if (categories.isEmpty()) { Box(modifier, contentAlignment = Alignment.Center) { Text("No category data yet") }; return }; Column(modifier) { categories.forEachIndexed { index, category -> val color = listOf(Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFFC62828), Color(0xFF6A1B9A), Color(0xFFEF6C00))[index % 5]; Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).background(color, CircleShape)); Spacer(Modifier.width(8.dp)); Text(category.name) }; AnalyticsLineChart(dates.mapNotNull { date -> aggregateDaily(state.habits.filter { it.categoryId == category.id }, state.progress, date)?.let { LinePoint(it.date.format(dateFormatter), it.score) } }, Modifier.fillMaxWidth().height(90.dp)) } } }
@Composable private fun TaskSummary(tasks: List<TaskEntity>) { val completed = tasks.count { it.completed }; Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Task Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Total Tasks: ${tasks.size}"); Text("Completed: $completed"); Text("Pending: ${tasks.size - completed}"); Text("Completion Rate: ${if (tasks.isEmpty()) 0 else completed * 100 / tasks.size}%") } }
