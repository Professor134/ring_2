package com.example.ring_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ring_2.logic.ScheduleManager
import com.example.ring_2.ui.components.HabitCard
import com.example.ring_2.ui.components.TaskCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: MainViewModel, onAddHabit: () -> Unit, onAddTask: () -> Unit) {
    val habits by viewModel.allHabits.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    
    val today = System.currentTimeMillis()
    val todayHabits = habits.filter { ScheduleManager.isHabitActiveOnDate(it.schedule, it.startDate, today) }
    val pendingTasks = tasks.filter { !it.isCompleted }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(profile?.name ?: "User", profile?.elitePoints ?: 0)
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddHabit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E676))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Habit", color = Color.White)
                    }
                    Button(
                        onClick = onAddTask,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E676))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Task", color = Color.White)
                    }
                }
            }

            item {
                SectionHeader("Today's habits", onSeeAll = {})
            }

            items(todayHabits) { habit ->
                HabitCard(
                    habit = habit,
                    onClick = { /* navigate to detail */ },
                    onComplete = { viewModel.completeHabit(habit) }
                )
            }

            item {
                SectionHeader("Today's tasks", onSeeAll = {})
            }

            items(pendingTasks) { task ->
                TaskCard(task, onComplete = { viewModel.completeTask(task) })
            }
        }
    }
}

@Composable
fun HomeHeader(name: String, points: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val calendar = Calendar.getInstance()
            val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
            Text(
                text = "$greeting, $name",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            Text(
                text = sdf.format(Date()),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(width = 100.dp, height = 60.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("ELITE POINTS", fontSize = 10.sp, color = Color.Gray)
                Text("$points", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
        TextButton(onClick = onSeeAll) {
            Text("See all", color = Color(0xFF00E676))
        }
    }
}
