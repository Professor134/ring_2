package com.example.ring_2.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.TaskEntity
import com.example.ring_2.data.model.TaskPriority
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
        
        // Ensure due date is not in the past
        val today = DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis())
        if (dueDate < today) {
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
            dueDate = dueDate,
            dueTime = dueTime,
            reminderEnabled = reminderEnabled,
            completed = existingTask?.completed ?: false,
            completedAt = existingTask?.completedAt
        )
        
        if (taskId == null) {
            viewModel.addTask(task) { newId ->
                if (reminderEnabled) {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = dueDate
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
                    timeInMillis = dueDate
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
                title = { Text(if (taskId == null) "Add Task" else "Edit Task", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { handleSave() }, enabled = canSave) {
                        Text(if (taskId == null) "Create" else "Save", color = if (canSave) Color(0xFF00E676) else Color.Gray)
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
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = showError!!,
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TASK", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; showError = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Work on project", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E676)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DESCRIPTION", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Optional details...", color = Color.Gray) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E676)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRIORITY", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
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
                                    TaskPriority.HIGH -> Color.Red
                                    TaskPriority.MEDIUM -> Color.Yellow
                                    TaskPriority.LOW -> Color.Cyan
                                    else -> Color.Gray
                                },
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DUE DATE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                    ) {
                        Text(
                            text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dueDate)),
                            modifier = Modifier.padding(16.dp),
                            color = Color.White
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DUE TIME", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true }
                    ) {
                        Text(
                            text = dueTime,
                            modifier = Modifier.padding(16.dp),
                            color = Color.White
                        )
                    }
                }
            }

            // REMINDER
            Surface(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Reminder", color = Color.White, fontWeight = FontWeight.Medium)
                        Text("Get notified when task is due", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = {
                            if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            reminderEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                    )
                }
            }

            if (taskId != null) {
                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Task", color = Color.Red)
                }
            } else {
                Text(
                    "Creating a task costs 1 Elite Point.",
                    color = Color.Gray,
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
            title = { Text("Delete Task?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    RingNotificationManager.cancelTaskReminder(context, existingTask.id)
                    viewModel.deleteTask(existingTask)
                    onBack()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
