package com.example.ringapp.ui.habits

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.domain.usecase.HabitDetailData
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
    val average = data.progress.map { it.percentage }.average().toInt().coerceIn(0, 100)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 20.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onEdit(habit.id) }) { Text("Edit") }
                    OutlinedButton(onClick = onDelete) { Text("Delete") }
                }
            }
            Text(habit.name, style = MaterialTheme.typography.headlineMedium)
            Text("${habit.type.name.replace('_', ' ')}  •  ${habit.scheduleType.name.lowercase().replaceFirstChar { it.uppercase() }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Current streak", "${habit.currentStreak} days", Modifier.weight(1f))
                StatCard("Best streak", "${habit.bestStreak} days", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Completion rate", "$average%", Modifier.weight(1f))
                StatCard("Completions", habit.totalCompletions.toString(), Modifier.weight(1f))
            }
        }
        item { Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text("Complete today") } }
        item {
            Text("Historical progress", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                listOf(7, 30, 90, 365).forEach { days -> FilterChip(selected = range == days, onClick = { onRangeChanged(days) }, label = { Text(if (days == 365) "1Y" else "${days}D") }) }
            }
        }
        item { ProgressChart(data, Modifier.fillMaxWidth().height(180.dp)) }
        item {
            Text("History", style = MaterialTheme.typography.titleLarge)
            data.progress.asReversed().take(14).forEach { progress ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(progress.date)))
                    Text("${progress.actual} / ${progress.target} ${habit.unit.orEmpty()}  ${progress.percentage}%")
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(14.dp)) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleLarge) } }
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