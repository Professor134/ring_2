package com.example.ringapp.ui.analytics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private fun dayStart(date: LocalDate) = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

@Composable
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

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(1000)) + slideInVertically(initialOffsetY = { 50 })
        ) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Insights",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "ELITE  ${state.points}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
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

    @Composable
    fun PersonalAnalyticsScreen(
        onBack: () -> Unit = {},
        onEdit: (Long) -> Unit = {},
        viewModel: PersonalAnalyticsViewModel = hiltViewModel()
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        var range by remember { mutableStateOf("Days") }
        var selected by remember { mutableStateOf<LinePoint?>(null) }
        var confirmDelete by remember { mutableStateOf(false) }
        val habit = state.habit
        if (state.loading || habit == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    state.error ?: "Loading neural data...", Modifier.padding(24.dp)
                )
            }; return
        }

        val isSteps = habit.isStepsHabit()
        val today = LocalDate.now()

        data class PeriodPoint(
            val label: String,
            val actual: Double,
            val completed: Boolean,
            val date: LocalDate? = null
        )

        val periodPoints: List<PeriodPoint> = when (range) {
            "Weeks" -> {
                (6 downTo 0).map { w ->
                    val endDate = today.minusDays((w * 7).toLong())
                    val startDate = endDate.minusDays(6)
                    var totalActual = 0.0
                    var completedDays = 0
                    var current = startDate
                    while (!current.isAfter(endDate)) {
                        val rec = state.progress.firstOrNull { it.date == dayStart(current) }
                        if (rec != null) {
                            totalActual += rec.actual
                            if (rec.completed) completedDays++
                        }
                        current = current.plusDays(1)
                    }
                    val avgActual = totalActual / 7.0
                    PeriodPoint(
                        label = startDate.format(DateTimeFormatter.ofPattern("d MMM")),
                        actual = avgActual,
                        completed = completedDays >= 4,
                        date = endDate
                    )
                }
            }

            "Months" -> {
                (6 downTo 0).map { m ->
                    val monthDate = today.minusMonths(m.toLong())
                    val startOfMonth = monthDate.withDayOfMonth(1)
                    val endOfMonth =
                        if (m == 0) today else monthDate.withDayOfMonth(monthDate.lengthOfMonth())
                    var totalActual = 0.0
                    var daysCount = 0
                    var current = startOfMonth
                    while (!current.isAfter(endOfMonth)) {
                        val rec = state.progress.firstOrNull { it.date == dayStart(current) }
                        if (rec != null) totalActual += rec.actual
                        daysCount++
                        current = current.plusDays(1)
                    }
                    val avgActual = if (daysCount > 0) totalActual / daysCount else 0.0
                    PeriodPoint(
                        label = monthDate.format(DateTimeFormatter.ofPattern("MMM")),
                        actual = avgActual,
                        completed = avgActual > 0,
                        date = endOfMonth
                    )
                }
            }

            "Year" -> {
                (11 downTo 0).map { m ->
                    val monthDate = today.minusMonths(m.toLong())
                    val startOfMonth = monthDate.withDayOfMonth(1)
                    val endOfMonth =
                        if (m == 0) today else monthDate.withDayOfMonth(monthDate.lengthOfMonth())
                    var totalActual = 0.0
                    var daysCount = 0
                    var current = startOfMonth
                    while (!current.isAfter(endOfMonth)) {
                        val rec = state.progress.firstOrNull { it.date == dayStart(current) }
                        if (rec != null) totalActual += rec.actual
                        daysCount++
                        current = current.plusDays(1)
                    }
                    val avgActual = if (daysCount > 0) totalActual / daysCount else 0.0
                    PeriodPoint(
                        label = monthDate.format(DateTimeFormatter.ofPattern("MMM")),
                        actual = avgActual,
                        completed = avgActual > 0,
                        date = endOfMonth
                    )
                }
            }

            else -> { // Days: past 11 days
                (10 downTo 0).map { d ->
                    val date = today.minusDays(d.toLong())
                    val rec = state.progress.firstOrNull { it.date == dayStart(date) }
                    val actual = rec?.actual ?: 0.0
                    PeriodPoint(
                        label = date.format(dateFormatter),
                        actual = actual,
                        completed = rec?.completed == true,
                        date = date
                    )
                }
            }
        }

        val maxActual = periodPoints.maxOfOrNull { it.actual } ?: 0.0
        val dynamicCeiling = when {
            maxActual <= 1.0 -> 1.0
            maxActual <= 10.0 -> 10.0
            maxActual <= 25.0 -> 25.0
            maxActual <= 50.0 -> 50.0
            maxActual <= 100.0 -> 100.0
            maxActual <= 250.0 -> 250.0
            maxActual <= 500.0 -> 500.0
            maxActual <= 1000.0 -> 1000.0
            maxActual <= 2500.0 -> 2500.0
            maxActual <= 5000.0 -> 5000.0
            maxActual <= 7500.0 -> 7500.0
            maxActual <= 10000.0 -> 10000.0
            else -> (kotlin.math.ceil(maxActual / 2500.0) * 2500.0).coerceAtLeast(10000.0)
        }

        val points = periodPoints.map { p ->
            val score = (p.actual * 100 / dynamicCeiling).toInt().coerceIn(0, 100)
            LinePoint(
                p.label,
                score,
                p.actual,
                dynamicCeiling,
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
                                state.points.toString(),
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
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, habitColor.copy(alpha = 0.3f))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(point.label, fontWeight = FontWeight.Black, color = habitColor)
                                if (isSteps) {
                                    val valueText =
                                        if (range == "Days") "${point.actual.toInt()} steps" else "${point.actual.toInt()} avg steps/day"
                                    Text(valueText, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Scale: 0 - ${point.target.toInt()} steps",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                } else {
                                    val valFormatted = if (point.actual == point.actual.toInt()
                                            .toDouble()
                                    ) point.actual.toInt().toString() else String.format(
                                        Locale.US,
                                        "%.1f",
                                        point.actual
                                    )
                                    val unitStr = habit.unit.orEmpty()
                                    val valueText =
                                        if (range == "Days") "$valFormatted $unitStr" else "$valFormatted avg $unitStr/day"
                                    Text(valueText, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Scale: 0 - ${point.target.toInt()} $unitStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        "${point.score}%  ${if (point.completed) "Complete" else if (point.actual > 0) "Partial" else "Missed"}",
                                        color = if (point.completed) CyberNeonGreen else Color.Red
                                    )
                                }
                                if (!record?.note.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Log: ${record.note}",
                                        style = MaterialTheme.typography.bodySmall
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

    @Composable
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

    @Composable
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

    @Composable
    private fun HabitCalendar(
        habit: HabitEntity,
        progress: List<HabitProgressEntity>,
        textColor: Color,
        onDateClick: (LocalDate) -> Unit
    ) {
        var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

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
                            Icons.Default.ArrowBack,
                            null,
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
                            Icons.Default.ArrowBack,
                            null,
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
                            val isSteps = habit.isStepsHabit()
                            val color = when {
                                date.isAfter(LocalDate.now()) -> Color.Gray.copy(alpha = 0.1f)
                                record != null && record.actual > 0 -> if (isSteps) Color(
                                    HabitEntity.PLATINUM_COLOR
                                ) else MaterialTheme.colorScheme.primary

                                else -> Color.Transparent
                            }
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(color)
                                    .clickable { onDateClick(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (color == Color.Transparent) textColor else if (isSteps) Color.Black else Color.White
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

    @Composable
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

    @Composable
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

    @Composable
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

    data class LinePoint(
        val label: String,
        val score: Int,
        val actual: Double = 0.0,
        val target: Double = 0.0,
        val completed: Boolean = false,
        val date: LocalDate? = null,
        val isActive: Boolean = true
    )

    @Composable
    private fun AnalyticsLineChart(
        points: List<LinePoint>,
        modifier: Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        onPointTap: (LinePoint) -> Unit = {}
    ) {
        if (points.isEmpty() || points.all { it.score == 0 }) {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    "Create Habit",
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
                animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
            )
        }
        val progress = animatable.value

        val primaryColor = color
        Canvas(modifier.pointerInput(points) {
            detectTapGestures { position ->
                if (points.isNotEmpty()) {
                    val index = ((position.x / size.width) * points.size).toInt()
                        .coerceIn(0, points.lastIndex)
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
                drawLine(
                    Color.LightGray.copy(alpha = .1f),
                    Offset(left, y),
                    Offset(size.width, y),
                    1f
                )
            }

            val clipRight = left + width * progress

            clipRect(left = 0f, top = 0f, right = clipRight, bottom = size.height) {
                val path = Path()
                points.forEachIndexed { index, point ->
                    val x =
                        if (points.size > 1) left + width * index.toFloat() / points.lastIndex else left + width / 2
                    val y = bottom - (bottom - top) * point.score / 100f
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(primaryColor, 10f, Offset(x, y))
                }
                if (points.size > 1) {
                    drawPath(
                        path,
                        primaryColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(6f)
                    )
                    drawPath(
                        path,
                        primaryColor.copy(alpha = 0.25f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(14f)
                    )
                }
            }
        }
    }

    @Composable
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

    @Composable
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
