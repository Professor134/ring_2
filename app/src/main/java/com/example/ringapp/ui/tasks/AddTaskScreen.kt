package com.example.ringapp.ui.tasks

import android.Manifest
import android.app.*
import android.content.*
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.usecase.*
import com.example.ringapp.work.ReminderAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val observeProgress: ObserveUserProgressUseCase,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val deleteTask: DeleteTaskUseCase
) : ViewModel() {
    val tasks = observeTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val points = observeProgress().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun save(existing: TaskEntity?, task: TaskEntity, onSaved: (TaskEntity) -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        try {
            val saved = if (existing == null) {
                val current = (points.value ?: observeProgress().first())?.currentPoints ?: 0
                if (current < 1) throw IllegalStateException("Not enough Elite Points.")
                task.copy(id = createTask(task.title, task.description.orEmpty(), task.priority, task.dueDate, task.dueTime, task.repeatType, task.reminderEnabled, task.repeatInterval, task.repeatDaysOfWeek, task.repeatDayOfMonth, task.repeatMonth))
            } else {
                updateTask(task.copy(id = existing.id))
                task.copy(id = existing.id)
            }
            onSaved(saved)
        } catch (e: Exception) { onError(e.message ?: "Unable to save task") }
    }
    fun delete(task: TaskEntity, onDeleted: () -> Unit) = viewModelScope.launch { deleteTask(task.id); onDeleted() }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(taskId: Long? = null, onSaved: () -> Unit, onBack: () -> Unit, viewModel: AddTaskViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(); val existing = tasks.firstOrNull { it.id == taskId }; val context = LocalContext.current; val editing = taskId != null
    var title by remember(existing) { mutableStateOf(existing?.title.orEmpty()) }; var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }; var priority by remember(existing) { mutableStateOf(existing?.priority ?: TaskPriority.MEDIUM) }; var repeat by remember(existing) { mutableStateOf(existing?.repeatType ?: RepeatType.NONE) }; var repeatInterval by remember(existing) { mutableStateOf((existing?.repeatInterval ?: 1).toString()) }
    var repeatDaysOfWeek by remember(existing) { mutableStateOf(existing?.repeatDaysOfWeek?.split(",")?.filter { it.isNotBlank() }?.map { it.toInt() } ?: emptyList<Int>()) }
    var repeatDayOfMonth by remember(existing) { mutableStateOf(existing?.repeatDayOfMonth ?: 1) }
    var repeatMonth by remember(existing) { mutableStateOf(existing?.repeatMonth ?: 1) }
    var reminder by remember(existing) { mutableStateOf(existing?.reminderEnabled ?: false) }; var dueDate by remember(existing) { mutableStateOf(existing?.dueDate) }; var dueTime by remember(existing) { mutableStateOf(existing?.dueTime) }; var error by remember { mutableStateOf<String?>(null) }; var confirmDelete by remember { mutableStateOf(false) }
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (!granted) reminder = false }

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                Text(if (editing) "Edit Task" else "Add Task", style = MaterialTheme.typography.headlineSmall, color = textColor)
                TextButton(onClick = {
                    if (title.isBlank()) error = "Task title is required"
                    else {
                        val task = TaskEntity(
                            id = existing?.id ?: 0,
                            title = title,
                            description = description.ifBlank { null },
                            priority = priority,
                            dueDate = if (repeat == RepeatType.NONE) dueDate else null,
                            dueTime = if (repeat == RepeatType.NONE) dueTime else null,
                            repeatType = repeat,
                            repeatInterval = repeatInterval.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                            repeatDaysOfWeek = if (repeat == RepeatType.WEEKLY) repeatDaysOfWeek.joinToString(",") else null,
                            repeatDayOfMonth = if (repeat == RepeatType.MONTHLY || repeat == RepeatType.YEARLY) repeatDayOfMonth else null,
                            repeatMonth = if (repeat == RepeatType.YEARLY) repeatMonth else null,
                            reminderEnabled = reminder,
                            reminderTime = if (reminder) reminderTime(dueDate, dueTime) else null,
                            completed = existing?.completed ?: false,
                            completedAt = existing?.completedAt,
                            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.save(existing, task, { saved ->
                            if (saved.reminderEnabled) scheduleReminder(context, saved) else cancelReminder(context, saved.id)
                            onSaved()
                        }, { error = it })
                    }
                }) { Text("Save", color = MaterialTheme.colorScheme.primary) }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedTextField(value = title, onValueChange = { title = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Task") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
            FormSection("Description") { OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Description") }, minLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)) }
            
            FormSection("Priority") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskPriority.values().forEach { option ->
                        FilterChip(selected = priority == option, onClick = { priority = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White, labelColor = textColor))
                    }
                }
            }

            if (repeat == RepeatType.NONE) {
                FormSection("Due Date") {
                    OutlinedButton(onClick = {
                        val now = Calendar.getInstance()
                        DatePickerDialog(context, { _, year, month, day ->
                            dueDate = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                    }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(dueDate?.let { SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Choose date", color = textColor)
                    }
                }
                FormSection("Due Time") {
                    OutlinedButton(onClick = {
                        val now = Calendar.getInstance()
                        TimePickerDialog(context, { _, hour, minute ->
                            dueTime = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0) }.timeInMillis
                        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
                    }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(dueTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) } ?: "Choose time", color = textColor)
                    }
                }
            }

            FormSection("Reminder") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Reminder", color = textColor)
                    Switch(reminder, { if (it && Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS); reminder = it })
                }
            }

            FormSection("Repeat") {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RepeatType.values().filter { it != RepeatType.DAILY && it != RepeatType.CUSTOM }.forEach { option ->
                        FilterChip(selected = repeat == option, onClick = { repeat = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White, labelColor = textColor))
                    }
                }
                when (repeat) {
                    RepeatType.WEEKLY -> {
                        Text("Days of week", style = MaterialTheme.typography.labelMedium, color = textColor)
                        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEachIndexed { index, day ->
                                val dayNum = index + 1
                                val isSelected = repeatDaysOfWeek.contains(dayNum)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { repeatDaysOfWeek = if (isSelected) repeatDaysOfWeek - dayNum else repeatDaysOfWeek + dayNum },
                                    label = { Text(day) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White, labelColor = textColor)
                                )
                            }
                        }
                    }
                    RepeatType.MONTHLY -> {
                        var expanded by remember { mutableStateOf(false) }
                        Column {
                            Text("Repeat on day of month:", style = MaterialTheme.typography.labelMedium, color = textColor)
                            Spacer(Modifier.height(8.dp))
                            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                                OutlinedTextField(
                                    value = "Day $repeatDayOfMonth",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                                )
                                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    (1..31).forEach { day ->
                                        DropdownMenuItem(
                                            text = { Text("Day $day") },
                                            onClick = { repeatDayOfMonth = day; expanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    RepeatType.YEARLY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Select Date for Yearly Repeat:", style = MaterialTheme.typography.labelMedium, color = textColor)
                            OutlinedButton(onClick = {
                                val calendar = Calendar.getInstance().apply { 
                                    set(Calendar.MONTH, repeatMonth - 1)
                                    set(Calendar.DAY_OF_MONTH, repeatDayOfMonth)
                                }
                                DatePickerDialog(context, { _, year, month, day ->
                                    repeatMonth = month + 1
                                    repeatDayOfMonth = day
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                            }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text("${monthName(repeatMonth)} $repeatDayOfMonth", color = textColor)
                            }
                            Text("Repeats every year on ${monthName(repeatMonth)} $repeatDayOfMonth", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.6f))
                        }
                    }
                    else -> {}
                }
            }
            if (!editing) Text("Create Task (Costs 1 Point)", color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally), fontWeight = FontWeight.Bold)
            if (editing && existing != null) {
                Button(onClick = { confirmDelete = true }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Delete Task (Costs 5 Points)", color = Color.White)
                }
            }
            Spacer(Modifier.height(72.dp))
        }
    }
    if (confirmDelete && existing != null) {
        AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete task?") }, text = { Text("Delete Task (Costs 5 Points)", color = Color.Red) }, confirmButton = { TextButton(onClick = { cancelReminder(context, existing.id); viewModel.delete(existing) { onBack() } }) { Text("Delete", color = Color.Red) } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
    }
}

@Composable private fun FormSection(title: String, content: @Composable () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); content() } }
private fun monthName(month: Int): String = java.time.Month.of(month).name.lowercase().replaceFirstChar { it.uppercase() }
private fun reminderTime(date: Long?, time: Long?): Long? { if (date == null || time == null) return null; return Calendar.getInstance().apply { timeInMillis = date; val source = Calendar.getInstance().apply { timeInMillis = time }; set(Calendar.HOUR_OF_DAY, source.get(Calendar.HOUR_OF_DAY)); set(Calendar.MINUTE, source.get(Calendar.MINUTE)); set(Calendar.SECOND, 0) }.timeInMillis }
private fun scheduleReminder(context: Context, task: TaskEntity) { val trigger = task.reminderTime ?: return; val manager = context.getSystemService(AlarmManager::class.java); val intent = Intent(context, ReminderAlarmReceiver::class.java).putExtra("taskId", task.id).putExtra("title", task.title); val pending = PendingIntent.getBroadcast(context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending) }
private fun cancelReminder(context: Context, taskId: Long) { val manager = context.getSystemService(AlarmManager::class.java); val pending = PendingIntent.getBroadcast(context, taskId.toInt(), Intent(context, ReminderAlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE); pending?.let { manager.cancel(it); it.cancel() } }
