package com.example.ringapp.ui.tasks

import android.Manifest
import android.app.*
import android.content.*
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    observeTasks: ObserveTasksUseCase,
    observeProgress: ObserveUserProgressUseCase,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val deleteTask: DeleteTaskUseCase
) : ViewModel() {
    val tasks = observeTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val points = observeProgress().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun save(existing: TaskEntity?, task: TaskEntity, onSaved: (TaskEntity) -> Unit, onError: (String) -> Unit) = viewModelScope.launch { try { val saved = if (existing == null) { check((points.value?.currentPoints ?: 0) >= 1) { "Not enough Elite Points." }; task.copy(id = createTask(task.title, task.description.orEmpty(), task.priority, task.dueDate, task.dueTime, task.repeatType, task.reminderEnabled, task.repeatInterval)) } else { updateTask(task.copy(id = existing.id)); task.copy(id = existing.id) }; onSaved(saved) } catch (e: Exception) { onError(e.message ?: "Unable to save task") } }
    fun delete(task: TaskEntity, onDeleted: () -> Unit) = viewModelScope.launch { deleteTask(task.id); onDeleted() }
}

@Composable
fun AddTaskScreen(taskId: Long? = null, onSaved: () -> Unit, onBack: () -> Unit, viewModel: AddTaskViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(); val existing = tasks.firstOrNull { it.id == taskId }; val context = LocalContext.current; val editing = taskId != null
    var title by remember(existing) { mutableStateOf(existing?.title.orEmpty()) }; var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }; var priority by remember(existing) { mutableStateOf(existing?.priority ?: TaskPriority.MEDIUM) }; var repeat by remember(existing) { mutableStateOf(existing?.repeatType ?: RepeatType.NONE) }; var repeatInterval by remember(existing) { mutableStateOf((existing?.repeatInterval ?: 1).toString()) }; var reminder by remember(existing) { mutableStateOf(existing?.reminderEnabled ?: false) }; var dueDate by remember(existing) { mutableStateOf(existing?.dueDate) }; var dueTime by remember(existing) { mutableStateOf(existing?.dueTime) }; var error by remember { mutableStateOf<String?>(null) }; var confirmDelete by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (!granted) reminder = false }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text(if (editing) "Edit Task" else "Add Task", style = MaterialTheme.typography.headlineSmall); TextButton(onClick = { if (title.isBlank()) error = "Task title is required" else { val task = TaskEntity(id = existing?.id ?: 0, title = title, description = description.ifBlank { null }, priority = priority, dueDate = dueDate, dueTime = dueTime, repeatType = repeat, repeatInterval = repeatInterval.toIntOrNull()?.coerceAtLeast(1) ?: 1, reminderEnabled = reminder, reminderTime = if (reminder) reminderTime(dueDate, dueTime) else null, completed = existing?.completed ?: false, completedAt = existing?.completedAt, createdAt = existing?.createdAt ?: System.currentTimeMillis(), updatedAt = System.currentTimeMillis()); viewModel.save(existing, task, { saved -> if (saved.reminderEnabled) scheduleReminder(context, saved) else cancelReminder(context, saved.id); onSaved() }, { error = it }) } }) { Text("Save") } }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        FormSection("Task") { OutlinedTextField(title, { title = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Task") }, singleLine = true) }
        FormSection("Description") { OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") }, minLines = 3) }
        FormSection("Priority") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { TaskPriority.values().forEach { option -> FilterChip(priority == option, { priority = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }) } } }
        FormSection("Due Date") { OutlinedButton(onClick = { val now = Calendar.getInstance(); DatePickerDialog(context, { _, year, month, day -> dueDate = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show() }, Modifier.fillMaxWidth()) { Text(dueDate?.let { SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Choose date") } }
        FormSection("Due Time") { OutlinedButton(onClick = { val now = Calendar.getInstance(); TimePickerDialog(context, { _, hour, minute -> dueTime = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0) }.timeInMillis }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show() }, Modifier.fillMaxWidth()) { Text(dueTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) } ?: "Choose time") } }
        FormSection("Reminder") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Reminder"); Switch(reminder, { if (it && Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS); reminder = it }) } }
        FormSection("Repeat") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { RepeatType.values().forEach { option -> FilterChip(repeat == option, { repeat = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }) } }; if (repeat == RepeatType.CUSTOM) OutlinedTextField(repeatInterval, { repeatInterval = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text("Repeat every N days") }, singleLine = true) }
        if (editing && existing != null) { Button(onClick = { confirmDelete = true }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)) { Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Delete Task", color = MaterialTheme.colorScheme.error) } }
        Spacer(Modifier.height(72.dp))
    }
    if (confirmDelete && existing != null) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete task?") }, text = { Text("Deleting does not refund the creation cost.") }, confirmButton = { TextButton(onClick = { cancelReminder(context, existing.id); viewModel.delete(existing) { onBack() } }) { Text("Delete", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}

@Composable private fun FormSection(title: String, content: @Composable () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); content() } }
private fun reminderTime(date: Long?, time: Long?): Long? { if (date == null || time == null) return null; return Calendar.getInstance().apply { timeInMillis = date; val source = Calendar.getInstance().apply { timeInMillis = time }; set(Calendar.HOUR_OF_DAY, source.get(Calendar.HOUR_OF_DAY)); set(Calendar.MINUTE, source.get(Calendar.MINUTE)); set(Calendar.SECOND, 0) }.timeInMillis }
private fun scheduleReminder(context: Context, task: TaskEntity) { val trigger = task.reminderTime ?: return; val manager = context.getSystemService(AlarmManager::class.java); val intent = Intent(context, ReminderAlarmReceiver::class.java).putExtra("taskId", task.id).putExtra("title", task.title); val pending = PendingIntent.getBroadcast(context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending) }
private fun cancelReminder(context: Context, taskId: Long) { val manager = context.getSystemService(AlarmManager::class.java); val pending = PendingIntent.getBroadcast(context, taskId.toInt(), Intent(context, ReminderAlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE); pending?.let { manager.cancel(it); it.cancel() } }
