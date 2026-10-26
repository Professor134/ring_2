package com.example.ringapp.ui.habits

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.domain.engine.ScheduleEngine
import com.example.ringapp.domain.usecase.HabitDetailData
import java.time.LocalDate
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun HabitDetailScreen(onBack: () -> Unit, onEdit: (Long) -> Unit = {}, viewModel: HabitDetailViewModel = hiltViewModel()) {
    val data by viewModel.state.collectAsStateWithLifecycle()
    val range by viewModel.selectedRange.collectAsStateWithLifecycle()
    if (data == null) return
    HabitDetailContent(data!!, range, viewModel::setRange, viewModel::completeToday, viewModel::delete, onBack, onEdit)
}

@Composable
private fun HabitDetailContent(
    data: HabitDetailData,
    range: Int,
    onRangeChanged: (Int) -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val habit = data.habit
    val activeDays = ScheduleEngine.countActiveDays(habit, LocalDate.now().minusDays(range.toLong()), LocalDate.now())
    val completedCount = data.progress.count { it.completed }
    val average = if (activeDays > 0) (completedCount * 100 / activeDays).coerceIn(0, 100) else 0
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 48.dp, bottom = 32.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium, color = textColor) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onEdit(habit.id) }) { Text("Edit", color = textColor) }
                        OutlinedButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    }
                }
                Text(habit.name, style = MaterialTheme.typography.headlineMedium, color = textColor)
                Text("${habit.type.name.replace('_', ' ')}  •  ${habit.scheduleType.name.lowercase().replaceFirstChar { it.uppercase() }}", color = textColor.copy(alpha = 0.6f))
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Current streak", "${habit.currentStreak} days", Modifier.weight(1f), textColor)
                    StatCard("Best streak", "${habit.bestStreak} days", Modifier.weight(1f), textColor)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Completion rate", "$average%", Modifier.weight(1f), textColor)
                    StatCard("Completions", habit.totalCompletions.toString(), Modifier.weight(1f), textColor)
                }
            }
            item { Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text("Complete today") } }
            item {
                Text("Historical progress", style = MaterialTheme.typography.titleLarge, color = textColor)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    listOf(7, 30, 90, 365).forEach { days -> FilterChip(selected = range == days, onClick = { onRangeChanged(days) }, label = { Text(if (days == 365) "1Y" else "${days}D") }) }
                }
            }
            item { ProgressChart(data, Modifier.fillMaxWidth().height(180.dp)) }
            item {
            Text("History (Active Days Only)", style = MaterialTheme.typography.titleLarge, color = textColor)
            val today = LocalDate.now()
            val historyItems = mutableListOf<Pair<LocalDate, HabitProgressEntity?>>()
            var found = 0
            var offset = 0
            while (found < 14 && offset < 90) {
                val date = today.minusDays(offset.toLong())
                if (ScheduleEngine.isActiveOnDate(habit, date)) {
                    val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val record = data.progress.firstOrNull { it.date == timestamp }
                    historyItems.add(date to record)
                    found++
                }
                offset++
            }

            historyItems.forEach { (date, progress) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())), color = textColor)
                    if (progress != null) {
                        if (habit.type == com.example.ringapp.data.local.entities.HabitType.MEASURABLE) {
                            Text("${progress.actual} / ${progress.target} ${habit.unit.orEmpty()}", color = textColor, fontWeight = FontWeight.Bold)
                        } else {
                            Text(if (progress.completed) "✓" else "✗", color = if (progress.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold)
                        }
                    } else {
                        Text("✗", color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), fontWeight = FontWeight.ExtraBold)
                    }
                }
                HorizontalDivider()
            }
        }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, textColor: Color) {
    Card(modifier) { Column(Modifier.padding(14.dp)) { Text(label, color = textColor.copy(alpha = 0.6f)); Text(value, style = MaterialTheme.typography.titleLarge, color = textColor) } }
}

@Composable
private fun ProgressChart(data: HabitDetailData, modifier: Modifier = Modifier) {
    val values = data.progress.map { it.percentage.coerceIn(0, 100).toDouble() }.ifEmpty { listOf(0.0) }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(values) { modelProducer.runTransaction { lineSeries { series(values) } } }
    CartesianChartHost(
        chart = rememberCartesianChart(rememberLineCartesianLayer()),
        modelProducer = modelProducer,
        modifier = modifier.padding(vertical = 12.dp)
    )
}