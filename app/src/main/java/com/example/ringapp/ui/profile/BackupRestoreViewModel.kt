// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\profile\BackupRestoreViewModel.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.BackupRestoreService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Handles the backup restore ui state logic for this component without modifying runtime behavior.
// Represents the backup restore ui state structure used by the application.
data class BackupRestoreUiState(
    val busy: Boolean = false,
    val workingMessage: String? = null,
    val resultMessage: String? = null,
    val isError: Boolean = false
)
@HiltViewModel

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

@HiltViewModel
// Handles the backup restore view model logic for this component without modifying runtime behavior.
// Represents the backup restore view model structure used by the application.
class BackupRestoreViewModel @Inject constructor(
    private val backupRestoreService: BackupRestoreService
) : ViewModel() {
    private val _state = MutableStateFlow(BackupRestoreUiState())
    val state: StateFlow<BackupRestoreUiState> = _state.asStateFlow()

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the export to flow without changing runtime behavior.
    fun exportTo(uri: Uri) = runOperation(
        working = "Creating backup…",
        success = "Backup exported successfully."
    ) {
        backupRestoreService.exportTo(uri)
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the restore from flow without changing runtime behavior.
    fun restoreFrom(uri: Uri) = runOperation(
        working = "Restoring backup…",
        success = "Backup restored successfully."
    ) {
        backupRestoreService.restoreFrom(uri)
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the run operation flow without changing runtime behavior.
    private fun runOperation(
        working: String,
        success: String,
        operation: suspend () -> Unit
    ) = viewModelScope.launch {
        if (_state.value.busy) return@launch
        _state.value = BackupRestoreUiState(busy = true, workingMessage = working)
        try {
            operation()
            _state.value = BackupRestoreUiState(resultMessage = success)
        } catch (failure: Exception) {
            if (failure is CancellationException) throw failure
            Log.e("BackupRestoreViewModel", working, failure)
            _state.value = BackupRestoreUiState(
                resultMessage = failure.message ?: "The operation could not be completed.",
                isError = true
            )
        }
    }
}
