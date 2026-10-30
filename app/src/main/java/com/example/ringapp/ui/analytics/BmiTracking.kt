// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\analytics\BmiTracking.kt
// Purpose: BMI measurement logging, 30-day reminder scheduling, and dialog.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.analytics

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.ringapp.work.BmiReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class BmiMeasurement(
    val date: Long,
    val weightKg: Double,
    val heightCm: Double,
    val bmi: Double
)

@HiltViewModel
class BmiTrackingViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
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

    fun save(
        weightKg: Double,
        heightCm: Double,
        onSaved: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            require(weightKg in 1.0..500.0) { "Please enter a valid weight between 1 and 500 kg." }
            require(heightCm in 30.0..300.0) { "Please enter a valid height between 30 and 300 cm." }

            val now = System.currentTimeMillis()
            val heightMeters = heightCm / 100.0
            check(heightMeters > 0) { "Height must be greater than 0." }
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
                        color = 0xFFFF6D00.toInt(),
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

            // Schedule 30-day reminder relative to the new measurement date
            val nextReminderTime = now + (30L * 24 * 60 * 60 * 1000L)
            BmiReminderScheduler.scheduleBmiReminder(appContext, nextReminderTime)

            onSaved()
        } catch (error: Exception) {
            android.util.Log.e("BmiTracking", "Unable to save BMI measurement", error)
            onError(error.message ?: "Unable to save this measurement. Please try again.")
        }
    }
}

@Composable
fun BmiMeasurementDialog(
    onDismiss: () -> Unit,
    viewModel: BmiTrackingViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var weight by remember(profile) { mutableStateOf(profile?.weightKg?.toString().orEmpty()) }
    var height by remember(profile) { mutableStateOf(profile?.heightCm?.toString().orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val dialogBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF20232A) else androidx.compose.ui.graphics.Color(0xFFF0F2F5)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        title = { Text("Update BMI Measurements") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter your current height and weight. This health log does not affect Elite Points.")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { input ->
                            weight = input.filter { it.isDigit() || it == '.' }
                            error = null
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Weight (kg)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { input ->
                            height = input.filter { it.isDigit() || it == '.' }
                            error = null
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Height (cm)") },
                        singleLine = true
                    )
                }
                val bmi = weight.toDoubleOrNull()?.let { w ->
                    height.toDoubleOrNull()?.let { cm ->
                        if (cm > 0.0) w / ((cm / 100.0) * (cm / 100.0)) else null
                    }
                }
                bmi?.let {
                    val category = when {
                        it < 18.5 -> "Underweight"
                        it < 25.0 -> "Normal weight"
                        it < 30.0 -> "Overweight"
                        else -> "Obese"
                    }
                    Text("Calculated BMI: ${"%.1f".format(Locale.getDefault(), it)} ($category)", color = MaterialTheme.colorScheme.primary)
                }
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
