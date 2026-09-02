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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.logic.ScheduleEngine
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.components.HabitCard
import com.example.ring_2.ui.components.NumericInputDialog
import com.example.ring_2.logic.DateTimeUtils
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
    val allProgress by viewModel.allProgress.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showNumericDialogFor by remember { mutableStateOf<HabitEntity?>(null) }

    val today = DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Habits", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(com.example.ring_2.R.string.label_elite_points).split(" ")[0], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${userProg?.currentPoints ?: 0}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search habits...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
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
            }

            Spacer(Modifier.height(16.dp))

            val filteredHabits = habits.filter { habit ->
                val categoryName = categories.find { it.id == habit.categoryId }?.name ?: "Others"
                habit.name.contains(searchQuery, ignoreCase = true) && 
                (selectedCategory == "All" || categoryName == selectedCategory)
            }

            if (filteredHabits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(com.example.ring_2.R.string.msg_no_habits), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onAddHabit,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(com.example.ring_2.R.string.quick_action_add_habit), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
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
                        val habitHistory = allProgress.filter { it.habitId == habit.id }
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
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
