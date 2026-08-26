package com.example.ring_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: MainViewModel) {
    val habits by viewModel.allHabits.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Insights", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("ELITE", fontSize = 10.sp, color = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text("${profile?.elitePoints ?: 0}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "0%", "Today")
                    StatCard(Modifier.weight(1f), "12", "Best streak")
                    StatCard(Modifier.weight(1f), "0", "Total done")
                }
            }

            item {
                TimeFilterRow()
            }

            item {
                ChartSection("Overall growth", "Days of month")
            }

            item {
                ChartSection("Category breakdown", "Days of month")
            }

            item {
                TaskSummarySection(tasks.size, tasks.count { it.isCompleted }, tasks.count { !it.isCompleted })
            }
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
fun TimeFilterRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = true, onClick = {}, label = { Text("Days") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00E676)))
        FilterChip(selected = false, onClick = {}, label = { Text("Weeks") })
        FilterChip(selected = false, onClick = {}, label = { Text("Months") })
        FilterChip(selected = false, onClick = {}, label = { Text("Year") })
    }
}

@Composable
fun ChartSection(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(250.dp)
        ) {
            // Chart placeholder
            Box(contentAlignment = Alignment.Center) {
                Text("Chart Visualization", color = Color.Gray)
            }
        }
    }
}

@Composable
fun TaskSummarySection(total: Int, done: Int, pending: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Task summary", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TaskSummaryRow("Total tasks", "$total")
                TaskSummaryRow("Completed", "$done")
                TaskSummaryRow("Pending", "$pending")
                val rate = if (total > 0) (done * 100 / total) else 0
                TaskSummaryRow("Completion rate", "$rate%")
            }
        }
    }
}

@Composable
fun TaskSummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
