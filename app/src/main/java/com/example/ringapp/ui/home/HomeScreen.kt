package com.example.ringapp.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.data.local.entities.CategoryConstants
import com.example.ringapp.domain.engine.ScheduleEngine
import com.example.ringapp.ui.theme.CyberNeonBlue
import com.example.ringapp.ui.theme.CyberNeonGreen
import com.example.ringapp.ui.theme.SpaceBlack
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.text.SimpleDateFormat
import java.util.*

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
    val bgColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF0F2F5)
    val textColor = MaterialTheme.colorScheme.onBackground

    if (state.isLoading) { 
        Box(Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) { 
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }; return 
    }
    
    val error = state.errorMessage
    if (error != null) { 
        Box(Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) { 
            Text(error, color = MaterialTheme.colorScheme.error) 
        }; return 
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Glow
            if (isDark) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(CyberNeonBlue.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.2f),
                            radius = size.width
                        )
                    )
                }
            }

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp), 
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp), 
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                state.greeting, 
                                style = MaterialTheme.typography.headlineMedium, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = textColor
                            )
                            Text(
                                state.date.uppercase(), 
                                style = MaterialTheme.typography.labelMedium,
                                color = textColor.copy(alpha = 0.5f),
                                letterSpacing = 1.5.sp
                            )
                        }
                        
                        val infiniteTransition = rememberInfiniteTransition(label = "points")
                        val glowAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 0.8f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "glow"
                        )

                        Surface(
                            color = if (isDark) Color(0xFF1A1D23) else Color.White, 
                            shape = RoundedCornerShape(16.dp), 
                            tonalElevation = 8.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha))
                        ) {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ELITE POINTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.6f))
                                Text(
                                    state.points.toString(), 
                                    style = MaterialTheme.typography.titleLarge, 
                                    color = MaterialTheme.colorScheme.primary, 
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.graphicsLayer {
                                        shadowElevation = 8f
                                    }
                                )
                            }
                        }
                    }
                }
                item { 
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                        Button(
                            onClick = onAddHabit, 
                            Modifier.weight(1f).height(56.dp), 
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) { 
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Habit", fontWeight = FontWeight.Bold) 
                        } 
                        OutlinedButton(
                            onClick = onAddTask, 
                            Modifier.weight(1f).height(56.dp), 
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f))
                        ) { 
                            Icon(Icons.Default.Task, null, tint = textColor)
                            Spacer(Modifier.width(8.dp))
                            Text("Task", color = textColor, fontWeight = FontWeight.Bold) 
                        } 
                    } 
                }
                item { SectionHeader("DASHBOARD", onSeeAllHabits, textColor) }
                if (state.habits.isEmpty()) item { EmptyState("INITIATE YOUR JOURNEY", "The first ring is the hardest. Create a habit to begin.", onAddHabit, textColor) }
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
                item { SectionHeader("TASKS", onSeeAllTasks, textColor) }
                if (state.tasks.isEmpty()) item { EmptyState("ALL CLEAR", "No pending tasks for today.", null, textColor) }
                else items(state.tasks, key = { "task_${it.id}" }) { task -> HomeTaskCard(task, textColor) { viewModel.onEvent(HomeEvent.ToggleTask(task)) } }
            }
        }
    }
    measurableHabit?.let { habit -> ProgressDialog(habit, state.todayProgress.firstOrNull { it.habitId == habit.id }, { measurableHabit = null }) { value, note -> viewModel.onEvent(HomeEvent.RecordProgress(habit, todayTimestamp(), value, note)); measurableHabit = null } }
}

@Composable private fun SectionHeader(title: String, onSeeAll: () -> Unit, textColor: Color) { 
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.5f), letterSpacing = 2.sp); 
        TextButton(onClick = onSeeAll) { Text("SEE ALL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) } 
    } 
}

@Composable private fun EmptyState(title: String, message: String?, action: (() -> Unit)?, textColor: Color) { 
    val isDark = isSystemInDarkTheme()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1A1D23) else Color.White),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
    ) { 
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { 
            Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
            Text(title, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 1.sp); 
            if (message != null) Text(message, color = textColor.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center); 
            if (action != null) Button(onClick = action, shape = RoundedCornerShape(12.dp)) { Text("INITIALIZE") } 
        } 
    } 
}

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
    val isSteps = habit.isStepsHabit()
    val categoryName = if (isSteps) "System" else (category?.name ?: "Personal")
    
    // CRITICAL: Preserve habit colors from logic
    val baseColorInt = if (isSteps) HabitEntity.PLATINUM_COLOR else CategoryConstants.getColorForCategory(categoryName)
    val baseColor = Color(baseColorInt)
    
    val isCompleted = progress?.completed == true
    val isPartial = (progress?.actual ?: 0.0) > 0.0 && !isCompleted
    
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scale")

    val progressValue = remember(progress?.actual, habit.target) {
        if (habit.type == HabitType.MEASURABLE) {
            ((progress?.actual ?: 0.0) / habit.target.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
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
                shadowElevation = if (isSteps) 12f else 4f
                spotShadowColor = baseColor
            },
        onClick = onHabitClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161920) else Color.White),
        border = BorderStroke(if (isSteps) 2.dp else 1.dp, if (isSteps) baseColor else baseColor.copy(alpha = 0.15f))
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
                        Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                        if (!isSteps) {
                            Surface(color = Color(0xFFFF3131).copy(alpha = 0.1f), shape = CircleShape) {
                                Text("🔥 ${habit.currentStreak}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFFFF3131), fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Text(if (isSteps) "NEURAL SYNC ACTIVE" else categoryName.uppercase(), color = baseColor.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    
                    Spacer(Modifier.height(8.dp))
                    if (isSteps) {
                        val steps = progress?.actual?.toInt() ?: 0
                        val points = steps / 1000
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$steps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = textColor)
                            Text(" STEPS", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("+$points PTS", color = CyberNeonGreen, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    } else {
                        Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(baseColor.copy(alpha = 0.1f))) {
                            Box(Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(CircleShape).background(
                                Brush.horizontalGradient(listOf(baseColor.copy(alpha = 0.7f), baseColor))
                            ))
                        }
                    }
                }
                
                val buttonColor = when {
                    isCompleted -> baseColor
                    isPartial -> baseColor.copy(alpha = 0.6f)
                    else -> if (isDark) Color(0xFF252932) else Color(0xFFF0F2F5)
                }

                IconButton(
                    onClick = { if (!isSteps) onComplete() }, 
                    enabled = isSteps || ScheduleEngine.isActiveOnDate(habit, LocalDate.now()),
                    modifier = Modifier.size(48.dp).background(if (isSteps) baseColor.copy(alpha = 0.2f) else buttonColor, CircleShape)
                        .border(1.dp, if (isSteps) baseColor else Color.Transparent, CircleShape)
                ) { 
                    Icon(
                        imageVector = when {
                            isSteps -> Icons.AutoMirrored.Filled.TrendingUp
                            habit.type == HabitType.MEASURABLE -> Icons.Default.Edit
                            isCompleted -> Icons.Default.Check
                            else -> Icons.Default.Add
                        }, 
                        contentDescription = "Action", 
                        tint = if (isCompleted || isPartial) Color.White else baseColor
                    ) 
                } 
            }
            
            // History
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("LOG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.3f))
                
                val today = LocalDate.now()
                var activeDaysFound = 0
                var dayOffset = 1
                
                while (activeDaysFound < 6 && dayOffset < 30) {
                    val date = today.minusDays(dayOffset.toLong())
                    if (ScheduleEngine.isActiveOnDate(habit, date)) {
                        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val record = history.firstOrNull { it.date == timestamp }
                        
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
    } 
}

@Composable private fun IconContainer(iconName: String, color: Color) { Surface(color = color.copy(alpha = 0.18f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(categoryIcon(iconName), null, tint = color) } } }
private fun categoryIcon(iconName: String): ImageVector = when (iconName.lowercase()) { 
    "gym" -> Icons.Default.FitnessCenter
    "fitness" -> Icons.Default.FitnessCenter
    "finance" -> Icons.Default.Payments
    "payments" -> Icons.Default.Payments
    "study" -> Icons.Default.School
    "school" -> Icons.Default.School
    "work" -> Icons.Default.Work
    "sleep" -> Icons.Default.Bedtime
    "bedtime" -> Icons.Default.Bedtime
    "yoga" -> Icons.Default.SelfImprovement
    "self" -> Icons.Default.SelfImprovement
    "personal" -> Icons.Default.Person
    "person" -> Icons.Default.Person
    "book" -> Icons.AutoMirrored.Filled.MenuBook
    else -> Icons.Default.Flag 
}

@Composable private fun HomeTaskCard(task: TaskEntity, textColor: Color, onToggle: () -> Unit) { 
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFFF3131)
        TaskPriority.MEDIUM -> Color(0xFFFFD600)
        TaskPriority.LOW -> CyberNeonGreen
    }
    val isDark = isSystemInDarkTheme()
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161920) else Color.White),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
    ) { 
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { 
            Box(Modifier.size(12.dp).background(priorityColor, CircleShape).border(2.dp, priorityColor.copy(alpha = 0.3f), CircleShape))
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { 
                Text(task.title, fontWeight = FontWeight.Bold, color = textColor, style = MaterialTheme.typography.bodyLarge)
                Text(task.priority.name, color = priorityColor.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black) 
            }
            IconButton(
                onClick = onToggle, 
                modifier = Modifier.size(40.dp).background(if (task.completed) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.05f), CircleShape)
            ) { 
                Icon(Icons.Default.Check, null, tint = if (task.completed) Color.White else textColor.copy(alpha = 0.2f)) 
            }
        } 
    } 
}

@Composable private fun ProgressDialog(habit: HabitEntity, progress: HabitProgressEntity?, onDismiss: () -> Unit, onSave: (Double, String?) -> Unit) { var value by remember(progress) { mutableStateOf((progress?.actual ?: 0.0).toString()) }; var note by remember(progress) { mutableStateOf(progress?.note.orEmpty()) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Today's Progress") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Target: ${habit.target} ${habit.unit.orEmpty()}"); OutlinedTextField(value, { value = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Value") }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text("Add Note (optional)") }) } }, confirmButton = { TextButton(onClick = { onSave(value.toDoubleOrNull() ?: 0.0, note.ifBlank { null }) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

private fun todayTimestamp(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
