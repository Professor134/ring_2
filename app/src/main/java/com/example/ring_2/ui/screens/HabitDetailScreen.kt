package com.example.ring_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(viewModel: MainViewModel, habitId: Long, onBack: () -> Unit) {
    val habits by viewModel.allHabits.collectAsState()
    val habit = habits.find { it.id == habitId } ?: return

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Habit Analytics", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00E676))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(habit.color).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+", color = Color(habit.color), fontSize = 32.sp)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(habit.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Health • 🔥 ${habit.currentStreak} day streak • daily", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailStatCard(Modifier.weight(1f), "${habit.currentStreak}", "Current streak")
                    DetailStatCard(Modifier.weight(1f), "${habit.bestStreak}", "Best streak")
                }
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailStatCard(Modifier.weight(1f), "${habit.totalCompletions}", "Completions")
                    DetailStatCard(Modifier.weight(1f), "83%", "Success rate")
                }
            }

            item {
                ChartSection("Progress trend", "Completion %")
            }

            item {
                TargetsSection(habit)
            }
        }
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
fun TargetsSection(habit: com.example.ring_2.data.model.Habit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Targets", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TargetRow("Daily target", "1")
                TargetRow("Weekly target", "7")
                TargetRow("Monthly target", "30")
                TargetRow("Yearly target", "365")
            }
        }
    }
}

@Composable
fun TargetRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, color = Color.White)
    }
}
