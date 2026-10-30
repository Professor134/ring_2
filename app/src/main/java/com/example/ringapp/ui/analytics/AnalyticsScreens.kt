// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\analytics\AnalyticsScreens.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.analytics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.engine.ScheduleEngine
import com.example.ringapp.ui.theme.CyberNeonGreen
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
// Handles the day start logic for this component without modifying runtime behavior.
// Manages the day start flow without changing runtime behavior.
private fun dayStart(date: LocalDate) = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

@Composable
// Handles the main analytics screen logic for this component without modifying runtime behavior.
// Manages the main analytics screen flow without changing runtime behavior.
fun MainAnalyticsScreen(onPersonal: () -> Unit = {}, viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var range by remember { mutableStateOf("Days") }
    var category by remember { mutableStateOf<Long?>(null) }

    val points = when (range) {
        "Weeks" -> {
            val today = LocalDate.now()
            (6 downTo 0).map { w ->
                val endDate = today.minusDays((w * 7).toLong())
                val startDate = endDate.minusDays(6)
                val weekPoints = (0..6).mapNotNull { offset ->
                    aggregateDaily(
                        state.habits,
                        state.progress,
                        startDate.plusDays(offset.toLong())
                    )
                }
                val avgScore = if (weekPoints.isNotEmpty()) weekPoints.map { it.score }.average()
                    .toInt() else 0
                LinePoint(startDate.format(DateTimeFormatter.ofPattern("d MMM")), avgScore)
            }
        }

        "Months" -> {
            val today = LocalDate.now()
            (6 downTo 0).map { m ->
                val monthDate = today.minusMonths(m.toLong())
                val startOfMonth = monthDate.withDayOfMonth(1)
                val endOfMonth =
                    if (m == 0) today else monthDate.withDayOfMonth(monthDate.lengthOfMonth())
                var current = startOfMonth
                val monthPoints = mutableListOf<AnalyticsPoint>()
                while (!current.isAfter(endOfMonth)) {
                    aggregateDaily(
                        state.habits,
                        state.progress,
                        current
                    )?.let { monthPoints.add(it) }
                    current = current.plusDays(1)
                }
                val avgScore = if (monthPoints.isNotEmpty()) monthPoints.map { it.score }.average()
                    .toInt() else 0
                LinePoint(monthDate.format(DateTimeFormatter.ofPattern("MMM")), avgScore)
            }
        }

        "Year" -> {
            val today = LocalDate.now()
            (11 downTo 0).map { m ->
                val monthDate = today.minusMonths(m.toLong())
                val startOfMonth = monthDate.withDayOfMonth(1)
                val endOfMonth =
                    if (m == 0) today else monthDate.withDayOfMonth(monthDate.lengthOfMonth())
                var current = startOfMonth
                val monthPoints = mutableListOf<AnalyticsPoint>()
                while (!current.isAfter(endOfMonth)) {
                    aggregateDaily(
                        state.habits,
                        state.progress,
                        current
                    )?.let { monthPoints.add(it) }
                    current = current.plusDays(1)
                }
                val avgScore = if (monthPoints.isNotEmpty()) monthPoints.map { it.score }.average()
                    .toInt() else 0
                LinePoint(monthDate.format(DateTimeFormatter.ofPattern("MMM")), avgScore)
            }
        }

        else -> {
            (10 downTo 0).map { d ->
                val date = LocalDate.now().minusDays(d.toLong())
                val p = aggregateDaily(state.habits, state.progress, date)
                LinePoint(date.format(dateFormatter), p?.score ?: 0)
            }
        }
    }
    val dates = (10 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
    val filteredCategories = state.categories.filter { it.name.lowercase() != "habits" }
    val categoryNames =
        listOf(null to "All Categories") + filteredCategories.map { it.id to it.name }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    val displayedPoints by animateIntAsState(
        targetValue = state.points,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "animatedMainPoints"
    )

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(1000)) + slideInVertically(initialOffsetY = { 50 })
        ) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "INSIGHTS",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                letterSpacing = 2.sp
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    "ELITE $displayedPoints",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Metric(
                                "Score",
                                "${points.lastOrNull()?.score ?: 0}%",
                                Modifier.weight(1f),
                                textColor
                            )
                            Metric("Streak", "${state.bestStreak}", Modifier.weight(1f), textColor)
                            Metric(
                                "Done",
                                state.habits.sumOf { it.totalCompletions }.toString(),
                                Modifier.weight(1f),
                                textColor
                            )
                        }
                    }
                    item { TimeFilters(range) { range = it } }
                    item {
                        Text(
                            "GROWTH MATRIX",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = textColor.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.2f
                                )
                            )
                        ) {
                            AnalyticsLineChart(
                                points,
                                Modifier.fillMaxWidth().height(220.dp).padding(16.dp)
                            )
                        }
                    }

                    item {
                        Text(
                            "CATEGORY DYNAMICS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = textColor.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categoryNames.size) { index ->
                                val item = categoryNames[index]
                                val categoryColor = item.second.let { name ->
                                    if (name == "All Categories") MaterialTheme.colorScheme.primary
                                    else Color(CategoryConstants.getColorForCategory(name))
                                }
                                FilterChip(
                                    selected = category == item.first,
                                    onClick = { category = item.first },
                                    label = { Text(item.second.uppercase()) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        labelColor = textColor.copy(
                                            alpha = 0.6f
                                        ),
                                        selectedLabelColor = Color.White,
                                        selectedContainerColor = categoryColor
                                    )
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
    }

// Manages the personal analytics screen flow without changing runtime behavior.
    @Composable
// Manages the personal analytics screen flow without changing runtime behavior.
    fun PersonalAnalyticsScreen(
        onBack: () -> Unit = {},
        onEdit: (Long) -> Unit = {},
        viewModel: PersonalAnalyticsViewModel = hiltViewModel()
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        var range by remember { mutableStateOf("Days") }
        var selected by remember { mutableStateOf<LinePoint?>(null) }
        var confirmDelete by remember { mutableStateOf(false) }

        val displayedPoints by animateIntAsState(
            targetValue = state.points,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            label = "animatedPersonalPoints"
        )

        val habit = state.habit
        if (state.loading || habit == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    state.error ?: "Loading neural data...", Modifier.padding(24.dp)
                )
            }; return
        }

        if (habit.isBmiHabit()) {
            BmiAnalyticsScreen(
                habit = habit,
                profile = state.profile,
                onBack = onBack
            )
            return
        }

        val isSteps = habit.isStepsHabit()
        val today = LocalDate.now()
        val habitStart = java.time.Instant.ofEpochMilli(habit.startDate).atZone(ZoneId.systemDefault()).toLocalDate()

        data class PeriodPoint(
            val label: String,
            val actual: Double,
            val target: Double,
            val completed: Boolean,
            val date: LocalDate? = null
        )

        // Filter ONLY active scheduled dates for graph points (Requirement 10: Inactive dates must not produce graph points or zero values)
        val activeDatesInPeriod = mutableListOf<LocalDate>()
        var scanDate = habitStart
        while (!scanDate.isAfter(today)) {
            if (ScheduleEngine.isActiveOnDate(habit, scanDate)) {
                activeDatesInPeriod.add(scanDate)
            }
            scanDate = scanDate.plusDays(1)
        }

        val limitCount = when (range) {
            "Weeks" -> 28
            "Months" -> 90
            "Year" -> 365
            else -> 14 // Days: past 14 active days
        }
        val selectedActiveDates = activeDatesInPeriod.takeLast(limitCount)

        val periodPoints = if (selectedActiveDates.isNotEmpty()) {
            selectedActiveDates.map { date ->
                val record = state.progress.firstOrNull { it.date == dayStart(date) }
                val actual = record?.actual ?: 0.0
                val dayTarget = habit.getTargetForDate(date)
                val completed = actual >= dayTarget && dayTarget > 0.0
                PeriodPoint(
                    label = date.format(dateFormatter),
                    actual = actual,
                    target = dayTarget,
                    completed = completed,
                    date = date
                )
            }
        } else {
            // Fallback for brand new habits
            listOf(
                PeriodPoint(
                    label = today.format(dateFormatter),
                    actual = 0.0,
                    target = habit.getTargetForDate(today),
                    completed = false,
                    date = today
                )
            )
        }

        val maxActual = periodPoints.maxOfOrNull { it.actual } ?: 0.0
        val maxTarget = periodPoints.maxOfOrNull { it.target } ?: 1.0
        val dynamicCeiling = maxOf(maxActual, maxTarget, 1.0)

        val points = periodPoints.map { p ->
            val score = if (dynamicCeiling > 0) (p.actual * 100 / dynamicCeiling).toInt().coerceIn(0, 100) else 0
            LinePoint(
                p.label,
                score,
                p.actual,
                p.target,
                p.completed,
                date = p.date,
                isActive = true
            )
        }

        val activePoints = points
        val successRate =
            if (activePoints.isEmpty()) 0 else activePoints.count { it.completed } * 100 / activePoints.size
        val categoryName = if (isSteps) "System" else (state.category?.name ?: "Personal")
        val habitColor = if (isSteps) Color(HabitEntity.PLATINUM_COLOR) else Color(
            CategoryConstants.getColorForCategory(categoryName)
        )

        val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
        val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

        Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(
                    Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    "Back",
                                    tint = textColor
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    habit.name.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = textColor,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    if (isSteps) "Neural Link Active" else "${categoryName.uppercase()} ANALYTICS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor.copy(alpha = 0.5f)
                                )
                            }
                            if (!isSteps) IconButton(onClick = { onEdit(habit.id) }) {
                                Icon(
                                    Icons.Default.Edit,
                                    "Edit",
                                    tint = textColor
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Metric(
                                "Streak",
                                "${habit.currentStreak} 🔥",
                                Modifier.weight(1f),
                                textColor
                            )
                            Metric(
                                "Habit Pts",
                                state.habitPoints.toString(),
                                Modifier.weight(1f),
                                textColor
                            )
                            Metric(
                                "Elite Pts",
                                displayedPoints.toString(),
                                Modifier.weight(1f),
                                textColor
                            )
                        }
                    }
                    if (!isSteps) item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Metric(
                                "Total Done",
                                habit.totalCompletions.toString(),
                                Modifier.weight(1f),
                                textColor
                            ); Metric("Success", "$successRate%", Modifier.weight(1f), textColor)
                        }
                    }

                    val desc = habit.description
                    if (!desc.isNullOrBlank() && !isSteps) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    )
                                ),
                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "PROTOCOL DESCRIPTION",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = textColor.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        desc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    if (!isSteps && !habit.isBmiHabit() && habit.type == HabitType.MEASURABLE) {
                        item {
                            val todayRec = state.progress.firstOrNull { it.date == dayStart(today) }
                            val progressActual = todayRec?.actual ?: 0.0
                            val todayTarget = habit.getTargetForDate(today)

                            DualCircularProgressIndicator(
                                progressActual = progressActual,
                                sets = habit.sets,
                                repsPerSet = habit.repsPerSet,
                                target = todayTarget,
                                baseColor = habitColor,
                                unit = habit.unit.orEmpty(),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                    if (!isSteps) item { Targets(habit, state.progress, habitColor, textColor) }
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.2f
                                )
                            )
                        ) {
                            AnalyticsLineChart(
                                activePoints,
                                Modifier.fillMaxWidth().height(220.dp).padding(16.dp),
                                color = habitColor,
                                onPointTap = { selected = it })
                        }
                    }
                    if (selected != null) item {
                        val point = selected!!
                        val record = if (point.date != null) state.progress.firstOrNull {
                            it.date == dayStart(point.date)
                        } else null
                        val dayTarget = point.date?.let { habit.getTargetForDate(it) } ?: point.target
                        val actualVal = point.actual

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, habitColor.copy(alpha = 0.3f))
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = point.date?.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")) ?: point.label,
                                    fontWeight = FontWeight.Black,
                                    color = habitColor
                                )

                                val valFormatted = if (actualVal == actualVal.toInt().toDouble()) actualVal.toInt().toString() else String.format(Locale.US, "%.1f", actualVal)
                                val targetFormatted = if (dayTarget == dayTarget.toInt().toDouble()) dayTarget.toInt().toString() else String.format(Locale.US, "%.1f", dayTarget)
                                val unitStr = habit.unit.orEmpty()

                                Text(
                                    text = "Recorded Progress: $valFormatted $unitStr (Target: $targetFormatted $unitStr)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (habit.sets > 1 && habit.repsPerSet > 1 && actualVal > 0) {
                                    val completeSets = actualVal.toInt() / habit.repsPerSet
                                    val partialReps = actualVal.toInt() % habit.repsPerSet
                                    val setStr = if (partialReps > 0) "$completeSets complete sets + $partialReps reps" else "$completeSets complete sets"
                                    Text(
                                        text = "Sets breakdown: $setStr (${habit.sets} sets × ${habit.repsPerSet} reps)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor.copy(alpha = 0.7f)
                                    )
                                }

                                val statusText: String
                                val statusColor: Color
                                if (point.date != null && point.date.isAfter(LocalDate.now())) {
                                    statusText = "Scheduled"
                                    statusColor = Color(0xFF2196F3)
                                } else if (actualVal >= dayTarget && dayTarget > 0) {
                                    statusText = "Target Achieved"
                                    statusColor = Color(0xFF4CAF50)
                                } else if (actualVal > 0) {
                                    statusText = "Partial Progress ($valFormatted / $targetFormatted $unitStr)"
                                    statusColor = Color(0xFFFF9800)
                                } else {
                                    statusText = "Zero Progress"
                                    statusColor = Color(0xFFFF3131)
                                }

                                Surface(
                                    color = statusColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                if (!record?.note.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Log Note: ${record.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        HabitCalendar(habit, state.progress, textColor) { date ->
                            val record = state.progress.firstOrNull { it.date == dayStart(date) }
                            val score = ((record?.actual ?: 0.0) * 100 / dynamicCeiling).toInt()
                                .coerceIn(0, 100)
                            selected = LinePoint(
                                date.format(dateFormatter),
                                score,
                                record?.actual ?: 0.0,
                                dynamicCeiling,
                                record?.completed == true,
                                date = date
                            )
                        }
                    }
                    item { PointSummary(state.transactions, textColor) }
                    if (!isSteps) item {
                        Button(
                            onClick = { confirmDelete = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.White); Spacer(
                            Modifier.width(
                                8.dp
                            )
                        ); Text(
                            "TERMINATE HABIT",
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        }
                    }
                }
        }
        if (confirmDelete) AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Terminate Habit?") },
            text = { Text("Terminating the Habit Cost the -50 Points") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false; viewModel.delete()
                }) { Text("Terminate", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
    }

// Manages the targets flow without changing runtime behavior.
    @Composable
// Manages the targets flow without changing runtime behavior.
    private fun Targets(
        habit: HabitEntity,
        progress: List<HabitProgressEntity>,
        color: Color,
        textColor: Color
    ) {
        val unit = habit.unit.orEmpty()
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value % 7L)
        val monthStart = today.withDayOfMonth(1)
        val yearStart = today.withDayOfYear(1)

// Manages the calculate progress flow without changing runtime behavior.
        fun calculateProgress(start: LocalDate, end: LocalDate): Pair<Double, Double> {
            var completed = 0.0
            var targetTotal = 0.0
            var current = start
            while (!current.isAfter(end)) {
                if (ScheduleEngine.isActiveOnDate(habit, current)) {
                    targetTotal += habit.target
                    val record = progress.firstOrNull { it.date == dayStart(current) }
                    completed += record?.actual ?: 0.0
                }
                current = current.plusDays(1)
            }
            return completed to targetTotal
        }

        val todayRecord = progress.firstOrNull { it.date == dayStart(today) }
        val todayActive = ScheduleEngine.isActiveOnDate(habit, today)
        val weekProg = calculateProgress(weekStart, weekStart.plusDays(6))
        val monthProg =
            calculateProgress(monthStart, monthStart.withDayOfMonth(monthStart.lengthOfMonth()))
        val yearProg =
            calculateProgress(yearStart, yearStart.withDayOfYear(yearStart.lengthOfYear()))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "TARGET QUOTAS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = textColor.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
            TargetRow(
                "Daily",
                if (todayActive) (todayRecord?.actual ?: 0.0) else 0.0,
                if (todayActive) habit.target else 0.0,
                unit,
                habit.type,
                color,
                textColor
            )
            TargetRow("Weekly", weekProg.first, weekProg.second, unit, habit.type, color, textColor)
            TargetRow(
                "Monthly",
                monthProg.first,
                monthProg.second,
                unit,
                habit.type,
                color,
                textColor
            )
            TargetRow("Yearly", yearProg.first, yearProg.second, unit, habit.type, color, textColor)
        }
    }

// Manages the target row flow without changing runtime behavior.
    @Composable
// Manages the target row flow without changing runtime behavior.
    private fun TargetRow(
        label: String,
        completed: Double,
        target: Double,
        unit: String,
        type: HabitType,
        color: Color,
        textColor: Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = textColor, fontWeight = FontWeight.Bold)
                Text(
                    text = if (type == HabitType.MEASURABLE) String.format(
                        Locale.US,
                        "%.1f / %.1f %s",
                        completed,
                        target,
                        unit
                    ) else String.format(Locale.US, "%.0f / %.0f", completed, target),
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            }
            LinearProgressIndicator(
                progress = {
                    if (target > 0.0) (completed / target).toFloat().coerceIn(0f, 1f) else 0f
                },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }

// Manages the habit calendar flow without changing runtime behavior.
    @Composable
    private fun HabitCalendar(
        habit: HabitEntity,
        progress: List<HabitProgressEntity>,
        textColor: Color,
        onDateClick: (LocalDate) -> Unit
    ) {
        var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
        val habitStart = java.time.Instant.ofEpochMilli(habit.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CHRONO LOG",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = textColor.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Previous Month",
                            Modifier.size(20.dp),
                            tint = textColor
                        )
                    }
                    Text(
                        currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")).uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = {
                        currentMonth = currentMonth.plusMonths(1)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Next Month",
                            Modifier.size(20.dp).rotate(180f),
                            tint = textColor
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        day,
                        Modifier.width(34.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Black
                    )
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
                            val dayTarget = habit.getTargetForDate(date)

                            val (borderColor, textColorForDate, bgColor) = when {
                                // 1. Pre-installation / habit start
                                date.isBefore(habitStart) -> {
                                    Triple(Color.Gray.copy(alpha = 0.5f), Color.White, Color.Gray.copy(alpha = 0.15f))
                                }
                                // 2. Future dates that are scheduled as active
                                date.isAfter(today) && active -> {
                                    Triple(Color(0xFF2196F3), Color.White, Color(0xFF2196F3).copy(alpha = 0.18f))
                                }
                                // 3. Future dates that are inactive
                                date.isAfter(today) -> {
                                    Triple(Color.White.copy(alpha = 0.5f), Color.White, Color.Gray.copy(alpha = 0.15f))
                                }
                                // 4. Past/today dates that are inactive
                                !active -> {
                                    Triple(Color.White.copy(alpha = 0.5f), Color.White, Color.Gray.copy(alpha = 0.15f))
                                }
                                // 5. Active past/today dates with COMPLETED progress
                                record != null && record.actual >= dayTarget && dayTarget > 0.0 -> {
                                    Triple(Color(0xFF4CAF50), Color.White, Color(0xFF4CAF50).copy(alpha = 0.18f))
                                }
                                // 6. Active past/today dates with PARTIAL progress (> 0 but < target)
                                record != null && record.actual > 0.0 -> {
                                    Triple(Color(0xFFFF9800), Color.White, Color(0xFFFF9800).copy(alpha = 0.18f))
                                }
                                // 7. Active past/today dates with ZERO progress
                                else -> {
                                    Triple(Color(0xFFFF3131), Color.White, Color(0xFFFF3131).copy(alpha = 0.18f))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .border(1.5.dp, borderColor, CircleShape)
                                    .clickable { onDateClick(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textColorForDate
                                )
                            }
                        } else {
                            Spacer(Modifier.size(34.dp))
                        }
                    }
                }
            }
        }
    }

// Manages the point summary flow without changing runtime behavior.
    @Composable
// Manages the point summary flow without changing runtime behavior.
    private fun PointSummary(transactions: List<PointTransactionEntity>, textColor: Color) {
        val earned = transactions.filter { it.amount > 0 }.sumOf { it.amount };
        val lost = transactions.filter { it.amount < 0 }.sumOf { it.amount }; Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "POINTS AUDIT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = textColor.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            ); Text(
            "Accumulated: +$earned",
            color = textColor,
            fontWeight = FontWeight.Bold
        ); Text(
            "Expended: $lost",
            color = textColor.copy(alpha = 0.7f)
        ); Text(
            "Net Balance: ${earned + lost}",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        }
    }

// Manages the time filters flow without changing runtime behavior.
    @Composable
// Manages the time filters flow without changing runtime behavior.
    private fun TimeFilters(current: String, onChange: (String) -> Unit) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf(
                "Days",
                "Weeks",
                "Months",
                "Year"
            ).forEachIndexed { index, value ->
                SegmentedButton(
                    current == value,
                    { onChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, 4)
                ) {
                    Text(
                        value.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }

// Manages the metric flow without changing runtime behavior.
    @Composable
// Manages the metric flow without changing runtime behavior.
    private fun Metric(label: String, value: String, modifier: Modifier, textColor: Color) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                )
            )
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Black
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            }
        }
    }

// Represents the line point structure used by the application.
    data class LinePoint(
        val label: String,
        val score: Int,
        val actual: Double = 0.0,
        val target: Double = 0.0,
        val completed: Boolean = false,
        val date: LocalDate? = null,
        val isActive: Boolean = true
    )

// Manages the analytics line chart flow without changing runtime behavior.
    @Composable
// Manages the analytics line chart flow without changing runtime behavior.
    private fun AnalyticsLineChart(
        points: List<LinePoint>,
        modifier: Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        onPointTap: (LinePoint) -> Unit = {}
    ) {
        if (points.isEmpty()) {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    "NO METRIC DATA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            return
        }

        val animatable = remember { Animatable(0f) }
        LaunchedEffect(points) {
            animatable.snapTo(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
        val progress = animatable.value
        val primaryColor = color

        val actualValues = points.map { it.actual }
        val minVal = actualValues.minOrNull() ?: 0.0
        val maxVal = actualValues.maxOrNull() ?: 100.0
        val valRange = (maxVal - minVal).let { if (it <= 0.0) 10.0 else it }

        Canvas(modifier.pointerInput(points) {
            detectTapGestures { position ->
                if (points.isNotEmpty()) {
                    val index = ((position.x / size.width) * points.size).toInt()
                        .coerceIn(0, points.lastIndex)
                    onPointTap(points[index])
                }
            }
        }) {
            val left = 24f
            val bottom = size.height - 24f
            val top = 16f
            val width = size.width - left - 12f
            val height = bottom - top

            repeat(4) { i ->
                val y = top + (height * i / 3f)
                drawLine(
                    Color.LightGray.copy(alpha = 0.12f),
                    Offset(left, y),
                    Offset(size.width - 8f, y),
                    1.dp.toPx()
                )
            }

            val clipRight = left + width * progress

            clipRect(left = 0f, top = 0f, right = clipRight, bottom = size.height) {
                val path = Path()
                val pointOffsets = mutableListOf<Offset>()

                points.forEachIndexed { index, point ->
                    val x = if (points.size > 1) left + width * index.toFloat() / points.lastIndex else left + width / 2f
                    val normalizedY = ((point.actual - minVal) / valRange).toFloat().coerceIn(0f, 1f)
                    val y = bottom - (height * normalizedY)
                    val offset = Offset(x, y)
                    pointOffsets.add(offset)

                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                if (points.size > 1) {
                    drawPath(
                        path,
                        primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path,
                        primaryColor.copy(alpha = 0.25f),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                pointOffsets.forEach { offset ->
                    drawCircle(Color.Black, radius = 5.dp.toPx(), center = offset)
                    drawCircle(primaryColor, radius = 4.dp.toPx(), center = offset)
                }
            }
        }
    }

// Manages the category chart flow without changing runtime behavior.
    @Composable
// Manages the category chart flow without changing runtime behavior.
    private fun CategoryChart(
        state: AnalyticsUiState,
        selected: Long?,
        dates: List<LocalDate>,
        modifier: Modifier,
        textColor: Color
    ) {
        val categories =
            (if (selected == null) state.categories else state.categories.filter { it.id == selected }).filter { it.name.lowercase() != "habits" }
        if (categories.isEmpty()) {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    "NO CATEGORY DATA",
                    color = textColor.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }
            return
        }
        Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            categories.forEach { category ->
                val color = Color(CategoryConstants.getColorForCategory(category.name))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.1f)),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(color, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                category.name.uppercase(),
                                color = textColor,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        AnalyticsLineChart(
                            dates.mapNotNull { date ->
                                aggregateDaily(
                                    state.habits.filter { it.categoryId == category.id },
                                    state.progress,
                                    date
                                )?.let { LinePoint(it.date.format(dateFormatter), it.score) }
                            },
                            Modifier.fillMaxWidth().height(80.dp),
                            color = color
                        )
                    }
                }
            }
        }
    }

// Manages the task summary flow without changing runtime behavior.
    @Composable
// Manages the task summary flow without changing runtime behavior.
    private fun TaskSummary(tasks: List<TaskEntity>, textColor: Color) {
        val completed = tasks.count { it.completed }
        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                )
            )
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "TASK AUDIT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = textColor.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tasks", color = textColor)
                    Text("${tasks.size}", fontWeight = FontWeight.Black)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Completed", color = CyberNeonGreen)
                    Text("$completed", fontWeight = FontWeight.Black, color = CyberNeonGreen)
                }
                val rate = if (tasks.isEmpty()) 0 else completed * 100 / tasks.size
                LinearProgressIndicator(
                    progress = { rate / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = CyberNeonGreen
                )
            }
        }
    }

// -----------------------------------------------------------------------------
// Dual Circular Progress Indicator for Measurable Habits (Requirement 11)
// Outer ring = Completed Sets relative to target sets
// Inner ring = Repetitions within current set relative to repsPerSet
// Center element = Round button displaying actual recorded progress
// -----------------------------------------------------------------------------
@Composable
fun DualCircularProgressIndicator(
    progressActual: Double,
    sets: Int,
    repsPerSet: Int,
    target: Double,
    baseColor: Color,
    unit: String,
    modifier: Modifier = Modifier,
    onRecordTap: (() -> Unit)? = null
) {
    val safeSets = sets.coerceAtLeast(1)
    val safeReps = repsPerSet.coerceAtLeast(1)

    val isSingleSetRep = (safeSets == 1 && safeReps == 1) || target <= 1.0

    val actualInt = progressActual.toInt()
    val completedSets = if (safeReps > 0) (actualInt / safeReps).coerceAtMost(safeSets) else 0
    val currentSetReps = if (safeReps > 0) (actualInt % safeReps) else 0

    val outerTargetProgress = (completedSets.toFloat() / safeSets.toFloat()).coerceIn(0f, 1f)
    val innerTargetProgress = if (completedSets >= safeSets) 1f else (currentSetReps.toFloat() / safeReps.toFloat()).coerceIn(0f, 1f)

    val animatedOuterProgress by animateFloatAsState(
        targetValue = outerTargetProgress,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
        label = "outerProgress"
    )

    val animatedInnerProgress by animateFloatAsState(
        targetValue = innerTargetProgress,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
        label = "innerProgress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeOuter = 10.dp.toPx()
                val strokeInner = 8.dp.toPx()
                val gap = 8.dp.toPx()

                val maxRadius = (size.minDimension / 2f)
                val outerRadius = maxRadius - (strokeOuter / 2f) - 4.dp.toPx()
                val innerRadius = outerRadius - (strokeOuter / 2f) - gap - (strokeInner / 2f)

                // Dynamic color intensity based on progress (mild at 0% to bright at 100%)
                val outerColor = baseColor.copy(alpha = (0.4f + 0.6f * animatedOuterProgress).coerceIn(0.4f, 1f))
                val innerColor = baseColor.copy(alpha = (0.4f + 0.6f * animatedInnerProgress).coerceIn(0.4f, 1f))

                // Outer Track
                drawCircle(
                    color = baseColor.copy(alpha = 0.12f),
                    radius = outerRadius,
                    style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                )

                if (!isSingleSetRep) {
                    // Inner Track
                    drawCircle(
                        color = baseColor.copy(alpha = 0.08f),
                        radius = innerRadius,
                        style = Stroke(width = strokeInner, cap = StrokeCap.Round)
                    )
                }

                // Outer Ring (Completed Sets)
                val outerRect = androidx.compose.ui.geometry.Rect(
                    Offset(center.x - outerRadius, center.y - outerRadius),
                    androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                )
                drawArc(
                    color = outerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedOuterProgress,
                    useCenter = false,
                    topLeft = outerRect.topLeft,
                    size = outerRect.size,
                    style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
                )

                // Inner Ring (Current Set Reps)
                if (!isSingleSetRep) {
                    val innerRect = androidx.compose.ui.geometry.Rect(
                        Offset(center.x - innerRadius, center.y - innerRadius),
                        androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2)
                    )
                    drawArc(
                        color = innerColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedInnerProgress,
                        useCenter = false,
                        topLeft = innerRect.topLeft,
                        size = innerRect.size,
                        style = Stroke(width = strokeInner, cap = StrokeCap.Round)
                    )
                }
            }

            // Round Center Element
            Surface(
                shape = CircleShape,
                color = baseColor.copy(alpha = 0.14f),
                border = BorderStroke(1.5.dp, baseColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(105.dp)
                    .then(if (onRecordTap != null) Modifier.clickable { onRecordTap() } else Modifier)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Text(
                        text = if (actualInt == progressActual.toInt()) "$actualInt" else "%.1f".format(Locale.US, progressActual),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "of ${target.toInt()} $unit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    if (!isSingleSetRep) {
                        Text(
                            text = "Set ${minOf(completedSets + 1, safeSets)} / $safeSets",
                            style = MaterialTheme.typography.labelSmall,
                            color = baseColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// BMI Analytics Screen (Requirement 12)
// Dedicated Bright Orange theme, Graph filters for BMI, Weight, and Height,
// Historical measurements line chart over actual dates, details card, and update dialog.
// -----------------------------------------------------------------------------
@Composable
fun BmiAnalyticsScreen(
    habit: HabitEntity,
    profile: ProfileEntity?,
    onBack: () -> Unit
) {
    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<BmiMeasurement?>(null) }

    val orangeColor = Color(0xFFFF6D00)
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    val measurements = remember(profile?.bodyMeasurements, profile?.updatedAt) {
        val list = mutableListOf<BmiMeasurement>()
        val jsonStr = profile?.bodyMeasurements
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val date = obj.optLong("date", System.currentTimeMillis())
                    val w = obj.optDouble("weightKg", 0.0)
                    val h = obj.optDouble("heightCm", 0.0)
                    val b = obj.optDouble("bmi", 0.0)
                    if (w > 0 && h > 0 && b > 0) {
                        list.add(BmiMeasurement(date, w, h, b))
                    }
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
        if (list.isEmpty() && profile != null && (profile.weightKg ?: 0.0) > 0 && (profile.heightCm ?: 0.0) > 0) {
            val w = profile.weightKg!!
            val h = profile.heightCm!!
            val b = w / ((h / 100.0) * (h / 100.0))
            list.add(BmiMeasurement(profile.createdAt, w, h, b))
        }
        list.sortedBy { it.date }
    }

    val latest = measurements.lastOrNull()

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BMI TRACKING",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = orangeColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "HEALTH & BODY METRICS (0 POINTS)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Current BMI Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = orangeColor.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, orangeColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (latest != null) {
                            val category = when {
                                latest.bmi < 18.5 -> "Underweight"
                                latest.bmi < 25.0 -> "Normal Weight"
                                latest.bmi < 30.0 -> "Overweight"
                                else -> "Obese"
                            }
                            val formattedDate = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(latest.date))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "BMI ${"%.1f".format(Locale.getDefault(), latest.bmi)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = orangeColor
                                    )
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                                Surface(
                                    color = orangeColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${latest.weightKg} kg  •  ${latest.heightCm.toInt()} cm",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Last updated: $formattedDate",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = "No BMI measurements recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }

                        Button(
                            onClick = { showUpdateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = orangeColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text("Update Weight & Height", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Line Chart for BMI Historical Measurements (Plotting BMI over time)
            item {
                Text(
                    text = "HISTORICAL BMI GRAPH",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = textColor.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
            }

            item {
                val chartPoints = measurements.map { m ->
                    val dateLabel = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(m.date))
                    val dateObj = Instant.ofEpochMilli(m.date).atZone(ZoneId.systemDefault()).toLocalDate()

                    LinePoint(
                        label = dateLabel,
                        score = (m.bmi * 10).toInt(),
                        actual = m.bmi,
                        target = 25.0,
                        completed = true,
                        date = dateObj,
                        isActive = true
                    )
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, orangeColor.copy(alpha = 0.2f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    if (chartPoints.isNotEmpty()) {
                        AnalyticsLineChart(
                            points = chartPoints,
                            modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp),
                            color = orangeColor,
                            onPointTap = { point ->
                                selectedPoint = measurements.firstOrNull {
                                    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(it.date)) == point.label
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No BMI measurements recorded yet", color = textColor.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Selected Measurement Point Details Card
            if (selectedPoint != null) item {
                val p = selectedPoint!!
                val formattedDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(p.date))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, orangeColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(formattedDate, fontWeight = FontWeight.Black, color = orangeColor)
                        Text("BMI: ${"%.1f".format(Locale.getDefault(), p.bmi)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Weight: ${p.weightKg} kg  •  Height: ${p.heightCm.toInt()} cm", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.8f))
                    }
                }
            }

            // Measurement History Log
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "HISTORICAL MEASUREMENTS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = textColor.copy(alpha = 0.5f),
                        letterSpacing = 2.sp
                    )

                    measurements.reversed().forEach { m ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)),
                            border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(m.date)),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text(
                                        text = "${m.weightKg} kg  •  ${m.heightCm.toInt()} cm",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                }
                                Surface(
                                    color = orangeColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "BMI ${"%.1f".format(Locale.getDefault(), m.bmi)}",
                                        color = orangeColor,
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUpdateDialog) {
        BmiMeasurementDialog(onDismiss = { showUpdateDialog = false })
    }
}
