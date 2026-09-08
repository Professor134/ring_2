package com.example.ringapp.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.engine.ScheduleEngine
import com.example.ringapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class HabitListViewModel @Inject constructor(
    observeHabits: ObserveHabitsUseCase,
    observeCategories: ObserveCategoriesUseCase,
    observeProgress: ObserveProgressRangeUseCase,
    observeUserProgress: ObserveUserProgressUseCase,
    private val completeHabit: CompleteHabitUseCase,
    private val recordProgress: RecordHabitProgressUseCase
) : ViewModel() {
    private val today = LocalDate.now()
    private val from = today.minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val to = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    val state: StateFlow<HabitListState> = combine(observeHabits(), observeCategories(), observeProgress(from, to), observeUserProgress()) { habits, categories, progress, userProgress -> HabitListState(habits, categories, progress, userProgress?.currentPoints ?: 0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListState())
    fun complete(habit: HabitEntity) = viewModelScope.launch { completeHabit(habit) }
    fun record(habit: HabitEntity, value: Int, note: String?) = viewModelScope.launch { recordProgress(habit, todayTimestamp(), value, note) }
}

data class HabitListState(val habits: List<HabitEntity> = emptyList(), val categories: List<CategoryEntity> = emptyList(), val progress: List<HabitProgressEntity> = emptyList(), val points: Int = 0)

@Composable
fun HabitListScreen(onHabitClick: (Long) -> Unit, onAddHabit: () -> Unit, viewModel: HabitListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("All") }
    var measurableHabit by remember { mutableStateOf<HabitEntity?>(null) }
    val filtered = state.habits.filter { habit -> habit.name.contains(query, ignoreCase = true) && (categoryFilter == "All" || state.categories.firstOrNull { it.id == habit.categoryId }?.name == categoryFilter) }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = onAddHabit, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Add Habit") } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Habits", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); PointsPill(state.points) }
            OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search habits...") }, singleLine = true, textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface), leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 2.dp)) { item { FilterChip(categoryFilter == "All", { categoryFilter = "All" }, label = { Text("All") }) }; items(state.categories) { category -> FilterChip(categoryFilter == category.name, { categoryFilter = category.name }, label = { Text(category.name) }) } }
            if (filtered.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("No habits yet", style = MaterialTheme.typography.titleLarge); Button(onClick = onAddHabit) { Text("+ Add Habit") } } } }
            else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(filtered, key = { it.id }) { habit -> HabitRow(habit, state.categories, state.progress, onHabitClick) { if (habit.type == HabitType.MEASURABLE) measurableHabit = habit else viewModel.complete(habit) } } }
        }
    }
    measurableHabit?.let { habit ->
        val progress = state.progress.firstOrNull { it.habitId == habit.id && it.date == todayTimestamp() }
        ProgressDialog(habit, progress, { measurableHabit = null }) { value, note -> viewModel.record(habit, value, note); measurableHabit = null }
    }
}

@Composable private fun PointsPill(points: Int) { Text("ELITE  $points", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) }

@Composable private fun HabitRow(habit: HabitEntity, categories: List<CategoryEntity>, progress: List<HabitProgressEntity>, onHabitClick: (Long) -> Unit, onAction: () -> Unit) {
    val category = categories.firstOrNull { it.id == habit.categoryId }; val color = category?.color?.let(::Color) ?: Color(habit.color); val active = ScheduleEngine.isActiveOnDate(habit, LocalDate.now()); val todayProgress = progress.firstOrNull { it.habitId == habit.id && it.date == todayTimestamp() }
    Card(onClick = { onHabitClick(habit.id) }) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Surface(color = color.copy(alpha = 0.18f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(categoryIcon(category?.name), null, tint = color) } }; Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(habit.name, fontWeight = FontWeight.SemiBold); Text("${category?.name ?: "Personal"}  •  ${scheduleLabel(habit)}  •  ${habit.currentStreak} days", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); if (!active) Text("Inactive today", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }; IconButton(onClick = onAction, enabled = active, modifier = Modifier.size(38.dp).background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, CircleShape)) { Icon(if (habit.type == HabitType.MEASURABLE) Icons.Default.Edit else Icons.Default.Check, null, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) } }; if (habit.type == HabitType.MEASURABLE && active) { LinearProgressIndicator(progress = { ((todayProgress?.actual ?: 0).toFloat() / habit.target).coerceIn(0f, 1f) }, Modifier.fillMaxWidth()); Text("${todayProgress?.actual ?: 0} / ${habit.target} ${habit.unit.orEmpty()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; HistoryRow(habit, progress) } } }

@Composable private fun HistoryRow(habit: HabitEntity, progress: List<HabitProgressEntity>) { val today = LocalDate.now(); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { (1..5).forEach { offset -> val date = today.minusDays((6 - offset).toLong()); val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(); val record = progress.firstOrNull { it.habitId == habit.id && it.date == timestamp }; val scheduled = ScheduleEngine.isActiveOnDate(habit, date); val color = when { !scheduled -> MaterialTheme.colorScheme.surfaceVariant; record?.completed == true -> MaterialTheme.colorScheme.primary; record != null -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.errorContainer }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Box(Modifier.padding(top = 4.dp).size(24.dp).background(color, CircleShape), contentAlignment = Alignment.Center) { if (record?.completed == true) Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimary) } } } } }

@Composable private fun ProgressDialog(habit: HabitEntity, progress: HabitProgressEntity?, onDismiss: () -> Unit, onSave: (Int, String?) -> Unit) { var value by remember(progress) { mutableStateOf((progress?.actual ?: 0).toString()) }; var note by remember(progress) { mutableStateOf(progress?.note.orEmpty()) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Today's Progress") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Target: ${habit.target} ${habit.unit.orEmpty()}"); OutlinedTextField(value, { value = it.filter(Char::isDigit) }, label = { Text("Value") }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text("Add Note (optional)") }) } }, confirmButton = { TextButton(onClick = { onSave(value.toIntOrNull() ?: 0, note.ifBlank { null }) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

private fun scheduleLabel(habit: HabitEntity): String = when (habit.scheduleType) { ScheduleType.ODD_DAYS -> "Odd days"; ScheduleType.EVEN_DAYS -> "Even days"; ScheduleType.WEEKLY -> "Weekly"; ScheduleType.MONTHLY -> "Monthly"; ScheduleType.YEARLY -> "Yearly"; ScheduleType.CUSTOM -> "Every ${habit.scheduleDays?.firstOrNull() ?: 1} days"; else -> "Daily" }
private fun categoryIcon(category: String?): ImageVector = when (category?.lowercase()) { "gym" -> Icons.Default.FitnessCenter; "finance" -> Icons.Default.Payments; "study" -> Icons.Default.School; "work" -> Icons.Default.Work; "sleep" -> Icons.Default.Bedtime; "yoga" -> Icons.Default.SelfImprovement; "personal" -> Icons.Default.Person; else -> Icons.Default.Flag }
private fun todayTimestamp(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
