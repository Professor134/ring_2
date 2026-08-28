package com.example.ring_2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.components.LineGraph
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    viewModel: MainViewModel,
    habitId: Long,
    onBack: () -> Unit,
    onEditHabit: () -> Unit,
) {
    val habits by viewModel.allHabits.collectAsState()
    val habit = habits.find { it.id == habitId } ?: return
    
    val progressList by viewModel.getProgressForHabit(habitId).collectAsState(initial = emptyList())
    val transactions by viewModel.getTransactionsForHabit(habitId).collectAsState(initial = emptyList())
    val userProg by viewModel.userProgress.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Days") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(habit.name, color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditHabit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF00E676))
                    }
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            "${userProg?.currentPoints ?: 0}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // SECTION 1: HABIT SUMMARY
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailStatCard(Modifier.weight(1f), habit.currentStreak.toString(), "Current Streak")
                DetailStatCard(Modifier.weight(1f), habit.bestStreak.toString(), "Best Streak")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailStatCard(Modifier.weight(1f), habit.totalCompletions.toString(), "Total Done")
                val successRate = if (progressList.isEmpty()) 0 else (progressList.count { it.completed } * 100 / progressList.size)
                DetailStatCard(Modifier.weight(1f), "$successRate%", "Success Rate")
            }

            // SECTION 2: TARGETS
            TargetsSection(habit)

            // SECTION 3: HABIT NOTE
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("HABIT NOTE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = habit.description.ifEmpty { "No note added." },
                        modifier = Modifier.padding(16.dp),
                        color = if (habit.description.isEmpty()) Color.DarkGray else Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // SECTION 4: TIME FILTER
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val filters = listOf("Days", "Weeks", "Months", "Year")
                SingleChoiceSegmentedButtonRow {
                    filters.forEachIndexed { index, filter ->
                        SegmentedButton(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = filters.size)
                        ) {
                            Text(filter)
                        }
                    }
                }
            }

            // SECTION 5: LINE GRAPH
            DetailChartSection("Progress Trend", if (selectedFilter == "Days") "Calendar dates" else selectedFilter) {
                val graphData = progressList.take(7).map { it.percentage.toFloat() }.reversed()
                LineGraph(
                    dataPoints = if (graphData.isEmpty()) listOf(0f) else graphData,
                    color = Color(habit.color),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // SECTION 7: STREAK MILESTONES
            StreakMilestoneSection(habit.currentStreak)

            // SECTION 8: CALENDAR
            CalendarSection(habit, progressList)

            // SECTION 9: PROGRESS NOTES
            ProgressNotesSection(progressList)

            // SECTION 10: POINT SUMMARY
            PointSummarySection(transactions)

            // SECTION 12: DELETE
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                Spacer(Modifier.width(8.dp))
                Text("Delete Habit", color = Color.Red)
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Habit?") },
            text = { Text("Historical records will be preserved where possible. This costs 50 Elite Points.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(habitId)
                    onBack()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun DetailStatCard(modifier: Modifier, value: String, label: String) {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TargetsSection(habit: HabitEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("TARGETS", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val unit = habit.unit
                val target = habit.target.toInt()
                TargetRow("Daily target", if (habit.type == HabitType.MEASURABLE) "$target $unit" else "Once per scheduled day")
                TargetRow("Weekly target", if (habit.type == HabitType.MEASURABLE) "${target * 7} $unit" else "Calculated per schedule")
                TargetRow("Monthly target", "—")
                TargetRow("Yearly target", "—")
            }
        }
    }
}

@Composable
fun TargetRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun StreakMilestoneSection(currentStreak: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("STREAK MILESTONES", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MilestoneCard(Modifier.weight(1f), "10 Days", currentStreak >= 10)
            MilestoneCard(Modifier.weight(1f), "20 Days", currentStreak >= 20)
        }
    }
}

@Composable
fun MilestoneCard(modifier: Modifier, label: String, isAchieved: Boolean) {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(80.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    if (isAchieved) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isAchieved) Color(0xFF00E676) else Color.DarkGray
                )
                Text(label, fontSize = 12.sp, color = if (isAchieved) Color.White else Color.Gray)
            }
        }
    }
}

@Composable
fun CalendarSection(habit: HabitEntity, progressList: List<com.example.ring_2.data.model.HabitProgressEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("CALENDAR", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                        Text(it, color = Color.DarkGray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(8.dp))

                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                val progressMap = progressList.associateBy { it.date }

                var day = 1
                for (row in 0..5) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (col in 1..7) {
                            val isCurrentMonthDay = (row == 0 && col >= firstDayOfWeek) || (row > 0 && day <= daysInMonth)

                            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                if (isCurrentMonthDay) {
                                    val cellCal = Calendar.getInstance().apply {
                                        set(currentYear, currentMonth, day, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    val dateMillis = cellCal.timeInMillis
                                    val progress = progressMap[dateMillis]
                                    val isActive = com.example.ring_2.logic.ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, dateMillis)
                                    val isFuture = dateMillis > System.currentTimeMillis()

                                    val color = when {
                                        isFuture -> Color.Transparent
                                        progress?.completed == true -> Color(0xFF00E676)
                                        progress != null && progress.actualValue > 0 -> Color(0xFF00E676).copy(alpha = 0.5f)
                                        isActive -> Color.Red.copy(alpha = 0.3f)
                                        else -> Color.Transparent
                                    }

                                    Surface(
                                        color = color,
                                        shape = CircleShape,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = day.toString(),
                                                fontSize = 11.sp,
                                                color = if (color != Color.Transparent) Color.Black else Color.White
                                            )
                                        }
                                    }
                                    day++
                                }
                            }
                        }
                    }
                    if (day > daysInMonth) break
                }
            }
        }
    }
}

@Composable
fun ProgressNotesSection(progressList: List<com.example.ring_2.data.model.HabitProgressEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PROGRESS HISTORY & NOTES", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        if (progressList.isEmpty()) {
            Text("No progress recorded yet.", color = Color.DarkGray, fontSize = 14.sp)
        } else {
            progressList.take(5).forEach { progress ->
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                            Text(sdf.format(Date(progress.date)), color = Color.Gray, fontSize = 12.sp)
                            Text("${progress.actualValue.toInt()} / ${progress.target.toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (progress.note.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("\"${progress.note}\"", color = Color(0xFF00E676), fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PointSummarySection(transactions: List<com.example.ring_2.data.model.PointTransactionEntity>) {
    val earned = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val lost = transactions.filter { it.amount < 0 }.sumOf { it.amount }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("POINT SUMMARY", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PointSummaryRow("Elite Points Earned", "+$earned", Color(0xFF00E676))
                PointSummaryRow("Elite Points Lost", "$lost", Color.Red)
                HorizontalDivider(color = Color.DarkGray)
                PointSummaryRow("Net", "${earned + lost}", Color.White)
            }
        }
    }
}

@Composable
fun PointSummaryRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun DetailChartSection(title: String, subtitle: String, chart: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(200.dp)
        ) {
            chart()
        }
    }
}
