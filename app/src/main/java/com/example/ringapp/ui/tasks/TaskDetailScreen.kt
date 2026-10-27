// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\tasks\TaskDetailScreen.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.local.entities.TaskPriority
import com.example.ringapp.data.local.entities.RepeatType
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.domain.engine.ScheduleEngine
import com.example.ringapp.domain.usecase.*
import com.example.ringapp.ui.theme.CyberNeonGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@HiltViewModel
// Handles the task detail view model logic for this component without modifying runtime behavior.
// Represents the task detail view model structure used by the application.
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTask: ObserveTaskUseCase,
    observeTaskTransactions: ObserveTaskTransactionsUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val deleteTask: DeleteTaskUseCase
) : ViewModel() {
    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0L

    val task: StateFlow<TaskEntity?> = observeTask(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val transactions: StateFlow<List<PointTransactionEntity>> = observeTaskTransactions(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

// Manages the toggle task flow without changing runtime behavior.
    fun toggleTask(task: TaskEntity) = viewModelScope.launch {
        try {
            _errorMessage.value = null
            completeTask(task)
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Unable to complete task"
        }
    }
    
// Manages the toggle subtask flow without changing runtime behavior.
    fun toggleSubtask(task: TaskEntity, subtaskId: String) = viewModelScope.launch {
        val updatedSubtasks = task.subtasks?.map { if (it.id == subtaskId) it.copy(completed = !it.completed) else it }
        updateTask(task.copy(subtasks = updatedSubtasks, updatedAt = System.currentTimeMillis()))
    }

// Manages the delete flow without changing runtime behavior.
    fun delete(taskId: Long, onDeleted: () -> Unit) = viewModelScope.launch {
        deleteTask(taskId)
        onDeleted()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Handles the task detail screen logic for this component without modifying runtime behavior.
// Manages the task detail screen flow without changing runtime behavior.
fun TaskDetailScreen(onBack: () -> Unit, onEdit: (Long) -> Unit, viewModel: TaskDetailViewModel = hiltViewModel()) {
    val task by viewModel.task.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
    var confirmDelete by remember { mutableStateOf(false) }

    if (task == null) {
        Surface(Modifier.fillMaxSize(), color = bgColor) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val t = task!!

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 48.dp, bottom = 48.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { onEdit(t.id) }) { Icon(Icons.Default.Edit, "Edit", tint = textColor) }
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                    }
                }
                Text(t.title, style = MaterialTheme.typography.headlineMedium, color = textColor, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = when(t.priority) {
                        TaskPriority.HIGH -> Color(0xFFD32F2F)
                        TaskPriority.MEDIUM -> Color(0xFFFBC02D)
                        TaskPriority.LOW -> Color(0xFF388E3C)
                    }) {
                        Text(t.priority.name, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    if (t.repeatType != RepeatType.NONE) {
                        Text("• Repeats: ${t.repeatType.name}", color = textColor.copy(alpha = 0.6f))
                    }
                }
            }

            errorMsg?.let {
                item {
                    Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }

            if (!t.description.isNullOrBlank()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(t.description!!, color = textColor.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Task Status & Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Status: ${if (t.completed) "Completed ✓" else "Pending ⏳"}", color = if (t.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        t.dueDate?.let {
                            Text("Due: ${SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault()).format(Date(it))}", color = textColor.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            if (!t.subtasks.isNullOrEmpty()) {
                item {
                    Text("Checklist & Subtasks", style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold)
                }
                items(t.subtasks.orEmpty(), key = { it.id }) { subtask ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(checked = subtask.completed, onCheckedChange = { viewModel.toggleSubtask(t, subtask.id) })
                                Spacer(Modifier.width(8.dp))
                                Text(subtask.title, color = textColor, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.toggleTask(t) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(if (t.completed) "Mark Incomplete (-4 Points)" else "Complete Task (+4 Points)")
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                TaskCalendar(task = t, transactions = transactions, textColor = textColor)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete task?") },
            text = { Text("Deleting a task costs 5 Elite Points.", color = Color.Red) },
            confirmButton = { TextButton(onClick = { viewModel.delete(t.id) { onBack() } }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
// Handles the task calendar logic for this component without modifying runtime behavior.
// Manages the task calendar flow without changing runtime behavior.
private fun TaskCalendar(
    task: TaskEntity,
    transactions: List<PointTransactionEntity>,
    textColor: Color
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "CHRONO LOG",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = textColor.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        null,
                        Modifier.size(20.dp),
                        tint = textColor
                    )
                }
                Text(
                    currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())).uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                IconButton(onClick = {
                    currentMonth = currentMonth.plusMonths(1)
                }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        null,
                        Modifier.size(20.dp).graphicsLayer(rotationZ = 180f),
                        tint = textColor
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    day,
                    Modifier.width(34.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Black
                )
            }
        }

        val firstDayOfWeek = currentMonth.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()
        val weeks = (daysInMonth + firstDayOfWeek + 6) / 7

        repeat(weeks) { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(7) { dayOfWeek ->
                    val dayOfMonth = week * 7 + dayOfWeek - firstDayOfWeek + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val date = currentMonth.withDayOfMonth(dayOfMonth)
                        val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                        val completedOnDate = transactions.any { it.timestamp in dayStart..dayEnd && it.amount > 0 }
                        val active = if (task.repeatType == RepeatType.NONE) {
                            val due = task.dueDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                            due == null || due == date
                        } else {
                            ScheduleEngine.isActiveOnDate(task, date)
                        }

                        val color = when {
                            date.isAfter(LocalDate.now()) -> Color.Gray.copy(alpha = 0.1f)
                            completedOnDate -> CyberNeonGreen
                            active -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (completedOnDate) Color.Black else if (color == Color.Transparent) textColor else textColor.copy(alpha = 0.8f)
                            )
                        }
                    } else {
                        Spacer(Modifier.size(34.dp))
                    }
                }
            }
        }
    }
}
