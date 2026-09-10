package com.example.ringapp.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 48.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = textColor); Text("ELITE  ${state.points}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Today's Score", "${points.lastOrNull()?.score ?: 0}%", Modifier.weight(1f), textColor); Metric("Best Streak", "${state.bestStreak}", Modifier.weight(1f), textColor); Metric("Total Done", state.habits.sumOf { it.totalCompletions }.toString(), Modifier.weight(1f), textColor) } }
            item { TimeFilters(range) { range = it } }
            item { Text("Overall Growth", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor) }
            item { AnalyticsLineChart(points.map { LinePoint(it.date.format(dateFormatter), it.score) }, Modifier.fillMaxWidth().height(220.dp)) }
            
            item { Text("Category Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor) }
            item { 
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { 
                    items(categoryNames.size) { index -> 
                        val item = categoryNames[index]
                        FilterChip(
                            selected = category == item.first, 
                            onClick = { category = item.first }, 
                            label = { Text(item.second) }, 
                            colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)
                        ) 
                    } 
                } 
            }
            item { 
                CategoryChart(state, category, dates, Modifier.fillMaxWidth(), textColor)
            }
            item { Spacer(Modifier.height(12.dp)) }
            item { TaskSummary(state.tasks, textColor) }
        }
    }
}

@Composable
fun PersonalAnalyticsScreen(onBack: () -> Unit = {}, onEdit: (Long) -> Unit = {}, viewModel: PersonalAnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); var range by remember { mutableStateOf("Days") }; var selected by remember { mutableStateOf<LinePoint?>(null) }; var confirmDelete by remember { mutableStateOf(false) }; val habit = state.habit
    if (state.loading || habit == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error ?: "Loading habit analytics...", Modifier.padding(24.dp)) }; return }
    val days = when (range) { "Weeks" -> 28; "Months" -> 90; "Year" -> 365; else -> 14 }; val dates = (days - 1 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }; val points = dates.map { date -> 
        val active = ScheduleEngine.isActiveOnDate(habit, date)
        val record = state.progress.firstOrNull { it.date == dayStart(date) }
        LinePoint(date.format(dateFormatter), if (active) (record?.percentage ?: 0) else 0, if (active) (record?.actual ?: 0) else 0, habit.target, record?.completed == true && active, date = date, isActive = active) 
    }
    
    val activePoints = points.filter { it.isActive }
    val successRate = if (activePoints.isEmpty()) 0 else activePoints.count { it.completed } * 100 / activePoints.size
    val habitColor = Color(habit.color)

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 48.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item { 
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { 
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                    Column(Modifier.weight(1f)) {
                        Text(habit.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                        Text("Personal Insights", style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = { onEdit(habit.id) }) { Icon(Icons.Default.Edit, "Edit", tint = textColor) } 
                } 
            }
            item { 
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                    Metric("Current Streak", "${habit.currentStreak} 🔥", Modifier.weight(1f), textColor)
                    Metric("Habit Points", state.habitPoints.toString(), Modifier.weight(1f), textColor)
                    Metric("Elite Points", state.points.toString(), Modifier.weight(1f), textColor)
                } 
            }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Total Done", habit.totalCompletions.toString(), Modifier.weight(1f), textColor); Metric("Success Rate", "$successRate%", Modifier.weight(1f), textColor) } }
            item { Targets(habit, state.progress, textColor) }
            item { TimeFilters(range) { range = it } }
            item { AnalyticsLineChart(activePoints, Modifier.fillMaxWidth().height(220.dp), color = habitColor, onPointTap = { selected = it }) }
            if (selected != null) item { 
                val point = selected!!
                val record = state.progress.firstOrNull { it.date == dayStart(point.date!!) }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { 
                    Column(Modifier.padding(14.dp)) { 
                        Text(point.label, fontWeight = FontWeight.Bold)
                        Text("${point.actual} / ${point.target} ${habit.unit.orEmpty()}")
                        Text("${point.score}%  ${if (point.completed) "Complete" else if (point.actual > 0) "Partial" else "Missed"}")
                        if (!record?.note.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Note: ${record!!.note}", style = MaterialTheme.typography.bodySmall)
                        }
                    } 
                } 
            }
            item { HabitCalendar(habit, state.progress, textColor) { date ->
                val record = state.progress.firstOrNull { it.date == dayStart(date) }
                selected = LinePoint(date.format(dateFormatter), record?.percentage ?: 0, record?.actual ?: 0, habit.target, record?.completed == true, date = date)
            } }
            item { PointSummary(state.transactions, textColor) }
            item { Button(onClick = { confirmDelete = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Delete, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Delete Habit", color = Color.White) } }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete habit?") }, text = { Text("The habit will be hidden, but historical analytics remain available.") }, confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}

@Composable private fun Targets(habit: HabitEntity, progress: List<HabitProgressEntity>, textColor: Color) {
    val unit = habit.unit.orEmpty()
    val today = LocalDate.now()
    
    val weekStart = today.minusDays(today.dayOfWeek.value % 7L)
    val monthStart = today.withDayOfMonth(1)
    val yearStart = today.withDayOfYear(1)

    fun calculateProgress(start: LocalDate, end: LocalDate): Pair<Int, Int> {
        var completed = 0
        var targetTotal = 0
        var current = start
        while (!current.isAfter(end)) {
            if (ScheduleEngine.isActiveOnDate(habit, current)) {
                targetTotal += habit.target
                val record = progress.firstOrNull { it.date == dayStart(current) }
                completed += record?.actual ?: 0
            }
            current = current.plusDays(1)
        }
        return completed to targetTotal
    }
    
    val todayRecord = progress.firstOrNull { it.date == dayStart(today) }
    val todayActive = ScheduleEngine.isActiveOnDate(habit, today)
    val weekProg = calculateProgress(weekStart, weekStart.plusDays(6))
    val monthProg = calculateProgress(monthStart, monthStart.withDayOfMonth(monthStart.lengthOfMonth()))
    val yearProg = calculateProgress(yearStart, yearStart.withDayOfYear(yearStart.lengthOfYear()))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Targets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
        
        TargetRow("Daily", if (todayActive) todayRecord?.actual ?: 0 else 0, if (todayActive) habit.target else 0, unit, habit.type, textColor)
        TargetRow("Weekly", weekProg.first, weekProg.second, unit, habit.type, textColor)
        TargetRow("Monthly", monthProg.first, monthProg.second, unit, habit.type, textColor)
        TargetRow("Yearly", yearProg.first, yearProg.second, unit, habit.type, textColor)
    } 
}

@Composable
private fun TargetRow(label: String, completed: Int, target: Int, unit: String, type: HabitType, textColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = textColor, fontWeight = FontWeight.Medium)
            Text(
                text = if (type == HabitType.MEASURABLE) "$completed / $target $unit" else "$completed / $target",
                fontWeight = FontWeight.Bold, 
                color = textColor
            )
        }
        LinearProgressIndicator(
            progress = { if (target > 0) (completed.toFloat() / target).coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable private fun HabitCalendar(habit: HabitEntity, progress: List<HabitProgressEntity>, textColor: Color, onDateClick: (LocalDate) -> Unit) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { 
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.Default.ArrowBack, null, Modifier.size(20.dp), tint = textColor) }
                Text(currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.Default.ArrowBack, null, Modifier.size(20.dp).rotate(180f), tint = textColor) }
            }
        }
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(day, Modifier.width(34.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
            }
        }

        val firstDayOfWeek = currentMonth.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()
        val weeks = (daysInMonth + firstDayOfWeek + 6) / 7

        repeat(weeks) { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(7) { dayOfWeek ->
                    val dayOfMonth = week * 7 + dayOfWeek - firstDayOfWeek + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val date = currentMonth.withDayOfMonth(dayOfMonth)
                        val record = progress.firstOrNull { it.date == dayStart(date) }
                        val active = ScheduleEngine.isActiveOnDate(habit, date)
                        val color = when {
                            date.isAfter(LocalDate.now()) -> Color.Gray.copy(alpha = 0.2f)
                            !active -> Color.Transparent
                            record?.completed == true -> MaterialTheme.colorScheme.primary
                            record != null -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                        Box(
                            Modifier.size(34.dp).background(color, CircleShape)
                                .clickable(enabled = active) { onDateClick(date) }, 
                            contentAlignment = Alignment.Center
                        ) { 
                            Text(dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall, color = if (color == Color.Transparent) textColor else Color.White)
                        }
                    } else {
                        Spacer(Modifier.size(34.dp))
                    }
                }
            }
        }
    } 
}
@Composable private fun PointSummary(transactions: List<PointTransactionEntity>, textColor: Color) { val earned = transactions.filter { it.amount > 0 }.sumOf { it.amount }; val lost = transactions.filter { it.amount < 0 }.sumOf { it.amount }; Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Point Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor); Text("Elite Points Earned: +$earned", color = textColor); Text("Elite Points Lost: $lost", color = textColor); Text("Net: ${earned + lost}", color = textColor) } }
@Composable private fun TimeFilters(current: String, onChange: (String) -> Unit) { SingleChoiceSegmentedButtonRow { listOf("Days", "Weeks", "Months", "Year").forEachIndexed { index, value -> SegmentedButton(current == value, { onChange(value) }, shape = SegmentedButtonDefaults.itemShape(index, 4)) { Text(value) } } } }
@Composable private fun Metric(label: String, value: String, modifier: Modifier, textColor: Color) { Card(modifier) { Column(Modifier.padding(12.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f)); Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor) } } }

data class LinePoint(val label: String, val score: Int, val actual: Int = 0, val target: Int = 0, val completed: Boolean = false, val date: LocalDate? = null, val isActive: Boolean = true)

@Composable private fun AnalyticsLineChart(points: List<LinePoint>, modifier: Modifier, color: Color = MaterialTheme.colorScheme.primary, onPointTap: (LinePoint) -> Unit = {}) {
    if (points.isEmpty() || points.all { it.score == 0 }) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("No stored data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val primaryColor = color
    Canvas(modifier.pointerInput(points) {
        detectTapGestures { position ->
            if (points.isNotEmpty()) {
                val index = ((position.x / size.width) * points.size).toInt().coerceIn(0, points.lastIndex)
                onPointTap(points[index])
            }
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
            val x = if (points.size > 1) left + width * index.toFloat() / points.lastIndex else left + width / 2
            val y = bottom - (bottom - top) * point.score / 100f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(primaryColor, 5f, Offset(x, y))
        }
        if (points.size > 1) {
            drawPath(path, primaryColor, style = androidx.compose.ui.graphics.drawscope.Stroke(4f))
        }
    }
}
@Composable private fun CategoryChart(state: AnalyticsUiState, selected: Long?, dates: List<LocalDate>, modifier: Modifier, textColor: Color) { 
    val categories = if (selected == null) state.categories else state.categories.filter { it.id == selected }
    if (categories.isEmpty()) { 
        Box(modifier, contentAlignment = Alignment.Center) { Text("No category data yet", color = textColor) }
        return 
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) { 
        categories.forEach { category -> 
            val color = Color(category.color)
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) { 
                    Box(Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(category.name, color = textColor, fontWeight = FontWeight.Bold) 
                }
                AnalyticsLineChart(
                    dates.mapNotNull { date -> 
                        aggregateDaily(state.habits.filter { it.categoryId == category.id }, state.progress, date)?.let { LinePoint(it.date.format(dateFormatter), it.score) } 
                    }, 
                    Modifier.fillMaxWidth().height(100.dp),
                    color = color
                ) 
            }
        } 
    } 
}
@Composable private fun TaskSummary(tasks: List<TaskEntity>, textColor: Color) { val completed = tasks.count { it.completed }; Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Task Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor); Text("Total Tasks: ${tasks.size}", color = textColor); Text("Completed: $completed", color = textColor); Text("Pending: ${tasks.size - completed}", color = textColor); Text("Completion Rate: ${if (tasks.isEmpty()) 0 else completed * 100 / tasks.size}%", color = textColor) } }
