package com.example.ring_2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitProgressEntity
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.logic.DateTimeUtils
import com.example.ring_2.logic.ScheduleEngine
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.components.LineGraph
import com.example.ring_2.ui.components.NumericInputDialog
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
    var selectedDateForEdit by remember { mutableStateOf<Long?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(habit.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = onEditHabit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = (userProg?.currentPoints ?: 0).toString(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary,
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
                DetailStatCard(Modifier.weight(1f), habit.currentStreak.toString(), stringResource(com.example.ring_2.R.string.label_current_streak))
                DetailStatCard(Modifier.weight(1f), habit.bestStreak.toString(), stringResource(com.example.ring_2.R.string.label_best_streak))
            }

            // SECTION 2: TARGETS (Updated with Progress Bars)
            HabitTargetsProgressSection(habit, progressList)

            // SECTION 3: HABIT NOTE
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(com.example.ring_2.R.string.label_habit_note), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = habit.description.ifEmpty { "No note added." },
                        modifier = Modifier.padding(16.dp),
                        color = if (habit.description.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }

            // SECTION 4: TIME FILTER
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val filters = listOf(
                    stringResource(com.example.ring_2.R.string.filter_days) to "Days",
                    stringResource(com.example.ring_2.R.string.filter_weeks) to "Weeks",
                    stringResource(com.example.ring_2.R.string.filter_months) to "Months",
                    stringResource(com.example.ring_2.R.string.filter_year) to "Year"
                )
                SingleChoiceSegmentedButtonRow {
                    filters.forEachIndexed { index, (label, value) ->
                        SegmentedButton(
                            selected = selectedFilter == value,
                            onClick = { selectedFilter = value },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = filters.size)
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            // SECTION 5: LINE GRAPH
            DetailChartSection("Progress Trend", if (selectedFilter == "Days") "Last 7 entries" else selectedFilter) {
                val graphData = progressList.take(7).map { it.actualValue.toFloat() }.reversed()
                val labels = progressList.take(7).map { SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(it.date)) }.reversed()
                LineGraph(
                    dataPoints = graphData.ifEmpty { listOf(0f) },
                    labels = labels,
                    color = Color(habit.color),
                    modifier = Modifier.fillMaxSize(),
                    yAxisMax = if (habit.type == HabitType.MEASURABLE) habit.target.toFloat() * 1.5f else 1.2f
                )
            }

            // SECTION 6: CALENDAR (Interactive)
            InteractiveCalendarSection(habit, progressList) { date ->
                val fiveDaysAgo = DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L))
                if (date >= fiveDaysAgo && date <= System.currentTimeMillis()) {
                    selectedDateForEdit = date
                    showEditDialog = true
                }
            }

            // SECTION 7: POINT SUMMARY
            PointSummarySection(transactions)

            // SECTION 8: DELETE
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(com.example.ring_2.R.string.btn_delete_habit), color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }

    if (showEditDialog && selectedDateForEdit != null) {
        val progress = progressList.find { it.date == selectedDateForEdit }
        if (habit.type == HabitType.YES_NO) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Update Progress") },
                text = { Text("Did you complete this habit on ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(selectedDateForEdit!!))}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.recordProgressForDate(habit, selectedDateForEdit!!, habit.target)
                        showEditDialog = false
                    }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.recordProgressForDate(habit, selectedDateForEdit!!, 0.0)
                        showEditDialog = false
                    }) { Text("No") }
                }
            )
        } else {
            NumericInputDialog(
                title = "Update Progress",
                initialValue = (progress?.actualValue ?: 0.0).toString(),
                target = "${habit.target} ${habit.unit}",
                onDismiss = { showEditDialog = false }
            ) { value, note ->
                viewModel.recordProgressForDate(habit, selectedDateForEdit!!, value, note)
                showEditDialog = false
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(com.example.ring_2.R.string.dialog_delete_habit_title)) },
            text = { Text(stringResource(com.example.ring_2.R.string.dialog_delete_habit_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(habitId)
                    onBack()
                }) {
                    Text(stringResource(com.example.ring_2.R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(com.example.ring_2.R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun HabitTargetsProgressSection(habit: HabitEntity, progressList: List<HabitProgressEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("TARGETS & PROGRESS", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val dailyStats = calculateStats(progressList, Calendar.DAY_OF_YEAR, habit)
                TargetProgressBar("Daily", dailyStats, Color(habit.color))
                
                val weeklyStats = calculateStats(progressList, Calendar.WEEK_OF_YEAR, habit)
                TargetProgressBar("Weekly", weeklyStats, Color(habit.color))
                
                val monthlyStats = calculateStats(progressList, Calendar.MONTH, habit)
                TargetProgressBar("Monthly", monthlyStats, Color(habit.color))
            }
        }
    }
}

data class ProgressStats(val done: Double, val total: Double, val unit: String)

fun calculateStats(progressList: List<HabitProgressEntity>, period: Int, habit: HabitEntity): ProgressStats {
    val cal = Calendar.getInstance()
    val today = DateTimeUtils.getMidnightTimestamp(cal.timeInMillis)
    
    // Set to start of period
    cal.timeInMillis = today
    when(period) {
        Calendar.WEEK_OF_YEAR -> cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        Calendar.MONTH -> cal.set(Calendar.DAY_OF_MONTH, 1)
        Calendar.YEAR -> cal.set(Calendar.DAY_OF_YEAR, 1)
        else -> {} // Day is just today
    }
    val startOfPeriod = DateTimeUtils.getMidnightTimestamp(cal.timeInMillis)
    
    // Calculate total target for the FULL period based on active days in schedule
    val endOfPeriodCal = cal.clone() as Calendar
    when(period) {
        Calendar.WEEK_OF_YEAR -> endOfPeriodCal.add(Calendar.DAY_OF_YEAR, 6)
        Calendar.MONTH -> {
            endOfPeriodCal.add(Calendar.MONTH, 1)
            endOfPeriodCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        Calendar.YEAR -> {
            endOfPeriodCal.add(Calendar.YEAR, 1)
            endOfPeriodCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        else -> {} // Day is just today
    }
    val endOfPeriod = DateTimeUtils.getMidnightTimestamp(endOfPeriodCal.timeInMillis)

    var total = 0.0
    val tempCal = Calendar.getInstance()
    tempCal.timeInMillis = startOfPeriod
    while (tempCal.timeInMillis <= endOfPeriod) {
        if (ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, tempCal.timeInMillis)) {
            total += habit.target
        }
        tempCal.add(Calendar.DAY_OF_YEAR, 1)
    }

    // Done is measured up to today
    val filtered = progressList.filter { it.date in startOfPeriod..today }
    val done = filtered.sumOf { it.actualValue }
    
    return ProgressStats(done, total, habit.unit)
}

@Composable
fun TargetProgressBar(label: String, stats: ProgressStats, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("${stats.done.toInt()} / ${stats.total.toInt()} ${stats.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        val progress = if (stats.total > 0) (stats.done / stats.total).toFloat().coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun InteractiveCalendarSection(habit: HabitEntity, progressList: List<HabitProgressEntity>, onDateClick: (Long) -> Unit) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(com.example.ring_2.R.string.label_calendar), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val newCal = calendar.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        calendar = newCal
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = {
                        val newCal = calendar.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        calendar = newCal
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(8.dp))

                val tempCal = calendar.clone() as Calendar
                tempCal.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
                val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

                val progressMap = progressList.associateBy { it.date }

                var day = 1
                for (row in 0..5) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (col in 1..7) {
                            val isCurrentMonthDay = (row == 0 && col >= firstDayOfWeek) || (row > 0 && day <= daysInMonth)

                            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                if (isCurrentMonthDay) {
                                    val cellCal = calendar.clone() as Calendar
                                    cellCal.set(Calendar.DAY_OF_MONTH, day)
                                    val dateMillis = DateTimeUtils.getMidnightTimestamp(cellCal.timeInMillis)
                                    val progress = progressMap[dateMillis]
                                    val isActive = ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, dateMillis)
                                    val isFuture = dateMillis > System.currentTimeMillis()

                                    val bgColor = when {
                                        isFuture -> Color.Transparent
                                        progress?.completed == true -> MaterialTheme.colorScheme.primary
                                        progress != null && progress.actualValue > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        isActive -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    }

                                    Surface(
                                        color = bgColor,
                                        shape = CircleShape,
                                        modifier = Modifier.size(32.dp).clickable { onDateClick(dateMillis) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = day.toString(),
                                                fontSize = 12.sp,
                                                color = if (bgColor != Color.Transparent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
fun DetailStatCard(modifier: Modifier, value: String, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PointSummarySection(transactions: List<com.example.ring_2.data.model.PointTransactionEntity>) {
    val earned = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val lost = transactions.filter { it.amount < 0 }.sumOf { it.amount }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(com.example.ring_2.R.string.label_point_summary), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PointSummaryRow("Elite Points Earned", "+$earned", MaterialTheme.colorScheme.primary)
                PointSummaryRow("Elite Points Lost", "$lost", MaterialTheme.colorScheme.error)
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                PointSummaryRow("Net", "${earned + lost}", MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun PointSummaryRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun DetailChartSection(title: String, subtitle: String, chart: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(240.dp)
        ) {
            chart()
        }
    }
}
