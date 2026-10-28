// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\analytics\BmiTracking.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.dao.HabitDao
import com.example.ringapp.data.local.dao.ProfileDao
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.local.entities.ProfileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Handles the bmi measurement logic for this component without modifying runtime behavior.
// Represents the bmi measurement structure used by the application.
data class BmiMeasurement(
    val date: Long,
    val weightKg: Double,
    val heightCm: Double,
    val bmi: Double
)
@HiltViewModel
// Handles the bmi tracking view model logic for this component without modifying runtime behavior.
// Represents the bmi tracking view model structure used by the application.
class BmiTrackingViewModel @Inject constructor(
    private val database: AppDatabase,
    private val profileDao: ProfileDao,
    private val habitDao: HabitDao
) : ViewModel() {
    val profile: StateFlow<ProfileEntity?> = profileDao.observeCurrent()
        .catch { error ->
            android.util.Log.e("BmiTracking", "Unable to read profile", error)
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the save flow without changing runtime behavior.
    fun save(
        weightKg: Double,
        heightCm: Double,
        onSaved: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            require(weightKg in 1.0..500.0) { "Weight must be between 1 and 500 kg." }
            require(heightCm in 30.0..300.0) { "Height must be between 30 and 300 cm." }
            val now = System.currentTimeMillis()
            val heightMeters = heightCm / 100.0
            val bmi = weightKg / (heightMeters * heightMeters)
            database.withTransaction {
                val current = profileDao.getCurrent()
                    ?: error("Your profile is not available. Please restart the app and try again.")
                val history = current.bodyMeasurements?.let { JSONArray(it) } ?: JSONArray()
                history.put(
                    JSONObject().apply {
                        put("date", now)
                        put("weightKg", weightKg)
                        put("heightCm", heightCm)
                        put("bmi", bmi)
                    }
                )
                profileDao.insert(
                    current.copy(
                        weightKg = weightKg,
                        heightCm = heightCm,
                        bodyMeasurements = history.toString(),
                        updatedAt = now
                    )
                )

                val bmiHabit = habitDao.observeActive().first().firstOrNull { it.isBmiHabit() }
                    ?: HabitEntity(
                        id = HabitEntity.BMI_HABIT_ID,
                        name = HabitEntity.BMI_HABIT_NAME,
                        categoryId = null,
                        type = com.example.ringapp.data.local.entities.HabitType.MEASURABLE,
                        target = 25.0,
                        unit = "BMI",
                        scheduleType = com.example.ringapp.data.local.entities.ScheduleType.DAILY,
                        startDate = now,
                        color = HabitEntity.PLATINUM_COLOR,
                        createdAt = now,
                        updatedAt = now
                    ).also { habitDao.insert(it) }
                val dayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
                val existing = habitDao.progressForDate(bmiHabit.id, dayStart)
                habitDao.upsertProgress(
                    HabitProgressEntity(
                        id = existing?.id ?: 0L,
                        habitId = bmiHabit.id,
                        date = dayStart,
                        target = bmi,
                        actual = bmi,
                        percentage = 100,
                        completed = true,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now
                    )
                )
            }
            onSaved()
        } catch (error: Exception) {
            android.util.Log.e("BmiTracking", "Unable to save BMI measurement", error)
            onError(error.message ?: "Unable to save this measurement. Please try again.")
        }
    }
}
@Composable
// Handles the bmi measurement dialog logic for this component without modifying runtime behavior.
// Manages the bmi measurement dialog flow without changing runtime behavior.
fun BmiMeasurementDialog(
    onDismiss: () -> Unit,
    viewModel: BmiTrackingViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var weight by remember(profile) { mutableStateOf(profile?.weightKg?.toString().orEmpty()) }
    var height by remember(profile) { mutableStateOf(profile?.heightCm?.toString().orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update BMI measurements") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter your current measurements. This health log does not affect Elite Points.")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        weight,
                        { weight = it.filter { char -> char.isDigit() || char == '.' }; error = null },
                        Modifier.weight(1f),
                        label = { Text("Weight (kg)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        height,
                        { height = it.filter { char -> char.isDigit() || char == '.' }; error = null },
                        Modifier.weight(1f),
                        label = { Text("Height (cm)") },
                        singleLine = true
                    )
                }
                val bmi = weight.toDoubleOrNull()?.let { value ->
                    height.toDoubleOrNull()?.let { cm ->
                        if (cm > 0.0) value / ((cm / 100.0) * (cm / 100.0)) else null
                    }
                }
                bmi?.let { Text("BMI: ${"%.1f".format(java.util.Locale.getDefault(), it)}") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedWeight = weight.toDoubleOrNull()
                    val parsedHeight = height.toDoubleOrNull()
                    if (parsedWeight == null || parsedHeight == null) {
                        error = "Enter both weight and height."
                    } else {
                        viewModel.save(parsedWeight, parsedHeight, onSaved = onDismiss) { error = it }
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
