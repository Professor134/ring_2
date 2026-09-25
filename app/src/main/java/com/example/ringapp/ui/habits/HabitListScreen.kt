package com.example.ringapp.ui.habits

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.data.local.entities.CategoryConstants
import com.example.ringapp.domain.engine.ScheduleEngine
import com.example.ringapp.domain.usecase.*
import com.example.ringapp.ui.theme.CyberNeonBlue
import com.example.ringapp.ui.theme.CyberNeonGreen
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
    val state: StateFlow<HabitListState> = combine(observeHabits(), observeCategories(), observeProgress(from, to), observeUserProgress()) { habits, categories, progress, userProgress -> 
        val sortedHabits = habits.sortedWith(compareByDescending<HabitEntity> { it.isStepsHabit() }.thenBy { it.name })
        HabitListState(sortedHabits, categories, progress, userProgress?.currentPoints ?: 0) 
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListState())
    fun complete(habit: HabitEntity) = viewModelScope.launch { toggleHabit(habit) }
    fun record(habit: HabitEntity, value: Double, note: String?) = viewModelScope.launch { recordProgress(habit, todayTimestamp(), value, note) }
}

data class HabitListState(val habits: List<HabitEntity> = emptyList(), val categories: List<CategoryEntity> = emptyList(), val progress: List<HabitProgressEntity> = emptyList(), val points: Int = 0)

@Composable
fun HabitListScreen(onHabitClick: (Long) -> Unit, onAddHabit: () -> Unit, viewModel: HabitListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("All") }
    var scheduleFilter by remember { mutableStateOf("All") }
    var measurableHabit by remember { mutableStateOf<HabitEntity?>(null) }
    
    val filtered = state.habits.filter { habit -> 
        val matchesQuery = habit.name.contains(query, ignoreCase = true)
        val matchesCategory = categoryFilter == "All" || state.categories.find { it.id == habit.categoryId }?.name == categoryFilter
        val matchesSchedule = scheduleFilter == "All" || scheduleLabel(habit) == scheduleFilter
        matchesQuery && matchesCategory && matchesSchedule
    }
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF0F2F5)
    val textColor = MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor) {
        Scaffold(
            topBar = {
                Column(
                    Modifier
                        .background(bgColor)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp, bottom = 8.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                        Text("HABITS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 2.sp)
                        PointsPill(state.points) 
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = query, 
                        onValueChange = { query = it }, 
                        modifier = Modifier.fillMaxWidth(), 
                        placeholder = { Text("Find Habits...", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true, 
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) }, 
                        shape = RoundedCornerShape(16.dp), 
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor, 
                            unfocusedTextColor = textColor,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                            focusedContainerColor = if (isDark) Color(0xFF1A1D23) else Color.White,
                            unfocusedContainerColor = if (isDark) Color(0xFF161920) else Color.White
                        )
                    )
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) { 
                        item { 
                            FilterChip(
                                selected = categoryFilter == "All", 
                                onClick = { categoryFilter = "All" }, 
                                label = { Text("ALL") }, 
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(labelColor = textColor.copy(alpha = 0.6f), selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)
                            ) 
                        }
                        val filteredCategories = state.categories.filter { it.name.lowercase() != "habits" }
                        items(filteredCategories) { category -> 
                            FilterChip(
                                selected = categoryFilter == category.name, 
                                onClick = { categoryFilter = category.name }, 
                                label = { Text(category.name.uppercase()) }, 
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(labelColor = textColor.copy(alpha = 0.6f), selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)
                            ) 
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                        item { 
                            FilterChip(
                                selected = scheduleFilter == "All", 
                                onClick = { scheduleFilter = "All" }, 
                                label = { Text("ANY SCHEDULE") }, 
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(labelColor = textColor.copy(alpha = 0.6f), selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.secondary)
                            ) 
                        }
                        items(listOf("Daily", "Weekly", "Odd days", "Even days", "Custom")) { schedule ->
                            FilterChip(
                                selected = scheduleFilter == schedule, 
                                onClick = { scheduleFilter = schedule }, 
                                label = { Text(schedule.uppercase()) }, 
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(labelColor = textColor.copy(alpha = 0.6f), selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }
                }
            },
            floatingActionButton = { 
                FloatingActionButton(
                    onClick = onAddHabit, 
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) { Icon(Icons.Default.Add, "Add Habit", tint = Color.Black) } 
            },
            containerColor = bgColor
        ) { padding ->
            if (filtered.isEmpty()) { 
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { 
                        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = textColor.copy(alpha = 0.1f))
                        Text("NO DATA MATCHES", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.3f)); 
                        Button(onClick = onAddHabit, shape = RoundedCornerShape(12.dp)) { Text("INITIALIZE HABIT") } 
                    } 
                } 
            }
            else LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { 
                items(filtered, key = { it.id }) { habit -> 
                    HabitRow(habit, state.categories, state.progress, onHabitClick, textColor) { 
                        if (habit.type == HabitType.MEASURABLE) measurableHabit = habit else viewModel.complete(habit) 
                    } 
                } 
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
    measurableHabit?.let { habit ->
        val progress = state.progress.firstOrNull { it.habitId == habit.id && it.date == todayTimestamp() }
        ProgressDialog(habit, progress, { measurableHabit = null }) { value, note -> viewModel.record(habit, value, note); measurableHabit = null }
    }
}

@Composable private fun PointsPill(points: Int) { 
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), 
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Text(
            "ELITE  $points", 
            color = MaterialTheme.colorScheme.primary, 
            fontWeight = FontWeight.Black, 
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium
        ) 
    }
}

@Composable private fun HabitRow(habit: HabitEntity, categories: List<CategoryEntity>, progress: List<HabitProgressEntity>, onHabitClick: (Long) -> Unit, textColor: Color, onAction: () -> Unit) {
    val category = categories.firstOrNull { it.id == habit.categoryId }
    val isSteps = habit.isStepsHabit()
    val categoryName = if (isSteps) "System" else (category?.name ?: "Personal")
    
    // CRITICAL: Preserve colors
    val baseColorInt = if (isSteps) HabitEntity.PLATINUM_COLOR else CategoryConstants.getColorForCategory(categoryName)
    val baseColor = Color(baseColorInt)
    
    val active = ScheduleEngine.isActiveOnDate(habit, LocalDate.now())
    val todayProgress = progress.firstOrNull { it.habitId == habit.id && it.date == todayTimestamp() }
    
    val isCompleted = todayProgress?.completed == true
    val isPartial = (todayProgress?.actual ?: 0.0) > 0.0 && !isCompleted
    
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

    val progressValue = remember(todayProgress?.actual, habit.target) {
        if (habit.type == HabitType.MEASURABLE) {
            ((todayProgress?.actual ?: 0.0) / habit.target.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
        } else {
            if (isCompleted) 1f else 0f
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer {
                shadowElevation = if (isSteps) 10f else 2f
                spotShadowColor = baseColor
            },
        onClick = { onHabitClick(habit.id) },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161920) else Color.White),
        border = BorderStroke(if (isSteps) 2.dp else 1.dp, if (isSteps) baseColor else baseColor.copy(alpha = 0.1f))
    ) { 
        Column(Modifier.padding(18.dp)) { 
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { 
                Surface(
                    color = baseColor.copy(alpha = 0.12f), 
                    shape = RoundedCornerShape(14.dp), 
                    modifier = Modifier.size(52.dp),
                    border = BorderStroke(1.dp, baseColor.copy(alpha = 0.2f))
                ) { 
                    Box(contentAlignment = Alignment.Center) { 
                        Icon(
                            imageVector = if (isSteps) Icons.AutoMirrored.Filled.DirectionsRun else categoryIcon(CategoryConstants.getIconForCategory(categoryName)), 
                            contentDescription = null, 
                            tint = baseColor,
                            modifier = Modifier.size(28.dp)
                        ) 
                    } 
                }
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { 
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = textColor)
                        if (!isSteps) {
                            Surface(color = Color(0xFFFF3131).copy(alpha = 0.1f), shape = CircleShape) {
                                Text("🔥 ${habit.currentStreak}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFFFF3131), fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Text(if (isSteps) "NEURAL LINK" else categoryName.uppercase(), color = baseColor.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    
                    Spacer(Modifier.height(8.dp))
                    if (isSteps) {
                        val steps = todayProgress?.actual?.toInt() ?: 0
                        val points = steps / 1000
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$steps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = textColor)
                            Text(" STEPS", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("+$points PTS", color = CyberNeonGreen, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    } else {
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(baseColor.copy(alpha = 0.1f))) {
                            Box(Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(CircleShape).background(
                                Brush.horizontalGradient(listOf(baseColor.copy(alpha = 0.6f), baseColor))
                            ))
                        }
                    }
                    if (!active && !isSteps) Text("INACTIVE CYCLE", color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) 
                }
                
                val buttonColor = when {
                    isCompleted -> baseColor
                    isPartial -> baseColor.copy(alpha = 0.6f)
                    else -> if (isDark) Color(0xFF252932) else Color(0xFFF0F2F5)
                }

                IconButton(
                    onClick = { if (!isSteps) onAction() }, 
                    enabled = (active && !isSteps) || isSteps, 
                    modifier = Modifier.size(48.dp).background(if (isSteps) baseColor.copy(alpha = 0.1f) else if (active) buttonColor else textColor.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, if (isSteps) baseColor else Color.Transparent, CircleShape)
                ) { 
                    Icon(
                        imageVector = when {
                            isSteps -> Icons.AutoMirrored.Filled.TrendingUp
                            habit.type == HabitType.MEASURABLE -> Icons.Default.Edit
                            isCompleted -> Icons.Default.Check
                            else -> Icons.Default.Add
                        }, 
                        contentDescription = null, 
                        tint = if (isSteps || active && (isCompleted || isPartial)) Color.White else if (active) baseColor else textColor.copy(alpha = 0.2f)
                    ) 
                } 
            }
            HistoryRow(habit, progress, baseColor)
        } 
    } 
}

@Composable private fun HistoryRow(habit: HabitEntity, progress: List<HabitProgressEntity>, baseColor: Color) { 
    val today = LocalDate.now()
    val textColor = MaterialTheme.colorScheme.onSurface
    
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("LOG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.3f))
        
        var activeDaysFound = 0
        var dayOffset = 1
        
        while (activeDaysFound < 6 && dayOffset < 30) {
            val date = today.minusDays(dayOffset.toLong())
            if (ScheduleEngine.isActiveOnDate(habit, date)) {
                val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val record = progress.firstOrNull { it.habitId == habit.id && it.date == timestamp }
                
                val isRecordCompleted = record?.completed == true
                val isRecordPartial = record != null && record.actual > 0 && !isRecordCompleted
                
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isRecordCompleted -> baseColor.copy(alpha = 0.25f)
                                isRecordPartial -> baseColor.copy(alpha = 0.1f)
                                record != null -> Color(0xFFFF3131).copy(alpha = 0.15f)
                                else -> textColor.copy(alpha = 0.05f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val valueText = when {
                        record == null -> "-"
                        record.actual >= 1000 -> "${(record.actual / 1000).toInt()}k"
                        record.actual == record.actual.toInt().toDouble() -> record.actual.toInt().toString()
                        else -> String.format("%.1f", record.actual)
                    }
                    Text(
                        text = valueText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            isRecordCompleted -> baseColor
                            record != null && !isRecordCompleted -> Color(0xFFFF3131)
                            else -> textColor.copy(alpha = 0.3f)
                        }
                    )
                }
                activeDaysFound++
            }
            dayOffset++
        }
    }
}

@Composable private fun ProgressDialog(habit: HabitEntity, progress: HabitProgressEntity?, onDismiss: () -> Unit, onSave: (Double, String?) -> Unit) { var value by remember(progress) { mutableStateOf((progress?.actual ?: 0.0).toString()) }; var note by remember(progress) { mutableStateOf(progress?.note.orEmpty()) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Today's Progress") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Target: ${habit.target} ${habit.unit.orEmpty()}"); OutlinedTextField(value, { value = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Value") }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text("Add Note (optional)") }) } }, confirmButton = { TextButton(onClick = { onSave(value.toDoubleOrNull() ?: 0.0, note.ifBlank { null }) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

private fun scheduleLabel(habit: HabitEntity): String = when (habit.scheduleType) { 
    ScheduleType.ODD_DAYS -> "Odd days"
    ScheduleType.EVEN_DAYS -> "Even days"
    ScheduleType.WEEKLY -> "Weekly"
    ScheduleType.MONTHLY -> "Monthly"
    ScheduleType.YEARLY -> "Yearly"
    ScheduleType.CUSTOM -> "Custom"
    else -> "Daily" 
}
private fun categoryIcon(iconName: String?): ImageVector = when (iconName?.lowercase()) { "gym" -> Icons.Default.FitnessCenter; "fitness" -> Icons.Default.FitnessCenter; "finance" -> Icons.Default.Payments; "payments" -> Icons.Default.Payments; "study" -> Icons.Default.School; "school" -> Icons.Default.School; "work" -> Icons.Default.Work; "sleep" -> Icons.Default.Bedtime; "bedtime" -> Icons.Default.Bedtime; "yoga" -> Icons.Default.SelfImprovement; "self" -> Icons.Default.SelfImprovement; "personal" -> Icons.Default.Person; "person" -> Icons.Default.Person; "book" -> Icons.AutoMirrored.Filled.MenuBook; else -> Icons.Default.Flag }
private fun todayTimestamp(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
