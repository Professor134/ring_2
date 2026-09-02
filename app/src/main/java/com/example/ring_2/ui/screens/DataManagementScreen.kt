package com.example.ring_2.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.logic.BackupService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val habits by viewModel.allHabits.collectAsState()
    val allProgress by viewModel.allProgress.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val achievements by viewModel.allAchievements.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showExportConfirm by remember { mutableStateOf<String?>(null) }

    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                val json = BackupService.exportAsJson(
                    habits, allProgress, tasks, transactions, userProgress, profile, categories, achievements, notifications
                )
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(json.toByteArray())
                }
                scope.launch { snackbarHostState.showSnackbar("JSON Exported successfully") }
            }
        }
    )

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                val csv = BackupService.exportAsCsv(habits)
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(csv.toByteArray())
                }
                scope.launch { snackbarHostState.showSnackbar("CSV Exported successfully") }
            }
        }
    )

    val dbExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            uri?.let {
                BackupService.exportDatabase(context, it)
                scope.launch { snackbarHostState.showSnackbar("Database Backup created") }
            }
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.example.ring_2.R.string.title_data_management), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(stringResource(com.example.ring_2.R.string.label_import_export), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    DataManagementItem(stringResource(com.example.ring_2.R.string.action_export_json), "Export all data (habits, tasks, points, etc.) to JSON") {
                        showExportConfirm = "JSON"
                    }
                    DataManagementItem(stringResource(com.example.ring_2.R.string.action_export_csv), "Export habits list to CSV") {
                        showExportConfirm = "CSV"
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    DataManagementItem("Database Export", "Export the complete internal database file") {
                        showExportConfirm = "DB"
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(16.dp))
                    Text("Exports allow you to keep your data safe and reusable. Import options are currently disabled for stability.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showExportConfirm != null) {
        AlertDialog(
            onDismissRequest = { showExportConfirm = null },
            title = { Text("Confirm Export") },
            text = { Text("Are you sure you want to export your data as $showExportConfirm?") },
            confirmButton = {
                TextButton(onClick = {
                    when(showExportConfirm) {
                        "JSON" -> jsonExportLauncher.launch("ring_backup_${System.currentTimeMillis()}.json")
                        "CSV" -> csvExportLauncher.launch("ring_habits_${System.currentTimeMillis()}.csv")
                        "DB" -> dbExportLauncher.launch("ring_db_backup_${System.currentTimeMillis()}.db")
                    }
                    showExportConfirm = null
                }) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DataManagementItem(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
