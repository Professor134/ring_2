package com.example.ring_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.logic.ScheduleEngine
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.components.SectionHeader
import com.example.ring_2.ui.components.HabitCard
import com.example.ring_2.ui.components.NumericInputDialog
import com.example.ring_2.ui.components.TaskCard
import com.example.ring_2.ui.components.LineGraph
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAddHabit: () -> Unit,
    onAddTask: () -> Unit,
    onHabitClick: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    onSeeAllHabits: () -> Unit,
    onSeeAllTasks: () -> Unit
) {
    val habits by viewModel.allHabits.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val userProg by viewModel.userProgress.collectAsState()
    val todayProgress by viewModel.todayProgress.collectAsState()
    val allProgress by viewModel.allProgress.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var selectedFilter by remember { mutableStateOf("Days") }
    var showNumericDialogFor by remember { mutableStateOf<HabitEntity?>(null) }
    
    val today = getMidnightTimestamp(System.currentTimeMillis())
    val todayHabits = habits.filter { ScheduleEngine.isHabitActiveOnDate(it.schedule, it.startDate, today) }
    val pendingTasks = tasks.filter { !it.completed }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. HEADER
            item {
                HomeHeader(profile?.name?.ifEmpty { "User" } ?: "User", userProg?.currentPoints ?: 500)
            }
            
            // 2. QUICK ACTION BUTTONS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        text = "+ Add Habit",
                        onClick = onAddHabit,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        text = "+ Add Task",
                        onClick = onAddTask,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. TODAY'S HABITS
            if (todayHabits.isNotEmpty()) {
                item {
                    SectionHeader("Today's Habits", onSeeAll = onSeeAllHabits)
                }

                items(todayHabits) { habit ->
                    val habitProgress = todayProgress.find { it.habitId == habit.id }
                    val categoryName = categories.find { it.id == habit.categoryId }?.name ?: "Others"
                    HabitCard(
                        habit = habit,
                        categoryName = categoryName,
                        todayProgress = habitProgress,
                        onClick = { onHabitClick(habit.id) },
                        onComplete = { 
                            if (habit.type == HabitType.YES_NO) {
                                val newValue = if (habitProgress?.completed == true) 0.0 else habit.target
                                viewModel.recordProgress(habit, newValue)
                            } else {
                                showNumericDialogFor = habit
                            }
                        }
                    )
                }
            } else {
                item {
                    EmptyHabitState(onAddHabit)
                }
            }

            // 8. TODAY'S TASKS
            if (pendingTasks.isNotEmpty()) {
                item {
                    SectionHeader("Today's Tasks", onSeeAll = onSeeAllTasks)
                }

                items(pendingTasks) { task ->
                    TaskCard(task, onClick = { onTaskClick(task.id) }, onComplete = { viewModel.completeTask(task) })
                }
            } else if (habits.isNotEmpty()) {
                item {
                    Text("No pending tasks for today", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // 9. TODAY'S GRAPH
            item {
                Spacer(Modifier.height(8.dp))
                Text("Overall Growth", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                
                val growthData = remember(allProgress, habits, selectedFilter) {
                    com.example.ring_2.logic.GrowthCalculator.calculateOverallGrowth(allProgress, habits, selectedFilter)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    tonalElevation = 2.dp
                ) {
                    LineGraph(
                        dataPoints = growthData.map { it.growthValue },
                        labels = growthData.map { it.dateLabel },
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize(),
                        tooltipData = growthData.map { point ->
                            """
                            Date: ${point.fullDate}
                            Aggregate: ${point.dailyAggregate.toInt()}%
                            Previous: ${point.prevAggregate.toInt()}%
                            Difference: ${if (point.difference >= 0) "+" else ""}${point.difference.toInt()}%
                            Average Streak: ${point.averageStreak.toInt()} days
                            Growth: ${String.format(Locale.getDefault(), "%.1f", point.growthValue)}
                            """.trimIndent()
                        }
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Time Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Days", "Weeks", "Months", "Year").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    showNumericDialogFor?.let { habit ->
        val habitProgress = todayProgress.find { it.habitId == habit.id }
        NumericInputDialog(
            title = "Today's Progress",
            target = "${habit.target.toInt()} ${habit.unit}",
            initialValue = habitProgress?.actualValue?.toInt()?.toString() ?: "",
            onDismiss = { showNumericDialogFor = null },
            onSave = { value, note ->
                viewModel.recordProgress(habit, value, note)
                showNumericDialogFor = null
            }
        )
    }
}

@Composable
fun QuickActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyHabitState(onAddHabit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No habits yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "Create your first habit and start your RING.",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        Button(
            onClick = onAddHabit,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ Add Habit", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HomeHeader(name: String, points: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val calendar = Calendar.getInstance()
            val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> "Good Morning,"
                in 12..16 -> "Good Afternoon,"
                else -> "Good Evening,"
            }
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            Text(
                text = sdf.format(Date()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(width = 120.dp, height = 54.dp),
            tonalElevation = 2.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("ELITE POINTS", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text(points.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun getMidnightTimestamp(time: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = time
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}
