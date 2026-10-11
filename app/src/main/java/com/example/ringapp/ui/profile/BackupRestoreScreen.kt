// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\profile\BackupRestoreScreen.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.ui.theme.AppBackground
@Composable

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

@Composable
// Handles the backup restore screen logic for this component without modifying runtime behavior.
// Manages the backup restore screen flow without changing runtime behavior.
fun BackupRestoreScreen(
    onBack: () -> Unit,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedBackup by remember { mutableStateOf<android.net.Uri?>(null) }
    var confirmRestore by remember { mutableStateOf(false) }
    val exportPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.exportTo(uri)
    }
    val restorePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedBackup = uri
            confirmRestore = true
        }
    }

    AppBackground {
        Surface(Modifier.fillMaxSize(), color = Color.Transparent) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Backup & Restore", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Keep a portable copy of your RING data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (state.busy) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                            Text(state.workingMessage.orEmpty(), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                } else {
                    state.resultMessage?.let { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.isError) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                message,
                                modifier = Modifier.padding(16.dp),
                                color = if (state.isError) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                ActionCard(
                    title = "Export a backup",
                    description = "Save your habits, progress, tasks, points, profile and health measurements, and app preferences to a portable JSON file.",
                    buttonLabel = "Choose backup location",
                    enabled = !state.busy,
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                    onClick = {
                        exportPicker.launch("ring-backup-${System.currentTimeMillis()}.json")
                    }
                )
                ActionCard(
                    title = "Restore a backup",
                    description = "Choose a RING backup file. Restoring replaces the data currently stored on this device.",
                    buttonLabel = "Choose backup file",
                    enabled = !state.busy,
                    icon = { Icon(Icons.Default.CloudDownload, contentDescription = null) },
                    onClick = { restorePicker.launch(arrayOf("application/json", "text/json")) }
                )
                Text(
                    "Backup files include personal profile and health data. They are stored wherever you choose in Android’s file picker. Keep the file somewhere safe; restoring replaces current local RING data.",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (confirmRestore && selectedBackup != null) {
        AlertDialog(
            onDismissRequest = {
                confirmRestore = false
                selectedBackup = null
            },
            title = { Text("Replace current RING data?") },
            text = {
                Text("This permanently replaces the data and app preferences on this device with the selected backup.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = selectedBackup
                    selectedBackup = null
                    confirmRestore = false
                    if (uri != null) viewModel.restoreFrom(uri)
                }) { Text("Restore backup", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    confirmRestore = false
                    selectedBackup = null
                }) { Text("Cancel") }
            }
        )
    }
}
@Composable

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

@Composable
// Handles the action card logic for this component without modifying runtime behavior.
// Manages the action card flow without changing runtime behavior.
private fun ActionCard(
    title: String,
    description: String,
    buttonLabel: String,
    enabled: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                icon()
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(buttonLabel) }
        }
    }
}
