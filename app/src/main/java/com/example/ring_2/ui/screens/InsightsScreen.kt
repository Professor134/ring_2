package com.example.ring_2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.components.MultiLineGraph
import com.example.ring_2.ui.components.LineGraph
import com.example.ring_2.logic.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: MainViewModel) {
    val habits by viewModel.allHabits.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val userProg by viewModel.userProgress.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val recentProgress by viewModel.recentProgress.collectAsState()

    var selectedFilter by remember { mutableStateOf("Days") }
    var selectedCategoryId by remember { mutableLongStateOf(-1L) } // -1 for All

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Insights", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("ELITE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Text("${userProg?.currentPoints ?: 0}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        }
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
            // SECTION 1: SUMMARY CARDS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val today = DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis())
                val todayRecs = recentProgress.filter { it.date == today }
                val score = if (todayRecs.isEmpty()) 0 else (todayRecs.sumOf { it.percentage } / todayRecs.size).toInt()
                
                StatCard(Modifier.weight(1f), "$score%", "Today's Score")
                StatCard(Modifier.weight(1f), "${habits.maxOfOrNull { it.bestStreak } ?: 0}", "Best Streak")
                StatCard(Modifier.weight(1f), "${habits.sumOf { it.totalCompletions }}", "Total Done")
            }

            // SECTION 2: TIME FILTER
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

            // SECTION 3: OVERALL GROWTH
            val daysCount = when(selectedFilter) {
                "Days" -> 7
                "Weeks" -> 28
                "Months" -> 90
                "Year" -> 365
                else -> 7
            }

            InsightsChartSection("Overall Growth", if (selectedFilter == "Days") "Recent dates" else selectedFilter) {
                val graphData = prepareGrowthData(recentProgress, daysCount)
                LineGraph(
                    dataPoints = graphData.map { it.second },
                    labels = graphData.map { it.first },
                    color = Color(0xFF00E676),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // SECTION 4: CATEGORY BREAKDOWN
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    
                    var expanded by remember { mutableStateOf(false) }
                    val currentCatName = if (selectedCategoryId == -1L) "All Categories" else categories.find { it.id == selectedCategoryId }?.name ?: "All Categories"
                    
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(currentCatName, color = Color(0xFF00E676), fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("All Categories") }, onClick = { selectedCategoryId = -1L; expanded = false })
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategoryId = cat.id; expanded = false })
                            }
                        }
                    }
                }

                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                ) {
                    val catData = prepareCategoryData(recentProgress, habits, categories, selectedCategoryId, daysCount)
                    MultiLineGraph(
                        data = catData.data,
                        labels = catData.labels,
                        colors = catData.colors,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // SECTION 7: TASK SUMMARY
            TaskSummaryCard(
                total = tasks.size,
                done = tasks.count { it.completed },
                pending = tasks.count { !it.completed }
            )
            
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, value: String, label: String) {
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
fun InsightsChartSection(title: String, subtitle: String, chart: @Composable () -> Unit) {
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

@Composable
fun TaskSummaryCard(total: Int, done: Int, pending: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Task summary", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TaskRow("Total tasks", "$total")
                TaskRow("Completed", "$done")
                TaskRow("Pending", "$pending")
                val rate = if (total > 0) (done * 100 / total) else 0
                TaskRow("Completion rate", "$rate%")
            }
        }
    }
}

@Composable
fun TaskRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

private fun prepareGrowthData(progress: List<com.example.ring_2.data.model.HabitProgressEntity>, days: Int): List<Pair<String, Float>> {
    val result = mutableListOf<Pair<String, Float>>()
    val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
    val cal = Calendar.getInstance()
    
    for (i in (days - 1) downTo 0) {
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val midnight = DateTimeUtils.getMidnightTimestamp(cal.timeInMillis)
        
        val dayRecs = progress.filter { it.date == midnight }
        val score = if (dayRecs.isEmpty()) 0f else (dayRecs.sumOf { it.percentage } / dayRecs.size).toFloat()
        result.add(sdf.format(cal.time) to score)
    }
    return result
}

data class MultiGraphData(
    val data: Map<String, List<Float>>,
    val labels: List<String>,
    val colors: Map<String, Color>
)

private fun prepareCategoryData(
    progress: List<com.example.ring_2.data.model.HabitProgressEntity>,
    habits: List<com.example.ring_2.data.model.HabitEntity>,
    categories: List<com.example.ring_2.data.model.Category>,
    selectedId: Long,
    days: Int
): MultiGraphData {
    val labels = mutableListOf<String>()
    val graphData = mutableMapOf<String, MutableList<Float>>()
    val colors = mutableMapOf<String, Color>()

    val targetCats = if (selectedId == -1L) categories else categories.filter { it.id == selectedId }
    val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
    val cal = Calendar.getInstance()

    for (i in (days - 1) downTo 0) {
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val midnight = DateTimeUtils.getMidnightTimestamp(cal.timeInMillis)
        labels.add(sdf.format(cal.time))

        targetCats.forEach { cat ->
            val catHabitIds = habits.filter { it.categoryId == cat.id }.map { it.id }
            val dayCatRecs = progress.filter { it.date == midnight && it.habitId in catHabitIds }
            val score = if (dayCatRecs.isEmpty()) 0f else (dayCatRecs.sumOf { it.percentage } / dayCatRecs.size).toFloat()
            
            graphData.getOrPut(cat.name) { mutableListOf() }.add(score)
            colors[cat.name] = Color(cat.color)
        }
    }

    return MultiGraphData(graphData, labels, colors)
}
