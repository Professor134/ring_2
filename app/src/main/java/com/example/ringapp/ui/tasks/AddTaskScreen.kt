// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\tasks\AddTaskScreen.kt
// Purpose: Screen for creating and editing tasks arranged in exact required field order.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.tasks

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ringapp.data.local.entities.RepeatType
import com.example.ringapp.data.local.entities.SubtaskItem
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.local.entities.TaskPriority
import com.example.ringapp.domain.usecase.CreateTaskUseCase
import com.example.ringapp.domain.usecase.DeleteTaskUseCase
import com.example.ringapp.domain.usecase.ObserveProgressRangeUseCase
import com.example.ringapp.domain.usecase.ObserveTasksUseCase
import com.example.ringapp.domain.usecase.ObserveUserProgressUseCase
import com.example.ringapp.domain.usecase.UpdateTaskUseCase
import com.example.ringapp.work.ReminderAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
                if (current < 1) throw IllegalStateException("Not enough Elite Points. You need 1 point to create a task.")
                val taskId = createTask(
                    task.title,
                    task.description.orEmpty(),
                    task.priority,
                    task.dueDate,
                    task.dueTime,
                    task.repeatType,
                    task.reminderEnabled,
                    task.repeatInterval,
                    task.repeatDaysOfWeek,
                    task.repeatDayOfMonth,
                    task.repeatMonth,
                    task.subtasks
                )
                task.copy(id = taskId)
            } else {
                updateTask(task.copy(id = existing.id))
                task.copy(id = existing.id)
            }
            onSaved(saved)
        } catch (e: Exception) {
            onError(e.message ?: "Unable to save task")
        }
    }

    fun delete(task: TaskEntity, onDeleted: () -> Unit) = viewModelScope.launch {
        deleteTask(task.id)
        onDeleted()
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    taskId: Long? = null,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddTaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val existing = tasks.firstOrNull { it.id == taskId }
    val context = LocalContext.current
    val editing = taskId != null

    // 1. First: Task Name
    var title by remember(existing) { mutableStateOf(existing?.title.orEmpty()) }

    // 2. Second: Description
    var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }

    // 3. Third: Priority
    var priority by remember(existing) { mutableStateOf(existing?.priority ?: TaskPriority.MEDIUM) }

    // 4. Fourth: Reminder Configuration
    var reminder by remember(existing) { mutableStateOf(existing?.reminderEnabled ?: false) }

    // 5. Fifth: Repeat Selector
    var repeat by remember(existing) { mutableStateOf(existing?.repeatType ?: RepeatType.NONE) }
    var repeatInterval by remember(existing) { mutableStateOf((existing?.repeatInterval ?: 1).toString()) }
    var repeatDaysOfWeek by remember(existing) {
        mutableStateOf(existing?.repeatDaysOfWeek?.split(",")?.filter { it.isNotBlank() }?.map { it.toInt() } ?: emptyList<Int>())
    }
    var repeatDayOfMonth by remember(existing) { mutableStateOf(existing?.repeatDayOfMonth ?: 1) }
    var repeatMonth by remember(existing) { mutableStateOf(existing?.repeatMonth ?: 1) }

    // 6. Sixth: Due Date & Due Time controls
    var dueDate by remember(existing) { mutableStateOf(existing?.dueDate) }
    var dueTime by remember(existing) { mutableStateOf(existing?.dueTime) }

    // Default due date: 7 days after task creation at 20:00 (Requirement 15)
    val defaultDueDateMillis = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // 7. Seventh: Subtasks & Checklist
    var subtasks by remember(existing) { mutableStateOf(existing?.subtasks ?: emptyList()) }
    var newSubtaskTitle by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) reminder = false
    }

    fun performSave() {
        if (title.isBlank()) {
            error = "Please enter a task name."
            return
        }

        // Apply default due date & time behavior if not provided
        val finalDueDate = dueDate ?: defaultDueDateMillis
        val finalDueTime = dueTime ?: defaultDueDateMillis

        val finalReminderTime = if (reminder) reminderTime(finalDueDate, finalDueTime) else null

        val task = TaskEntity(
            id = existing?.id ?: 0,
            title = title.trim(),
            description = description.trim().ifBlank { null },
            priority = priority,
            dueDate = finalDueDate,
            dueTime = finalDueTime,
            repeatType = repeat,
            repeatInterval = repeatInterval.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            repeatDaysOfWeek = if (repeat == RepeatType.WEEKLY) repeatDaysOfWeek.joinToString(",") else null,
            repeatDayOfMonth = if (repeat == RepeatType.MONTHLY || repeat == RepeatType.YEARLY) repeatDayOfMonth else null,
            repeatMonth = if (repeat == RepeatType.YEARLY) repeatMonth else null,
            reminderEnabled = reminder,
            reminderTime = finalReminderTime,
            subtasks = subtasks.ifEmpty { null },
            completed = existing?.completed ?: false,
            completedAt = existing?.completedAt,
            repeatHistory = existing?.repeatHistory,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModel.save(existing, task, { saved ->
            if (saved.reminderEnabled) scheduleReminder(context, saved) else cancelReminder(context, saved.id)
            onSaved()
        }, { error = it })
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                }
                Text(
                    text = if (editing) "Edit Task" else "Add Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                TextButton(onClick = { performSave() }) {
                    Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }

            // =========================================================================
            // REQUIRED FIELD ORDER
            // =========================================================================

            // 1. FIRST: Task Name Field
            FormSection("Task Name") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // 2. SECOND: Description Field
            FormSection("Description") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description / Note (Optional)") },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )
            }

            // 3. THIRD: Priority Selector
            FormSection("Priority") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.values().forEach { option ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (option) {
                                    TaskPriority.LOW -> Color(0xFF00E676)
                                    TaskPriority.MEDIUM -> Color(0xFFFFEA00)
                                    TaskPriority.HIGH -> Color(0xFFFF1744)
                                },
                                selectedLabelColor = if (option == TaskPriority.MEDIUM) Color.Black else Color.White,
                                labelColor = textColor
                            )
                        )
                    }
                }
            }

            // 4. FOURTH: Reminder Configuration
            FormSection("Reminder Settings") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Notification Reminder", color = textColor, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = reminder,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= 33) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                reminder = checked
                            }
                        )
                    }
                    if (reminder) {
                        Text(
                            text = "Reminder alarm will trigger at task due time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 5. FIFTH: Repeat Selector
            FormSection("Repeat Settings") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            RepeatType.NONE,
                            RepeatType.DAILY,
                            RepeatType.WEEKLY,
                            RepeatType.MONTHLY,
                            RepeatType.YEARLY,
                            RepeatType.CUSTOM
                        ).forEach { option ->
                            FilterChip(
                                selected = repeat == option,
                                onClick = { repeat = option },
                                label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    labelColor = textColor
                                )
                            )
                        }
                    }

                    when (repeat) {
                        RepeatType.WEEKLY -> {
                            Text("Active weekdays", style = MaterialTheme.typography.labelMedium, color = textColor)
                            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7).forEach { (day, dayNum) ->
                                    val isSelected = repeatDaysOfWeek.contains(dayNum)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { repeatDaysOfWeek = if (isSelected) repeatDaysOfWeek - dayNum else repeatDaysOfWeek + dayNum },
                                        label = { Text(day) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White,
                                            labelColor = textColor
                                        )
                                    )
                                }
                            }
                        }
                        RepeatType.MONTHLY -> {
                            var expanded by remember { mutableStateOf(false) }
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
                        RepeatType.YEARLY -> {
                            OutlinedButton(
                                onClick = {
                                    val calendar = Calendar.getInstance().apply {
                                        set(Calendar.MONTH, repeatMonth - 1)
                                        set(Calendar.DAY_OF_MONTH, repeatDayOfMonth)
                                    }
                                    DatePickerDialog(context, { _, year, month, day ->
                                        repeatMonth = month + 1
                                        repeatDayOfMonth = day
                                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("${monthNameText(repeatMonth)} $repeatDayOfMonth", color = textColor)
                            }
                        }
                        RepeatType.CUSTOM -> {
                            OutlinedTextField(
                                value = repeatInterval,
                                onValueChange = { input -> repeatInterval = input.filter { it.isDigit() } },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Repeat Interval (Every N Days)") },
                                singleLine = true
                            )
                        }
                        else -> {}
                    }
                }
            }

            // 6. SIXTH: Due Date and Due Time Controls
            FormSection("Due Date & Time") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                val now = Calendar.getInstance()
                                DatePickerDialog(context, { _, year, month, day ->
                                    dueDate = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = dueDate?.let { SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Select Date",
                                color = textColor
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val now = Calendar.getInstance()
                                TimePickerDialog(context, { _, hour, minute ->
                                    dueTime = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0) }.timeInMillis
                                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = dueTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) } ?: "Select Time",
                                color = textColor
                            )
                        }
                    }

                    // Communication of Default Due Date behavior
                    val isDefaultUsed = dueDate == null || dueTime == null
                    Surface(
                        color = if (isDefaultUsed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        val defaultDisplayDate = SimpleDateFormat("EEE, d MMM yyyy 'at 20:00'", Locale.getDefault()).format(Date(defaultDueDateMillis))
                        Text(
                            text = if (isDefaultUsed) "Default Schedule Applied: $defaultDisplayDate (Select custom date/time above to override)" else "Custom schedule configured.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // 7. SEVENTH: Subtask & Checklist Section
            FormSection("Subtasks & Checklist") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubtaskTitle,
                            onValueChange = { newSubtaskTitle = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Add checklist item / subtask") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor)
                        )
                        Button(onClick = {
                            if (newSubtaskTitle.isNotBlank()) {
                                subtasks = subtasks + SubtaskItem(title = newSubtaskTitle.trim())
                                newSubtaskTitle = ""
                            }
                        }) {
                            Text("Add")
                        }
                    }

                    subtasks.forEachIndexed { index, subtask ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = subtask.completed,
                                        onCheckedChange = { checked ->
                                            subtasks = subtasks.mapIndexed { idx, item -> if (idx == index) item.copy(completed = checked) else item }
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(subtask.title, color = textColor)
                                }
                                IconButton(onClick = { subtasks = subtasks.filterIndexed { idx, _ -> idx != index } }) {
                                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // Save / Delete & Point Cost Info
            if (!editing) {
                Text("Create Task (Costs 1 Point)", color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally), fontWeight = FontWeight.Bold)
            } else if (existing != null) {
                Button(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Task (Costs 5 Points if Incomplete)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(72.dp))
        }
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete Task?") },
            text = { Text("Deleting an incomplete task costs 5 Elite Points.", color = Color.Red) },
            confirmButton = {
                TextButton(onClick = {
                    cancelReminder(context, existing.id)
                    viewModel.delete(existing) { onBack() }
                }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

private fun monthNameText(month: Int): String = Month.of(month.coerceIn(1, 12)).name.lowercase().replaceFirstChar { it.uppercase() }

private fun reminderTime(date: Long?, time: Long?): Long? {
    if (date == null || time == null) return null
    return Calendar.getInstance().apply {
        timeInMillis = date
        val source = Calendar.getInstance().apply { timeInMillis = time }
        set(Calendar.HOUR_OF_DAY, source.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, source.get(Calendar.MINUTE))
        set(Calendar.SECOND, 0)
    }.timeInMillis
}

private fun scheduleReminder(context: Context, task: TaskEntity) {
    val trigger = task.reminderTime ?: return
    val manager = context.getSystemService(AlarmManager::class.java)
    val intent = Intent(context, ReminderAlarmReceiver::class.java).putExtra("taskId", task.id).putExtra("title", task.title)
    val pending = PendingIntent.getBroadcast(context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
}

private fun cancelReminder(context: Context, taskId: Long) {
    val manager = context.getSystemService(AlarmManager::class.java)
    val pending = PendingIntent.getBroadcast(context, taskId.toInt(), Intent(context, ReminderAlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
    pending?.let { manager.cancel(it); it.cancel() }
}
