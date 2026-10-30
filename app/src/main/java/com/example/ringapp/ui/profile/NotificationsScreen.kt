// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\profile\NotificationsScreen.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.PreferencesDataStore
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.domain.usecase.ObserveTasksUseCase
import com.example.ringapp.domain.usecase.UpdateTaskUseCase
import com.example.ringapp.ui.theme.AppBackground
import com.example.ringapp.work.TaskReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Handles the notification settings ui state logic for this component without modifying runtime behavior.
// Represents the notification settings ui state structure used by the application.
data class NotificationSettingsUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val remindersEnabled: Boolean = true,
    val loading: Boolean = true,
    val loadError: String? = null,
    val actionError: String? = null
)
@HiltViewModel
// Handles the notification settings view model logic for this component without modifying runtime behavior.
// Represents the notification settings view model structure used by the application.
class NotificationSettingsViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val preferences: PreferencesDataStore,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {
    private val loadError = MutableStateFlow<String?>(null)
    private val actionError = MutableStateFlow<String?>(null)
    private val taskFlow = observeTasks().catch { error ->
        if (error is CancellationException) throw error
        Log.e("NotificationSettings", "Unable to load tasks for reminders", error)
        loadError.value = error.message ?: "Unable to load tasks."
        emit(emptyList())
    }
    private val reminderPreferenceFlow = preferences.taskRemindersEnabled.catch { error ->
        if (error is CancellationException) throw error
        Log.e("NotificationSettings", "Unable to load reminder preference", error)
        loadError.value = error.message ?: "Unable to load notification preferences."
        emit(true)
    }

    val state: StateFlow<NotificationSettingsUiState> = combine(
        taskFlow,
        reminderPreferenceFlow,
        loadError,
        actionError
    ) { tasks, remindersEnabled, loadFailure, actionFailure ->
        NotificationSettingsUiState(
            tasks = tasks.filterNot { it.completed },
            remindersEnabled = remindersEnabled,
            loading = false,
            loadError = loadFailure,
            actionError = actionFailure
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationSettingsUiState())

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the set reminders enabled flow without changing runtime behavior.
    fun setRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        actionError.value = null
        try {
            val tasks = observeTasks().first().filterNot { it.completed }
            preferences.setTaskRemindersEnabled(enabled)
            tasks.forEach { task -> TaskReminderScheduler.sync(context, task, enabled) }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            Log.e("NotificationSettings", "Unable to update global reminder setting", error)
            actionError.value = error.message ?: "Unable to update reminders. Please try again."
        }
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the set task reminder flow without changing runtime behavior.
    fun setTaskReminder(task: TaskEntity, enabled: Boolean) = viewModelScope.launch {
        actionError.value = null
        try {
            val reminderTime = if (enabled) TaskReminderScheduler.reminderTime(task.dueDate, task.dueTime) else task.reminderTime
            if (enabled && reminderTime == null) {
                actionError.value = "Add a due date and time to this task before enabling its reminder."
                return@launch
            }
            val updated = task.copy(reminderEnabled = enabled, reminderTime = reminderTime)
            updateTask(updated)
            TaskReminderScheduler.sync(context, updated, preferences.taskRemindersEnabled.first())
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            Log.e("NotificationSettings", "Unable to update reminder for task ${task.id}", error)
            actionError.value = error.message ?: "Unable to update this task reminder."
        }
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the clear action error flow without changing runtime behavior.
    fun clearActionError() {
        actionError.value = null
    }
}

// Handles the reminder permission action logic for this component without modifying runtime behavior.
// Represents the reminder permission action structure used by the application.
private sealed interface ReminderPermissionAction {
    val enablesReminders: Boolean

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Represents the set global structure used by the application.
    data class SetGlobal(val enabled: Boolean) : ReminderPermissionAction {
        override val enablesReminders = enabled
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Represents the set task structure used by the application.
    data class SetTask(val task: TaskEntity, val enabled: Boolean) : ReminderPermissionAction {
        override val enablesReminders = enabled
    }
}
@Composable
// Handles the notifications screen logic for this component without modifying runtime behavior.
// Manages the notifications screen flow without changing runtime behavior.
fun NotificationsScreen(
    onBack: () -> Unit,
    onEditTask: (Long) -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var pendingPermissionAction by remember { mutableStateOf<ReminderPermissionAction?>(null) }

    val performAction: (ReminderPermissionAction) -> Unit = { action ->
        when (action) {
            is ReminderPermissionAction.SetGlobal -> viewModel.setRemindersEnabled(action.enabled)
            is ReminderPermissionAction.SetTask -> viewModel.setTaskReminder(action.task, action.enabled)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingPermissionAction
        pendingPermissionAction = null
        if (granted && action != null) {
            performAction(action)
        } else if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Notification permission is needed to enable reminders.")
            }
        }
    }
    val requestAction: (ReminderPermissionAction) -> Unit = { action ->
        val needsPermission = action.enablesReminders &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingPermissionAction = action
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            performAction(action)
        }
    }

    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    val orderedTasks = remember(state.tasks) {
        state.tasks.sortedWith(compareBy<TaskEntity> { it.dueDate ?: Long.MAX_VALUE }.thenBy { it.title.lowercase() })
    }
    val configuredCount = orderedTasks.count { it.reminderEnabled && it.reminderTime != null }
    val activeCount = if (state.remindersEnabled) {
        orderedTasks.count { task ->
            val reminderTime = task.reminderTime
            task.reminderEnabled && reminderTime != null && reminderTime > System.currentTimeMillis()
        }
    } else 0

    AppBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(contentPadding).statusBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Manage your task reminders", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                if (state.remindersEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Task reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (state.remindersEnabled) "Reminders can be delivered for enabled tasks."
                                    else "Paused. Your individual task choices are saved.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.remindersEnabled,
                                onCheckedChange = { requestAction(ReminderPermissionAction.SetGlobal(it)) }
                            )
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("REMINDER OVERVIEW", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OverviewMetric("Configured", configuredCount.toString(), Modifier.weight(1f))
                                OverviewMetric("Active now", activeCount.toString(), Modifier.weight(1f))
                            }
                            Text(
                                if (state.remindersEnabled) "Turning off the main switch pauses reminders without changing task settings."
                                else "Turn reminders back on to restore each task's saved reminder.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6D00).copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFFF6D00),
                                modifier = Modifier.size(24.dp)
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("BMI 30-Day Health Reminder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFF6D00))
                                Text("Scheduled 30 days after latest height & weight update", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Task controls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Choose which tasks may send a reminder.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (state.loading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.loadError != null) {
                    item {
                        Card(shape = RoundedCornerShape(18.dp)) {
                            Text(
                                state.loadError ?: "Unable to load tasks.",
                                Modifier.padding(18.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else if (orderedTasks.isEmpty()) {
                    item { EmptyReminderTasksCard() }
                } else {
                    items(orderedTasks, key = { it.id }) { task ->
                        TaskReminderCard(
                            task = task,
                            globallyEnabled = state.remindersEnabled,
                            onReminderChange = { enabled -> requestAction(ReminderPermissionAction.SetTask(task, enabled)) },
                            onEdit = { onEditTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
// Handles the overview metric logic for this component without modifying runtime behavior.
// Manages the overview metric flow without changing runtime behavior.
private fun OverviewMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable
// Handles the task reminder card logic for this component without modifying runtime behavior.
// Manages the task reminder card flow without changing runtime behavior.
private fun TaskReminderCard(
    task: TaskEntity,
    globallyEnabled: Boolean,
    onReminderChange: (Boolean) -> Unit,
    onEdit: () -> Unit
) {
    val hasReminderTime = task.reminderTime != null || TaskReminderScheduler.reminderTime(task.dueDate, task.dueTime) != null
    val displayTime = task.reminderTime ?: TaskReminderScheduler.reminderTime(task.dueDate, task.dueTime)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(start = 16.dp, top = 14.dp, end = 10.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        displayTime?.let { SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault()).format(Date(it)) }
                            ?: "Set a due date and time to add a reminder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = task.reminderEnabled,
                    enabled = hasReminderTime,
                    onCheckedChange = onReminderChange
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(42.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit task reminder")
                }
            }
            if (task.reminderEnabled && !globallyEnabled) {
                HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                    "Saved and paused by the main switch",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
// Handles the empty reminder tasks card logic for this component without modifying runtime behavior.
// Manages the empty reminder tasks card flow without changing runtime behavior.
private fun EmptyReminderTasksCard() {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            Text("No active tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Add a task with a due date and time to configure reminders.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
