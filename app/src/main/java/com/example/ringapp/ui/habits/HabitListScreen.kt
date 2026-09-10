package com.example.ringapp.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
    private val toggleHabit: ToggleHabitUseCase,
    private val recordProgress: RecordHabitProgressUseCase
) : ViewModel() {
    private val today = LocalDate.now()
    private val from = today.minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val to = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    val state: StateFlow<HabitListState> = combine(observeHabits(), observeCategories(), observeProgress(from, to), observeUserProgress()) { habits, categories, progress, userProgress -> HabitListState(habits, categories, progress, userProgress?.currentPoints ?: 0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListState())
    fun complete(habit: HabitEntity) = viewModelScope.launch { toggleHabit(habit) }
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
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Scaffold(
            topBar = {
                Column(Modifier.padding(horizontal = 16.dp).padding(top = 48.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                        Text("Habits", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = textColor)
                        PointsPill(state.points) 
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search habits...") }, singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) { 
                        item { FilterChip(categoryFilter == "All", { categoryFilter = "All" }, label = { Text("All") }, colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)) }
                        items(state.categories) { category -> FilterChip(categoryFilter == category.name, { categoryFilter = category.name }, label = { Text(category.name) }, colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)) }
                    }
                }
            },
            floatingActionButton = { FloatingActionButton(onClick = onAddHabit, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Add Habit") } },
            containerColor = bgColor
        ) { padding ->
            if (filtered.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("No habits yet", style = MaterialTheme.typography.titleLarge, color = textColor); Button(onClick = onAddHabit) { Text("+ Add Habit") } } } }
            else LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { 
                items(filtered, key = { it.id }) { habit -> 
                    HabitRow(habit, state.categories, state.progress, onHabitClick) { 
                        if (habit.type == HabitType.MEASURABLE) measurableHabit = habit else viewModel.complete(habit) 
                    } 
                } 
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
    measurableHabit?.let { habit ->
        val progress = state.progress.firstOrNull { it.habitId == habit.id && it.date == todayTimestamp() }
        ProgressDialog(habit, progress, { measurableHabit = null }) { value, note -> viewModel.record(habit, value, note); measurableHabit = null }
    }
}

@Composable private fun PointsPill(points: Int) { Text("ELITE  $points", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) }

@Composable private fun HabitRow(habit: HabitEntity, categories: List<CategoryEntity>, progress: List<HabitProgressEntity>, onHabitClick: (Long) -> Unit, onAction: () -> Unit) {
    val category = categories.firstOrNull { it.id == habit.categoryId }
    val baseColor = category?.color?.let(::Color) ?: Color(habit.color)
    val active = ScheduleEngine.isActiveOnDate(habit, LocalDate.now())
    val todayProgress = progress.firstOrNull { it.habitId == habit.id && it.date == todayTimestamp() }
    
    val isCompleted = todayProgress?.completed == true
    val isPartial = (todayProgress?.actual ?: 0) > 0 && !isCompleted
    
    val buttonColor = when {
        isCompleted -> baseColor
        isPartial -> baseColor.copy(alpha = 0.5f)
        else -> if (isSystemInDarkTheme()) Color(0xFF333333) else MaterialTheme.colorScheme.surfaceVariant
    }
    val iconTint = if (isCompleted || isPartial) Color.White else baseColor

    Card(onClick = { onHabitClick(habit.id) }) { 
        Column(Modifier.padding(16.dp)) { 
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { 
                Surface(color = baseColor.copy(alpha = 0.18f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) { 
                    Box(contentAlignment = Alignment.Center) { Icon(categoryIcon(category?.icon), null, tint = baseColor) } 
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { 
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(habit.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("🔥 ${habit.currentStreak}", color = Color.Red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("${category?.name ?: "Personal"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    val progressValue = if (habit.type == HabitType.MEASURABLE) {
                        ((todayProgress?.actual ?: 0).toFloat() / habit.target.coerceAtLeast(1)).coerceIn(0f, 1f)
                    } else {
                        if (isCompleted) 1f else 0f
                    }
                    LinearProgressIndicator(
                        progress = { progressValue }, 
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = baseColor,
                        trackColor = baseColor.copy(alpha = 0.2f)
                    )
                    if (!active) Text("Inactive today", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) 
                }
                IconButton(
                    onClick = onAction, 
                    enabled = active, 
                    modifier = Modifier.size(44.dp).background(if (active) buttonColor else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) { 
                    Icon(
                        if (habit.type == HabitType.MEASURABLE) Icons.Default.Edit else if (isCompleted) Icons.Default.Check else Icons.Default.Add, 
                        null, 
                        tint = if (active) iconTint else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                } 
            }
            HistoryRow(habit, progress)
        } 
    } 
}

@Composable private fun HistoryRow(habit: HabitEntity, progress: List<HabitProgressEntity>) { 
    val today = LocalDate.now()
    val categoryColor = Color(habit.color)
    val textColor = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface
    
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Last 5 active:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        
        var activeDaysFound = 0
        var dayOffset = 1
        
        while (activeDaysFound < 5 && dayOffset < 30) {
            val date = today.minusDays(dayOffset.toLong())
            if (ScheduleEngine.isActiveOnDate(habit, date)) {
                val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val record = progress.firstOrNull { it.habitId == habit.id && it.date == timestamp }
                
                val isRecordCompleted = record?.completed == true
                val isRecordPartial = record != null && record.actual > 0 && !isRecordCompleted
                
                Surface(
                    color = when {
                        isRecordCompleted -> categoryColor.copy(alpha = 0.2f)
                        isRecordPartial -> categoryColor.copy(alpha = 0.1f)
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
                                color = if (record != null) categoryColor else textColor.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = if (isRecordCompleted) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isRecordCompleted) categoryColor else if (record != null) Color.Red else textColor.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
                activeDaysFound++
            }
            dayOffset++
        }
        if (activeDaysFound == 0) Text("No history", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    }
}

@Composable private fun ProgressDialog(habit: HabitEntity, progress: HabitProgressEntity?, onDismiss: () -> Unit, onSave: (Int, String?) -> Unit) { var value by remember(progress) { mutableStateOf((progress?.actual ?: 0).toString()) }; var note by remember(progress) { mutableStateOf(progress?.note.orEmpty()) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Today's Progress") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Target: ${habit.target} ${habit.unit.orEmpty()}"); OutlinedTextField(value, { value = it.filter(Char::isDigit) }, label = { Text("Value") }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text("Add Note (optional)") }) } }, confirmButton = { TextButton(onClick = { onSave(value.toIntOrNull() ?: 0, note.ifBlank { null }) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

private fun scheduleLabel(habit: HabitEntity): String = when (habit.scheduleType) { ScheduleType.ODD_DAYS -> "Odd days"; ScheduleType.EVEN_DAYS -> "Even days"; ScheduleType.WEEKLY -> "Weekly"; ScheduleType.MONTHLY -> "Monthly"; ScheduleType.YEARLY -> "Yearly"; ScheduleType.CUSTOM -> "Every ${habit.scheduleDays?.firstOrNull() ?: 1} days"; else -> "Daily" }
private fun categoryIcon(iconName: String?): ImageVector = when (iconName?.lowercase()) { "gym" -> Icons.Default.FitnessCenter; "fitness" -> Icons.Default.FitnessCenter; "finance" -> Icons.Default.Payments; "payments" -> Icons.Default.Payments; "study" -> Icons.Default.School; "school" -> Icons.Default.School; "work" -> Icons.Default.Work; "sleep" -> Icons.Default.Bedtime; "bedtime" -> Icons.Default.Bedtime; "yoga" -> Icons.Default.SelfImprovement; "self" -> Icons.Default.SelfImprovement; "personal" -> Icons.Default.Person; "person" -> Icons.Default.Person; "book" -> Icons.AutoMirrored.Filled.MenuBook; else -> Icons.Default.Flag }
private fun todayTimestamp(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
