package com.example.ring_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.logic.ScheduleEngine
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.components.HabitCard
import com.example.ring_2.ui.components.NumericInputDialog

import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: MainViewModel,
    onHabitClick: (Long) -> Unit,
    onAddHabit: () -> Unit
) {
    val habits by viewModel.allHabits.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val userProg by viewModel.userProgress.collectAsState()
    val todayProgress by viewModel.todayProgress.collectAsState()
    val recentProgress by viewModel.recentProgress.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showNumericDialogFor by remember { mutableStateOf<HabitEntity?>(null) }

    val today = getMidnightTimestamp(System.currentTimeMillis())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Habits", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ELITE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${userProg?.currentPoints ?: 0}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = Color(0xFF00E676),
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // 2. SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search habits...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF1E1E1E),
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF00E676)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // 3. SPACING
            Spacer(Modifier.height(16.dp))
            
            // 4. CATEGORY FILTER
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip("All", selectedCategory == "All") { selectedCategory = "All" }
                }
                items(categories) { category ->
                    CategoryChip(category.name, selectedCategory == category.name) { selectedCategory = category.name }
                }
                // Add some default ones if empty
                if (categories.isEmpty()) {
                    listOf("Health", "Gym", "Yoga", "Study", "Work").forEach {
                        item { CategoryChip(it, selectedCategory == it) { selectedCategory = it } }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 5. HABIT LIST
            val filteredHabits = habits.filter { habit ->
                val categoryName = categories.find { it.id == habit.categoryId }?.name ?: "Others"
                habit.name.contains(searchQuery, ignoreCase = true) && 
                (selectedCategory == "All" || categoryName == selectedCategory)
            }

            if (filteredHabits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No habits yet", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onAddHabit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Add Habit", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredHabits) { habit ->
                        val habitHistory = recentProgress.filter { it.habitId == habit.id }
                        val todayHabitProgress = todayProgress.find { it.habitId == habit.id }
                        val activeToday = ScheduleEngine.isHabitActiveOnDate(habit.schedule, habit.startDate, today)
                        val categoryName = categories.find { it.id == habit.categoryId }?.name ?: "Others"
                        
                        HabitCard(
                            habit = habit,
                            categoryName = categoryName,
                            todayProgress = todayHabitProgress,
                            history = habitHistory,
                            showHistory = true,
                            isActiveToday = activeToday,
                            onClick = { onHabitClick(habit.id) },
                            onComplete = { 
                                if (habit.type == HabitType.YES_NO) {
                                    val newValue = if (todayHabitProgress?.completed == true) 0.0 else habit.target
                                    viewModel.recordProgress(habit, newValue)
                                } else {
                                    showNumericDialogFor = habit
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    showNumericDialogFor?.let { habit ->
        val habitProgress = todayProgress.find { it.habitId == habit.id }
        NumericInputDialog(
            title = "Today's Progress",
            target = "${habit.target.toInt()} ${habit.unit}",
            initialValue = habitProgress?.actualValue?.toInt()?.toString() ?: "",
            onDismiss = { showNumericDialogFor = null },
            onSave = { value, note ->
                viewModel.recordProgress(habit, value, note)
                showNumericDialogFor = null
            }
        )
    }
}

@Composable
fun CategoryChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(name) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF00E676),
            selectedLabelColor = Color.Black,
            containerColor = Color(0xFF1E1E1E),
            labelColor = Color.Gray
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) Color.Transparent else Color(0xFF333333)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

private fun getMidnightTimestamp(time: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = time
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}
