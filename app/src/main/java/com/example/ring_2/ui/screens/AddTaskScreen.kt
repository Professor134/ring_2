package com.example.ring_2.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.TaskEntity
import com.example.ring_2.data.model.TaskPriority
import com.example.ring_2.data.model.TaskRepeatType
import com.example.ring_2.logic.DateTimeUtils
import com.example.ring_2.logic.RingNotificationManager
import com.example.ring_2.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: MainViewModel,
    taskId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.allTasks.collectAsState()
    val userProg by viewModel.userProgress.collectAsState()
    val existingTask = remember(taskId, tasks) { tasks.find { it.id == taskId } }

    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var priority by remember { mutableStateOf(existingTask?.priority ?: TaskPriority.MEDIUM) }
    var dueDate by remember { mutableLongStateOf(existingTask?.dueDate ?: DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis())) }
    var dueTime by remember { mutableStateOf(existingTask?.dueTime ?: "12:00") }
    var reminderEnabled by remember { mutableStateOf(existingTask?.reminderEnabled ?: false) }
    var repeatType by remember { mutableStateOf(existingTask?.repeatType ?: TaskRepeatType.NONE) }
    var repeatDayOfWeek by remember { mutableIntStateOf(existingTask?.repeatDayOfWeek ?: 2) } // Default Monday (2)
    var repeatDayOfMonth by remember { mutableIntStateOf(existingTask?.repeatDayOfMonth ?: 1) }
    var repeatMonth by remember { mutableIntStateOf(existingTask?.repeatMonth ?: 0) } // Default January (0)

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }

    val canSave = title.isNotBlank()

    // Permission launcher for notifications (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            reminderEnabled = false
            showError = "Notification permission is required for reminders"
        }
    }

    fun handleSave() {
        if (!canSave) return
        
        var finalDueDate = dueDate
        if (repeatType != TaskRepeatType.NONE) {
            finalDueDate = viewModel.calculateInitialDueDate(
                repeatType,
                if (repeatType == TaskRepeatType.WEEKLY) repeatDayOfWeek else null,
                if (repeatType == TaskRepeatType.MONTHLY || repeatType == TaskRepeatType.YEARLY) repeatDayOfMonth else null,
                if (repeatType == TaskRepeatType.YEARLY) repeatMonth else null
            )
        }

        // Ensure due date is not in the past
        val today = DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis())
        if (finalDueDate < today) {
            showError = "Due date cannot be in the past."
            return
        }

        if (taskId == null) {
            val currentPoints = userProg?.currentPoints ?: 0
            if (currentPoints < 1) {
                showError = "Not enough Elite Points. (1 required)"
                return
            }
        }

        val task = TaskEntity(
            id = taskId ?: 0,
            title = title,
            description = description,
            priority = priority,
            dueDate = finalDueDate,
            dueTime = dueTime,
            repeatType = repeatType,
            repeatDayOfWeek = if (repeatType == TaskRepeatType.WEEKLY) repeatDayOfWeek else null,
            repeatDayOfMonth = if (repeatType == TaskRepeatType.MONTHLY || repeatType == TaskRepeatType.YEARLY) repeatDayOfMonth else null,
            repeatMonth = if (repeatType == TaskRepeatType.YEARLY) repeatMonth else null,
            reminderEnabled = reminderEnabled,
            completed = existingTask?.completed ?: false,
            completedAt = existingTask?.completedAt
        )
        
        if (taskId == null) {
            viewModel.addTask(task) { newId ->
                if (reminderEnabled) {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = finalDueDate
                        val parts = dueTime.split(":")
                        set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                        set(Calendar.MINUTE, parts[1].toInt())
                        set(Calendar.SECOND, 0)
                    }
                    RingNotificationManager.scheduleTaskReminder(context, newId, task.title, cal.timeInMillis)
                }
            }
        } else {
            viewModel.updateTask(task)
            if (reminderEnabled) {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = finalDueDate
                    val parts = dueTime.split(":")
                    set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    set(Calendar.MINUTE, parts[1].toInt())
                    set(Calendar.SECOND, 0)
                }
                RingNotificationManager.scheduleTaskReminder(context, taskId, task.title, cal.timeInMillis)
            } else {
                RingNotificationManager.cancelTaskReminder(context, taskId)
            }
        }
        
        onBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) stringResource(com.example.ring_2.R.string.title_add_task) else stringResource(com.example.ring_2.R.string.title_edit_task), color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    TextButton(onClick = { handleSave() }, enabled = canSave) {
                        Text(if (taskId == null) stringResource(com.example.ring_2.R.string.btn_create) else stringResource(com.example.ring_2.R.string.btn_save), color = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
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
            if (showError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = showError!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(com.example.ring_2.R.string.label_task_name), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; showError = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(com.example.ring_2.R.string.hint_habit_name), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(com.example.ring_2.R.string.label_description_note), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(com.example.ring_2.R.string.hint_description), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(com.example.ring_2.R.string.label_priority), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH).forEach { p ->
                        val isSelected = priority == p
                        FilterChip(
                            selected = isSelected,
                            onClick = { priority = p },
                            label = { Text(p.name, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when(p) {
                                    TaskPriority.HIGH -> colorResource(com.example.ring_2.R.color.priority_high)
                                    TaskPriority.MEDIUM -> colorResource(com.example.ring_2.R.color.priority_medium)
                                    TaskPriority.LOW -> colorResource(com.example.ring_2.R.color.priority_low)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                selectedLabelColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            if (repeatType == TaskRepeatType.NONE) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(com.example.ring_2.R.string.label_due_date), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dueDate)),
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(com.example.ring_2.R.string.label_due_time), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = dueTime,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                // For repeating tasks, still allow time picker for the daily notification
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(com.example.ring_2.R.string.label_due_time), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = dueTime,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // REMINDER
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(stringResource(com.example.ring_2.R.string.label_reminder), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        Text("Get notified when task is due", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = {
                            if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            reminderEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // REPEAT SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(com.example.ring_2.R.string.label_repeat), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TaskRepeatType.NONE, TaskRepeatType.WEEKLY, TaskRepeatType.MONTHLY, TaskRepeatType.YEARLY).forEach { type ->
                        val label = when(type) {
                            TaskRepeatType.NONE -> stringResource(com.example.ring_2.R.string.repeat_none)
                            TaskRepeatType.WEEKLY -> stringResource(com.example.ring_2.R.string.repeat_weekly)
                            TaskRepeatType.MONTHLY -> stringResource(com.example.ring_2.R.string.repeat_monthly)
                            TaskRepeatType.YEARLY -> stringResource(com.example.ring_2.R.string.repeat_yearly)
                        }
                        FilterChip(
                            selected = repeatType == type,
                            onClick = { repeatType = type },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (repeatType != TaskRepeatType.NONE) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (repeatType == TaskRepeatType.WEEKLY) {
                                Text(stringResource(com.example.ring_2.R.string.label_repeat_day), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                                    days.forEachIndexed { index, day ->
                                        val dayNum = index + 1
                                        FilterChip(
                                            selected = repeatDayOfWeek == dayNum,
                                            onClick = { repeatDayOfWeek = dayNum },
                                            label = { Text(day) },
                                            modifier = Modifier.size(38.dp),
                                            shape = CircleShape
                                        )
                                    }
                                }
                            }

                            if (repeatType == TaskRepeatType.MONTHLY || repeatType == TaskRepeatType.YEARLY) {
                                if (repeatType == TaskRepeatType.YEARLY) {
                                    Text(stringResource(com.example.ring_2.R.string.label_repeat_month), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                                            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, repeatMonth) }.time)
                                            Text(monthName)
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            for (i in 0..11) {
                                                val mName = SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, i) }.time)
                                                DropdownMenuItem(
                                                    text = { Text(mName) },
                                                    onClick = { repeatMonth = i; expanded = false }
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(stringResource(com.example.ring_2.R.string.label_repeat_date), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                var expandedDate by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(onClick = { expandedDate = true }, modifier = Modifier.fillMaxWidth()) {
                                        Text(repeatDayOfMonth.toString())
                                    }
                                    DropdownMenu(expanded = expandedDate, onDismissRequest = { expandedDate = false }) {
                                        for (i in 1..31) {
                                            DropdownMenuItem(
                                                text = { Text(i.toString()) },
                                                onClick = { repeatDayOfMonth = i; expandedDate = false }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (taskId != null) {
                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(com.example.ring_2.R.string.action_delete) + " Task", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Text(
                    stringResource(com.example.ring_2.R.string.msg_task_cost),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis())
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis ?: dueDate
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val parts = dueTime.split(":")
        val timePickerState = rememberTimePickerState(
            initialHour = parts[0].toInt(),
            initialMinute = parts[1].toInt()
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    if (showDeleteConfirmation && existingTask != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(com.example.ring_2.R.string.dialog_delete_habit_title)) },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    RingNotificationManager.cancelTaskReminder(context, existingTask.id)
                    viewModel.deleteTask(existingTask)
                    onBack()
                }) {
                    Text(stringResource(com.example.ring_2.R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(com.example.ring_2.R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
