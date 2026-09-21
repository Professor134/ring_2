package com.example.ringapp.ui.habits

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
    private val deleteHabit: DeleteHabitUseCase
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
    fun delete(habitId: Long, onDeleted: () -> Unit) = viewModelScope.launch { deleteHabit(habitId); onDeleted() }
}

data class HabitFields(val name: String, val description: String, val categoryId: Long?, val type: HabitType, val target: Double, val unit: String, val schedule: ScheduleType, val days: List<Int>, val startDate: Long, val color: Int)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(habitId: Long? = null, onSaved: () -> Unit, onBack: () -> Unit, viewModel: AddHabitViewModel = hiltViewModel()) {
    val habits by viewModel.habits.collectAsStateWithLifecycle(); val categories by viewModel.categories.collectAsStateWithLifecycle(); val existing = habits.firstOrNull { it.id == habitId }; val context = LocalContext.current
    val isSteps = existing?.isStepsHabit() ?: false
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }; var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }; var type by remember(existing) { mutableStateOf(existing?.type ?: HabitType.YES_NO) }; var target by remember(existing) { mutableStateOf((existing?.target ?: 1.0).toString()) }; var unit by remember(existing) { mutableStateOf(existing?.unit ?: "Pages") }; var schedule by remember(existing) { mutableStateOf(existing?.scheduleType ?: ScheduleType.DAILY) }; var days by remember(existing) { mutableStateOf(existing?.scheduleDays ?: emptyList()) }; var startDate by remember(existing) { mutableStateOf(existing?.startDate ?: todayTimestamp()) }; var categoryId by remember(existing, categories) { mutableStateOf(existing?.categoryId ?: categories.filter { it.name.lowercase() != "habits" }.firstOrNull()?.id ?: 0L) }; var error by remember { mutableStateOf<String?>(null) }; var confirmDelete by remember { mutableStateOf(false) }
    
    val category = categories.firstOrNull { it.id == categoryId }; val categoryName = if (isSteps) "System" else (category?.name ?: "Personal"); val color = if (isSteps) HabitEntity.PLATINUM_COLOR else CategoryConstants.getColorForCategory(categoryName); val editing = habitId != null; val targetValid = type == HabitType.YES_NO || (target.toDoubleOrNull() ?: 0.0) > 0.0; val scheduleValid = schedule != ScheduleType.WEEKLY || days.isNotEmpty(); val canSave = !isSteps && name.isNotBlank() && targetValid && scheduleValid
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                Text(if (isSteps) "Steps Habit" else if (editing) "Edit Habit" else "Create Habit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = textColor)
                if (!isSteps) TextButton(onClick = { if (canSave) viewModel.save(existing, HabitFields(name, description, categoryId, type, target.toDoubleOrNull() ?: 1.0, unit, schedule, days, startDate, color), onSaved) { error = it } }, enabled = canSave) { Text(if (editing) "Save" else "Create", color = MaterialTheme.colorScheme.primary) }
                else Spacer(Modifier.width(48.dp))
            }
            if (isSteps) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(HabitEntity.PLATINUM_COLOR).copy(alpha = 0.1f))) {
                    Text("This is a special automated habit that tracks your steps. It cannot be edited or deleted.", Modifier.padding(16.dp), color = textColor.copy(alpha = 0.7f))
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            
            OutlinedTextField(name, { name = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Habit Name") }, singleLine = true, enabled = !isSteps, isError = name.isBlank() && error != null, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedLabelColor = MaterialTheme.colorScheme.primary, unfocusedLabelColor = textColor.copy(alpha = 0.7f)))

            FormSection("Description / Note") {
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description / Note") }, minLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
            }
            
            FormSection("Category") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.filter { it.name.lowercase() != "habits" }.forEach { item ->
                            FilterChip(
                                selected = categoryId == item.id,
                                onClick = { categoryId = item.id },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(iconFor(CategoryConstants.getIconForCategory(item.name)), null, Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(item.name)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = Color(CategoryConstants.getColorForCategory(item.name)))
                            )
                        }
                    }
                }
            }
            
            FormSection("Habit Type") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == HabitType.YES_NO, onClick = { type = HabitType.YES_NO }, label = { Text("YES / NO") }, colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary))
                    FilterChip(selected = type == HabitType.MEASURABLE, onClick = { type = HabitType.MEASURABLE }, label = { Text("MEASURABLE") }, colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary))
                }
            }
            
            if (type == HabitType.MEASURABLE) FormSection("Measurable Settings") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(target, { target = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.weight(1f), label = { Text("Target") }, isError = !targetValid, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
                    OutlinedTextField(unit, { unit = it }, Modifier.weight(1f), label = { Text("Unit") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
                }
            }
            
            FormSection("Repeat") {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(ScheduleType.DAILY, ScheduleType.ODD_DAYS, ScheduleType.EVEN_DAYS, ScheduleType.WEEKLY).forEach { option ->
                        FilterChip(selected = schedule == option, onClick = { schedule = option }, label = { Text(scheduleLabel(option)) }, colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary))
                    }
                }
                if (schedule == ScheduleType.WEEKLY) {
                    Text("Select weekdays", fontWeight = FontWeight.Medium, color = textColor)
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val weekdays = listOf(
                            "Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7
                        )
                        weekdays.forEach { (label, dayNum) ->
                            val isSelected = days.contains(dayNum)
                            FilterChip(
                                selected = isSelected,
                                onClick = { days = if (isSelected) days - dayNum else days + dayNum },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
                if (schedule == ScheduleType.CUSTOM) {
                    OutlinedTextField(days.firstOrNull()?.toString() ?: "2", { value -> days = listOf(value.filter(Char::isDigit).toIntOrNull()?.coerceAtLeast(1) ?: 1) }, label = { Text("Repeat every N days") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
                }
            }
            
            FormSection("Start Date") {
                OutlinedButton(onClick = {
                    val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
                    DatePickerDialog(context, { _, year, month, day ->
                        startDate = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(startDate)), color = textColor)
                }
            }
            
            if (!editing) Text("Create Habit (Costs 25 Points)", color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally), fontWeight = FontWeight.Bold)
            else if (existing != null && existing.target != (target.toDoubleOrNull() ?: existing.target)) Text("Change Target (Costs 10 Points)", color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally), fontWeight = FontWeight.Bold)
            
            if (editing && existing != null && !isSteps) {
                Button(onClick = { confirmDelete = true }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(12.dp)) {
                    Text("Delete Habit (Costs 50 Points)", color = Color.White)
                }
            }
            
            Spacer(Modifier.height(72.dp))
        }
    }
    
    if (confirmDelete && existing != null) {
        AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete habit?") }, text = { Text("Delete Habit (Costs 50 Points)", color = Color.Red) }, confirmButton = { TextButton(onClick = { viewModel.delete(existing.id) { onBack() } }) { Text("Delete", color = Color.Red) } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
    }
}

@Composable private fun FormSection(title: String, content: @Composable () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); content() } }
private fun scheduleLabel(type: ScheduleType) = when (type) { ScheduleType.ODD_DAYS -> "Odd Days"; ScheduleType.EVEN_DAYS -> "Even Days"; ScheduleType.WEEKLY -> "Weekly"; ScheduleType.CUSTOM -> "Custom"; else -> "Daily" }
private fun iconFor(value: String): ImageVector = when (value) {
    "fitness" -> Icons.Default.FitnessCenter
    "school" -> Icons.Default.School
    "work" -> Icons.Default.Work
    "book" -> Icons.AutoMirrored.Filled.MenuBook
    "self" -> Icons.Default.SelfImprovement
    "payments" -> Icons.Default.Payments
    "bedtime" -> Icons.Default.Bedtime
    "person" -> Icons.Default.Person
    else -> Icons.Default.Flag
}
private fun todayTimestamp() = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
