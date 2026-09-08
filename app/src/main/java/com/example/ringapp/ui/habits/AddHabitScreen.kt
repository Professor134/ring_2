package com.example.ringapp.ui.habits

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    observeHabits: ObserveHabitsUseCase,
    observeCategories: ObserveCategoriesUseCase,
    private val createHabit: CreateHabitUseCase,
    private val updateHabit: UpdateHabitUseCase,
    private val createCategory: CreateCategoryUseCase
) : ViewModel() {
    val habits = observeHabits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun save(habit: HabitEntity?, fields: HabitFields, onSaved: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        try {
            if (habit == null) createHabit(fields.name, fields.description, fields.categoryId, fields.type, fields.target, fields.unit, fields.schedule, fields.days, fields.startDate)
            else updateHabit(habit.copy(name = fields.name, description = fields.description.ifBlank { null }, categoryId = fields.categoryId, type = fields.type, target = fields.target, unit = fields.unit.ifBlank { null }, scheduleType = fields.schedule, scheduleDays = fields.days, startDate = fields.startDate, color = fields.color, updatedAt = System.currentTimeMillis()))
            onSaved()
        } catch (error: Exception) { onError(error.message ?: "Unable to save habit") }
    }
    fun addCategory(name: String, color: Int, onCreated: (Long) -> Unit) = viewModelScope.launch { onCreated(createCategory(name, color)) }
}

data class HabitFields(val name: String, val description: String, val categoryId: Long, val type: HabitType, val target: Int, val unit: String, val schedule: ScheduleType, val days: List<Int>, val startDate: Long, val color: Int)

@Composable
fun AddHabitScreen(habitId: Long? = null, onSaved: () -> Unit, onBack: () -> Unit, viewModel: AddHabitViewModel = hiltViewModel()) {
    val habits by viewModel.habits.collectAsStateWithLifecycle(); val categories by viewModel.categories.collectAsStateWithLifecycle(); val existing = habits.firstOrNull { it.id == habitId }; val context = LocalContext.current
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }; var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }; var type by remember(existing) { mutableStateOf(existing?.type ?: HabitType.YES_NO) }; var target by remember(existing) { mutableStateOf((existing?.target ?: 1).toString()) }; var unit by remember(existing) { mutableStateOf(existing?.unit ?: "Pages") }; var schedule by remember(existing) { mutableStateOf(existing?.scheduleType ?: ScheduleType.DAILY) }; var days by remember(existing) { mutableStateOf(existing?.scheduleDays ?: emptyList()) }; var startDate by remember(existing) { mutableStateOf(existing?.startDate ?: todayTimestamp()) }; var categoryId by remember(existing, categories) { mutableStateOf(existing?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }; var selectedIcon by remember { mutableStateOf("flag") }; var error by remember { mutableStateOf<String?>(null) }; var showIcons by remember { mutableStateOf(false) }; var showCategoryDialog by remember { mutableStateOf(false) }
    val category = categories.firstOrNull { it.id == categoryId }; val color = category?.color ?: 0xFF00A84F.toInt(); val editing = habitId != null; val targetValid = type == HabitType.YES_NO || (target.toIntOrNull() ?: 0) > 0; val scheduleValid = schedule != ScheduleType.WEEKLY || days.isNotEmpty(); val canSave = name.isNotBlank() && targetValid && scheduleValid
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text(if (editing) "Edit Habit" else "Create Habit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); TextButton(onClick = { if (canSave) viewModel.save(existing, HabitFields(name, description, categoryId, type, target.toIntOrNull() ?: 1, unit, schedule, days, startDate, color), onSaved) { error = it } }, enabled = canSave) { Text(if (editing) "Save" else "Create") } }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        FormSection("Habit Name") { OutlinedTextField(name, { name = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Habit Name") }, singleLine = true, isError = name.isBlank() && error != null) }
        FormSection("Description / Note") { OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description / Note") }, minLines = 3) }
        FormSection("Icon") { OutlinedButton(onClick = { showIcons = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(iconFor(selectedIcon), null); Spacer(Modifier.width(10.dp)); Text("Choose icon") } }
        FormSection("Category") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { categories.take(4).forEach { item -> FilterChip(categoryId == item.id, { categoryId = item.id }, label = { Text(item.name) }) }; OutlinedButton(onClick = { showCategoryDialog = true }) { Text("add+") } } }
        FormSection("Habit Type") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(type == HabitType.YES_NO, { type = HabitType.YES_NO }, label = { Text("YES / NO") }); FilterChip(type == HabitType.MEASURABLE, { type = HabitType.MEASURABLE }, label = { Text("MEASURABLE") }) } }
        if (type == HabitType.MEASURABLE) FormSection("Measurable Settings") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(target, { target = it.filter(Char::isDigit) }, Modifier.weight(1f), label = { Text("Target") }, isError = !targetValid, singleLine = true); OutlinedTextField(unit, { unit = it }, Modifier.weight(1f), label = { Text("Unit") }, singleLine = true) }; Text("Pages  Steps  Minutes  Hours  Litres  Glasses  Reps  Km  Items  Custom", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        FormSection("Repeat") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf(ScheduleType.DAILY, ScheduleType.ODD_DAYS, ScheduleType.EVEN_DAYS, ScheduleType.WEEKLY, ScheduleType.CUSTOM).forEach { option -> FilterChip(schedule == option, { schedule = option }, label = { Text(scheduleLabel(option)) }) } }; if (schedule == ScheduleType.WEEKLY) { Text("Select weekdays", fontWeight = FontWeight.Medium); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEachIndexed { index, label -> FilterChip(days.contains(index + 1), { days = if (days.contains(index + 1)) days - (index + 1) else days + (index + 1) }, label = { Text(label.take(1)) }) } } }; if (schedule == ScheduleType.CUSTOM) { OutlinedTextField(days.firstOrNull()?.toString() ?: "2", { value -> days = listOf(value.filter(Char::isDigit).toIntOrNull()?.coerceAtLeast(1) ?: 1) }, label = { Text("Repeat every N days") }, singleLine = true) } }
        FormSection("Start Date") { OutlinedButton(onClick = { val calendar = Calendar.getInstance().apply { timeInMillis = startDate }; DatePickerDialog(context, { _, year, month, day -> startDate = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show() }, modifier = Modifier.fillMaxWidth()) { Text(SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(startDate))) } }
        if (!editing) Text("Creating this habit costs 25 Elite Points.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.CenterHorizontally)) else if (existing != null && existing.target != (target.toIntOrNull() ?: existing.target)) Text("Changing the target costs 10 Elite Points.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(72.dp))
    }
    if (showIcons) AlertDialog(onDismissRequest = { showIcons = false }, title = { Text("Choose Icon") }, text = { Column { listOf("flag", "fitness", "school", "work", "book", "self").chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { row.forEach { value -> IconButton(onClick = { selectedIcon = value; showIcons = false }) { Icon(iconFor(value), value) } } } } } }, confirmButton = {})
    if (showCategoryDialog) { var newName by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = { showCategoryDialog = false }, title = { Text("Add Category") }, text = { OutlinedTextField(newName, { newName = it }, label = { Text("Category name") }, singleLine = true) }, confirmButton = { TextButton(enabled = newName.isNotBlank(), onClick = { viewModel.addCategory(newName, 0xFF00A84F.toInt()) { categoryId = it; showCategoryDialog = false } }) { Text("Add") } }, dismissButton = { TextButton(onClick = { showCategoryDialog = false }) { Text("Cancel") } }) }
}

@Composable private fun FormSection(title: String, content: @Composable () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); content() } }
private fun scheduleLabel(type: ScheduleType) = when (type) { ScheduleType.ODD_DAYS -> "Odd Days"; ScheduleType.EVEN_DAYS -> "Even Days"; ScheduleType.WEEKLY -> "Weekly"; ScheduleType.CUSTOM -> "Custom"; else -> "Daily" }
private fun iconFor(value: String): ImageVector = when (value) { "fitness" -> Icons.Default.FitnessCenter; "school" -> Icons.Default.School; "work" -> Icons.Default.Work; "book" -> Icons.Default.MenuBook; "self" -> Icons.Default.SelfImprovement; else -> Icons.Default.Flag }
private fun todayTimestamp() = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
