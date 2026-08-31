package com.example.ring_2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.HabitSchedule
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.logic.RingNotificationManager
import com.example.ring_2.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    viewModel: MainViewModel,
    habitId: Long? = null,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val habits by viewModel.allHabits.collectAsState()
    val userProg by viewModel.userProgress.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    val existingHabit = remember(habitId, habits) { habits.find { it.id == habitId } }

    var name by remember { mutableStateOf(existingHabit?.name ?: "") }
    var description by remember { mutableStateOf(existingHabit?.description ?: "") }
    var selectedType by remember { mutableStateOf(existingHabit?.type ?: HabitType.YES_NO) }
    var targetValue by remember { mutableStateOf(existingHabit?.target?.toInt()?.toString() ?: "1") }
    var unit by remember { mutableStateOf(existingHabit?.unit ?: "Pages") }
    var selectedRepeat by remember { mutableStateOf(existingHabit?.repeatType ?: "Daily") }
    
    // Weekly Selection
    var selectedDays by remember { 
        mutableStateOf(
            (existingHabit?.schedule as? HabitSchedule.Weekly)?.daysOfWeek ?: emptySet()
        ) 
    }
    
    // Monthly Selection
    var dayOfMonth by remember { 
        mutableStateOf(
            (existingHabit?.schedule as? HabitSchedule.Monthly)?.dayOfMonth?.toString() ?: "15"
        ) 
    }
    
    // Yearly Selection
    var yearlyMonth by remember { 
        mutableIntStateOf(
            (existingHabit?.schedule as? HabitSchedule.Yearly)?.month ?: 8
        ) 
    }
    var yearlyDay by remember { 
        mutableStateOf(
            (existingHabit?.schedule as? HabitSchedule.Yearly)?.dayOfMonth?.toString() ?: "15"
        ) 
    }

    var selectedCategoryId by remember { mutableLongStateOf(existingHabit?.categoryId ?: 0L) }
    var selectedColor by remember { mutableIntStateOf(existingHabit?.color ?: 0xFF00E676.toInt()) }
    var startDate by remember { mutableLongStateOf(existingHabit?.startDate ?: System.currentTimeMillis()) }

    // Validation
    val isNameValid = name.isNotBlank()
    val isTargetValid = selectedType == HabitType.YES_NO || ((targetValue.toDoubleOrNull() ?: 0.0) > 0)
    val isWeeklyValid = selectedRepeat != "Weekly" || selectedDays.isNotEmpty()
    val isMonthlyValid = selectedRepeat != "Monthly" || (dayOfMonth.toIntOrNull() in 1..31)
    val isYearlyValid = selectedRepeat != "Yearly" || (yearlyDay.toIntOrNull() in 1..31)
    
    val canSave = isNameValid && isTargetValid && isWeeklyValid && isMonthlyValid && isYearlyValid

    val isEditMode = habitId != null
    val targetChanged = isEditMode && existingHabit?.target != targetValue.toDoubleOrNull()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Habit" else "Create Habit", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (canSave) {
                                val schedule = when(selectedRepeat) {
                                    "Odd Days" -> HabitSchedule.OddDays
                                    "Even Days" -> HabitSchedule.EvenDays
                                    "Weekly" -> HabitSchedule.Weekly(selectedDays)
                                    "Monthly" -> HabitSchedule.Monthly(dayOfMonth.toIntOrNull() ?: 1)
                                    "Yearly" -> HabitSchedule.Yearly(yearlyMonth, yearlyDay.toIntOrNull() ?: 1)
                                    else -> HabitSchedule.Daily
                                }
                                
                                val habit = HabitEntity(
                                    id = habitId ?: 0,
                                    name = name,
                                    description = description,
                                    categoryId = selectedCategoryId,
                                    icon = "default",
                                    type = selectedType,
                                    target = targetValue.toDoubleOrNull() ?: 1.0,
                                    unit = unit,
                                    schedule = schedule,
                                    repeatType = selectedRepeat,
                                    startDate = startDate,
                                    color = selectedColor,
                                    isActive = existingHabit?.isActive ?: true,
                                    currentStreak = existingHabit?.currentStreak ?: 0,
                                    bestStreak = existingHabit?.bestStreak ?: 0,
                                    totalCompletions = existingHabit?.totalCompletions ?: 0
                                )
                                if (isEditMode) {
                                    viewModel.updateHabit(habit)
                                } else {
                                    viewModel.addHabit(habit)
                                }
                                
                                // Schedule reminders for Monthly/Yearly
                                if (selectedRepeat == "Monthly" || selectedRepeat == "Yearly") {
                                    val cal = Calendar.getInstance()
                                    if (selectedRepeat == "Monthly") {
                                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth.toIntOrNull() ?: 15)
                                    } else {
                                        cal.set(Calendar.MONTH, yearlyMonth - 1)
                                        cal.set(Calendar.DAY_OF_MONTH, yearlyDay.toIntOrNull() ?: 15)
                                    }
                                    cal.set(Calendar.HOUR_OF_DAY, 8)
                                    cal.set(Calendar.MINUTE, 0)
                                    
                                    RingNotificationManager.scheduleHabitReminder(
                                        context = context,
                                        habitId = habit.id,
                                        title = "Habit Reminder: ${habit.name}",
                                        timeMillis = cal.timeInMillis
                                    )
                                }
                                
                                onBack()
                            }
                        }, enabled = canSave) {
                        Text(if (isEditMode) "Save" else "Create", color = if (canSave) Color(0xFF00E676) else Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            // 1. HABIT NAME
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("HABIT NAME", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Morning walk", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color(0xFF00E676)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 2. DESCRIPTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DESCRIPTION / NOTE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add a note (optional)", color = Color.Gray) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color(0xFF00E676)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 4. CATEGORY
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CATEGORY", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { 
                                selectedCategoryId = cat.id
                                selectedColor = cat.color
                            },
                            label = { Text(cat.name) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.color).copy(alpha = 0.8f),
                                selectedLabelColor = Color.Black,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }
            }

            // 5. HABIT TYPE
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TYPE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TypeButton(Modifier.weight(1f), "Yes / No", selectedType == HabitType.YES_NO) { selectedType = HabitType.YES_NO }
                    TypeButton(Modifier.weight(1f), "Measurable", selectedType == HabitType.MEASURABLE) { selectedType = HabitType.MEASURABLE }
                }
            }

            // 6. MEASURABLE SETTINGS
            if (selectedType == HabitType.MEASURABLE) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TARGET", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = targetValue,
                            onValueChange = { if (it.all { c -> c.isDigit() }) targetValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("UNIT", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // 7. REPEAT
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("REPEAT", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                val repeatOptions = listOf("Daily", "Odd Days", "Even Days", "Weekly", "Monthly", "Yearly")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(repeatOptions) { opt ->
                        FilterChip(
                            selected = selectedRepeat == opt,
                            onClick = { selectedRepeat = opt },
                            label = { Text(opt) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                
                when (selectedRepeat) {
                    "Weekly" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                            days.forEachIndexed { index, day ->
                                val dayNum = index + 1
                                DayChip(day, selectedDays.contains(dayNum)) {
                                    selectedDays = if (selectedDays.contains(dayNum)) selectedDays - dayNum else selectedDays + dayNum
                                }
                            }
                        }
                    }
                    "Monthly" -> {
                        OutlinedTextField(
                            value = dayOfMonth,
                            onValueChange = { if (it.all { c -> c.isDigit() }) dayOfMonth = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Day of month") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    "Yearly" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Month Selector
                            var expandedMonth by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedMonth,
                                onExpandedChange = { expandedMonth = it },
                                modifier = Modifier.weight(1.5f)
                            ) {
                                OutlinedTextField(
                                    value = SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, yearlyMonth - 1) }.time),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Month") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = Color(0xFF00E676)
                                    )
                                )
                                ExposedDropdownMenu(expanded = expandedMonth, onDismissRequest = { expandedMonth = false }) {
                                    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                                    months.forEachIndexed { index, m ->
                                        DropdownMenuItem(
                                            text = { Text(m) },
                                            onClick = {
                                                yearlyMonth = index + 1
                                                expandedMonth = false
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                        )
                                    }
                                }
                            }
                            // Day Selector
                            OutlinedTextField(
                                value = yearlyDay,
                                onValueChange = { if (it.all { c -> c.isDigit() }) yearlyDay = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Day") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = Color(0xFF00E676)
                                )
                            )
                        }
                    }
                }
            }

            // 12. START DATE
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("START DATE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { /* Show date picker */ }
                ) {
                    Text(
                        text = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(startDate)),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // 13. SAVE COST
            if (!isEditMode) {
                val canAfford = (userProg?.currentPoints ?: 0) >= 25
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (canAfford) "Creating this habit will cost 25 Elite Points." else "Not enough Elite Points (25 required)",
                        color = if (canAfford) Color(0xFF00E676) else Color.Red,
                        fontSize = 14.sp
                    )
                }
            } else if (targetChanged) {
                Text(
                    "Changing target will cost 10 Elite Points.",
                    color = Color(0xFF00E676),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun DayChip(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFF00E676) else MaterialTheme.colorScheme.surface,
        shape = CircleShape,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(day.take(1), color = if (isSelected) Color.Black else Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun TypeButton(modifier: Modifier, text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF00E676) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = if (isSelected) Color.Black else Color.Gray)
    }
}
