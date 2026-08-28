package com.example.ring_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ring_2.data.model.TaskEntity
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.components.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onAddTask: () -> Unit,
    onTaskClick: (Long) -> Unit
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val userProg by viewModel.userProgress.collectAsState()
    val taskStats by viewModel.taskStats.collectAsState()
    val currentFilter by viewModel.taskFilter.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Tasks", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("ELITE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Text("${userProg?.currentPoints ?: 0}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00E676))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = Color(0xFF00E676),
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                TaskStatsRow(taskStats)
            }

            item {
                TaskFiltersRow(currentFilter) { viewModel.setTaskFilter(it) }
            }

            if (tasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No tasks yet", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onAddTask,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+ Add Task", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task.id) },
                        onComplete = { viewModel.completeTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskStatsRow(stats: Triple<Int, Int, Int>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TaskStatCard(Modifier.weight(1f), stats.first.toString(), "Pending", Color.Red)
        TaskStatCard(Modifier.weight(1f), stats.second.toString(), "Completed", Color(0xFF00E676))
        TaskStatCard(Modifier.weight(1f), stats.third.toString(), "Total", Color.White)
    }
}

@Composable
fun TaskStatCard(modifier: Modifier, value: String, label: String, color: Color) {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TaskFiltersRow(current: String, onSelect: (String) -> Unit) {
    val filters = listOf("All", "Pending", "Completed", "High Priority", "Overdue", "Today", "Upcoming")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            FilterChip(
                selected = current == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00E676),
                    selectedLabelColor = Color.Black,
                    containerColor = Color(0xFF1E1E1E),
                    labelColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
