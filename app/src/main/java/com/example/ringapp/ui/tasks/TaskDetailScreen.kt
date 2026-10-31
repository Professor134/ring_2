// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\tasks\TaskDetailScreen.kt
// Purpose: Displays task details, checklist, historical repeat iterations, and chronology log.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.RepeatType
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.data.local.entities.TaskPriority
import com.example.ringapp.domain.engine.ScheduleEngine
import com.example.ringapp.domain.usecase.CompleteTaskUseCase
import com.example.ringapp.domain.usecase.DeleteTaskUseCase
import com.example.ringapp.domain.usecase.ObserveTaskTransactionsUseCase
import com.example.ringapp.domain.usecase.ObserveTaskUseCase
import com.example.ringapp.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
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

    fun toggleTask(task: TaskEntity) = viewModelScope.launch {
        try {
            _errorMessage.value = null
            completeTask(task)
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Unable to complete task"
        }
    }

    fun toggleSubtask(task: TaskEntity, subtaskId: String) = viewModelScope.launch {
        val updatedSubtasks = task.subtasks?.map { if (it.id == subtaskId) it.copy(completed = !it.completed) else it }
        updateTask(task.copy(subtasks = updatedSubtasks, updatedAt = System.currentTimeMillis()))
    }

    fun delete(taskId: Long, onDeleted: () -> Unit) = viewModelScope.launch {
        deleteTask(taskId)
        onDeleted()
    }
}

data class TaskIterationRecord(
    val iterationDate: Long,
    val completedAt: Long?,
    val status: String,
    val subtasksCount: Int,
    val subtasksCompleted: Int
)

@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
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

    // Parse historical iterations from repeatHistory JSON
    val historicalIterations = remember(t.repeatHistory) {
        val list = mutableListOf<TaskIterationRecord>()
        val historyStr = t.repeatHistory
        if (!historyStr.isNullOrBlank()) {
            try {
                val array = JSONArray(historyStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TaskIterationRecord(
                            iterationDate = obj.optLong("iterationDate", 0L),
                            completedAt = if (obj.has("completedAt")) obj.optLong("completedAt") else null,
                            status = obj.optString("status", "COMPLETED"),
                            subtasksCount = obj.optInt("subtasksCount", 0),
                            subtasksCompleted = obj.optInt("subtasksCompleted", 0)
                        )
                    )
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
        list.sortedByDescending { it.iterationDate }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 48.dp, bottom = 48.dp)
        ) {
            // Header Bar
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { onEdit(t.id) }) { Icon(Icons.Default.Edit, "Edit", tint = textColor) }
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                    }
                }
                Text(t.title, style = MaterialTheme.typography.headlineMedium, color = textColor, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = when (t.priority) {
                        TaskPriority.HIGH -> Color(0xFFFF1744)
                        TaskPriority.MEDIUM -> Color(0xFFFFEA00)
                        TaskPriority.LOW -> Color(0xFF00A84F)
                    }) {
                        Text(
                            t.priority.name,
                            color = if (t.priority == TaskPriority.MEDIUM) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
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

            // Current Task Status Card
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Task Status & Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Status: ${if (t.completed) "Completed ✓" else "Pending ⏳"}",
                            color = if (t.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        t.dueDate?.let {
                            Text(
                                text = "Current Due: ${SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault()).format(Date(it))}",
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Checklist & Subtasks
            if (!t.subtasks.isNullOrEmpty()) {
                item {
                    Text("Checklist & Subtasks", style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold)
                }
                items(t.subtasks.orEmpty(), key = { it.id }) { subtask ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
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
                Button(
                    onClick = { viewModel.toggleTask(t) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (t.completed) "Mark Incomplete (-3 Points)" else "Complete Task (+3 Points)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Historical Iterations Section for Repeatable Tasks
            if (t.repeatType != RepeatType.NONE && historicalIterations.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "HISTORICAL ITERATIONS LOG",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = textColor.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )

                        historicalIterations.forEach { iter ->
                            val iterDueDateStr = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(iter.iterationDate))
                            val iterCompletedDateStr = iter.completedAt?.let {
                                SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(it))
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)),
                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Iteration Due: $iterDueDateStr", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = if (iter.completedAt != null) "Completed ✓" else "Missed ⏳",
                                            color = if (iter.completedAt != null) MaterialTheme.colorScheme.primary else Color(0xFFFF3131),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (iterCompletedDateStr != null) {
                                        Text("Completed at: $iterCompletedDateStr", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.6f))
                                    }
                                    if (iter.subtasksCount > 0) {
                                        Text("Subtasks: ${iter.subtasksCompleted}/${iter.subtasksCount} completed", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Task Calendar Chronology Log
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
            text = { Text("Deleting an incomplete task costs 5 Elite Points.", color = Color.Red) },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(t.id) { onBack() } }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// Task Chronology Log Calendar
// Precedence: Main Task Completion > Subtask Completion > Overdue Date > Due Date
// -----------------------------------------------------------------------------
@Composable
private fun TaskCalendar(
    task: TaskEntity,
    transactions: List<PointTransactionEntity>,
    textColor: Color
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val today = LocalDate.now()

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
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Previous Month",
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
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Next Month",
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

                        val mainTaskCompletedOnDate = transactions.any {
                            it.timestamp in dayStart..dayEnd && it.type == com.example.ringapp.data.local.entities.TransactionType.TASK_COMPLETE
                        } || (task.completed && task.completedAt != null && task.completedAt in dayStart..dayEnd)

                        val subtaskCompletedOnDate = task.updatedAt in dayStart..dayEnd && (task.subtasks?.any { it.completed } == true)

                        val dueDateObj = task.dueDate?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        val isExactDueDate = dueDateObj == date
                        val isPastOverdue = dueDateObj != null && date.isAfter(dueDateObj) && date.isBefore(today) && !task.completed

                        val isScheduledFuture = date.isAfter(today) && ScheduleEngine.isActiveOnDate(task, date)

                        // Precedence Rule: Main Task Completion > Subtask Completion > Overdue Date > Due Date
                        val (borderColor, textColorForDate, bgColor) = when {
                            mainTaskCompletedOnDate -> {
                                Triple(Color(0xFF4CAF50), Color.White, Color(0xFF4CAF50).copy(alpha = 0.18f))
                            }
                            subtaskCompletedOnDate -> {
                                Triple(Color(0xFF2196F3), Color.White, Color(0xFF2196F3).copy(alpha = 0.18f))
                            }
                            isPastOverdue -> {
                                Triple(Color(0xFFFF3131), Color.White, Color(0xFF8B0000).copy(alpha = 0.6f))
                            }
                            isExactDueDate -> {
                                Triple(Color(0xFFFF3131), Color.White, Color(0xFFFF3131).copy(alpha = 0.18f))
                            }
                            isScheduledFuture -> {
                                Triple(Color(0xFF2196F3), Color.White, Color(0xFF2196F3).copy(alpha = 0.15f))
                            }
                            else -> {
                                Triple(Color.White.copy(alpha = 0.2f), Color.White, Color.Transparent)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(1.5.dp, borderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColorForDate
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
