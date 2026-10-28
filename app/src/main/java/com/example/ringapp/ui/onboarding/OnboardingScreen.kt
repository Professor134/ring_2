// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\onboarding\OnboardingScreen.kt
// Purpose: Multi-step onboarding sequence for RING application.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.onboarding

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringapp.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date
import java.util.Locale

data class OnboardingDetails(
    val name: String,
    val dateOfBirth: Long,
    val weightKg: Double,
    val heightCm: Double,
    val gender: String
)

@Composable
fun OnboardingScreen(
    isSaving: Boolean,
    errorMessage: String?,
    onComplete: (OnboardingDetails) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }

    // State collected across onboarding screens
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf<Long?>(null) }
    var gender by remember { mutableStateOf("") }
    var isConfirmed by remember { mutableStateOf(false) }

    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    var validationMessage by remember { mutableStateOf<String?>(null) }

    val calculatedAge = dateOfBirth?.let { dobMillis ->
        val birthDate = Instant.ofEpochMilli(dobMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        Period.between(birthDate, LocalDate.now()).years
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section Header & Progress Indicator
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "STEP $currentStep OF 3",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { currentStep / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            }

            // Screen Content Area with Slide Animations
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "onboarding_step_transition"
                ) { step ->
                    when (step) {
                        1 -> OnboardingScreenOne()
                        2 -> OnboardingScreenTwo(
                            name = name,
                            onNameChange = { name = it; validationMessage = null },
                            dateOfBirth = dateOfBirth,
                            calculatedAge = calculatedAge,
                            onDobChange = { dateOfBirth = it; validationMessage = null },
                            gender = gender,
                            onGenderChange = { gender = it; validationMessage = null },
                            isConfirmed = isConfirmed,
                            onConfirmChange = { isConfirmed = it; validationMessage = null }
                        )
                        3 -> OnboardingScreenThree(
                            weight = weight,
                            onWeightChange = { weight = it; validationMessage = null },
                            height = height,
                            onHeightChange = { height = it; validationMessage = null }
                        )
                    }
                }
            }

            // Validation or Error Message Display
            val displayedError = validationMessage ?: errorMessage
            if (displayedError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = displayedError,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Navigation Buttons (Next / Back / Finish)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = {
                            validationMessage = null
                            currentStep--
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isSaving
                    ) {
                        Text("Back", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        validationMessage = null
                        when (currentStep) {
                            1 -> {
                                currentStep = 2
                            }
                            2 -> {
                                val dob = dateOfBirth
                                val today = LocalDate.now()
                                val selectedDate = dob?.let {
                                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                }

                                when {
                                    name.trim().isBlank() -> {
                                        validationMessage = "Please enter your name to continue."
                                    }
                                    dob == null -> {
                                        validationMessage = "Please select your date of birth."
                                    }
                                    selectedDate != null && selectedDate.isAfter(today) -> {
                                        validationMessage = "Date of birth cannot be in the future."
                                    }
                                    calculatedAge != null && calculatedAge < 0 -> {
                                        validationMessage = "Please select a valid date of birth."
                                    }
                                    gender.trim().isBlank() -> {
                                        validationMessage = "Please select your gender."
                                    }
                                    !isConfirmed -> {
                                        validationMessage = "Please confirm your personal information."
                                    }
                                    else -> {
                                        currentStep = 3
                                    }
                                }
                            }
                            3 -> {
                                val parsedWeight = weight.toDoubleOrNull()
                                val parsedHeight = height.toDoubleOrNull()

                                when {
                                    parsedWeight == null || parsedWeight !in 1.0..500.0 -> {
                                        validationMessage = "Please enter a valid weight between 1 and 500 kg."
                                    }
                                    parsedHeight == null || parsedHeight !in 30.0..300.0 -> {
                                        validationMessage = "Please enter a valid height between 30 and 300 cm."
                                    }
                                    else -> {
                                        onComplete(
                                            OnboardingDetails(
                                                name = name.trim(),
                                                dateOfBirth = requireNotNull(dateOfBirth),
                                                weightKg = requireNotNull(parsedWeight),
                                                heightCm = requireNotNull(parsedHeight),
                                                gender = gender.trim()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(if (currentStep > 1) 1.5f else 1f)
                        .height(52.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = when {
                            isSaving -> "Saving Profile…"
                            currentStep == 3 -> "Get Started"
                            else -> "Next"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Screen 1: Welcome & App Branding
// -----------------------------------------------------------------------------
@Composable
private fun OnboardingScreenOne() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier.padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "RING logo",
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Build a rhythm that lasts.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Welcome to RING. Track your daily habits, organize smart tasks, and gain intelligent insights into your progress.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// Screen 2: Personal Information (Name, DOB, Calculated Age, Gender, Confirmation)
// -----------------------------------------------------------------------------
@Composable
private fun OnboardingScreenTwo(
    name: String,
    onNameChange: (String) -> Unit,
    dateOfBirth: Long?,
    calculatedAge: Int?,
    onDobChange: (Long) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    isConfirmed: Boolean,
    onConfirmChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val selectedDate = dateOfBirth?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column {
            Text(
                text = "PERSONAL DETAILS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Tell us about yourself",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Your details are stored locally on your device to personalize your experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Your Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Date of Birth Button & Age Display
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DATE OF BIRTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    OutlinedButton(
                        onClick = {
                            val today = LocalDate.now()
                            val initial = selectedDate ?: today.minusYears(25)
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val picked = LocalDate.of(year, month + 1, day)
                                    val millis = picked.atStartOfDay(ZoneId.systemDefault())
                                        .toInstant().toEpochMilli()
                                    onDobChange(millis)
                                },
                                initial.year,
                                initial.monthValue - 1,
                                initial.dayOfMonth
                            ).apply {
                                datePicker.maxDate = System.currentTimeMillis()
                            }.show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        val formattedDob = dateOfBirth?.let {
                            SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(it))
                        }
                        Text(
                            text = formattedDob ?: "Select Date of Birth",
                            color = if (dateOfBirth != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Calculated Age Display
                    if (calculatedAge != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Calculated Age: $calculatedAge years old",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Gender Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GENDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Female", "Male", "Other").forEach { option ->
                            FilterChip(
                                selected = gender == option,
                                onClick = { onGenderChange(option) },
                                label = { Text(option) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Confirmation Card & Checkbox
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CONFIRM INFORMATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "• Name: ${if (name.isBlank()) "Not set" else name}\n" +
                                    "• DOB: ${selectedDate?.toString() ?: "Not set"}" +
                                    "${calculatedAge?.let { " ($it yrs)" } ?: ""}\n" +
                                    "• Gender: ${if (gender.isBlank()) "Not set" else gender}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isConfirmed,
                                onCheckedChange = onConfirmChange
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "I confirm the entered personal information is correct.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Screen 3: Physical Information (Weight, Height & Initial BMI Initialization)
// -----------------------------------------------------------------------------
@Composable
private fun OnboardingScreenThree(
    weight: String,
    onWeightChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit
) {
    val weightVal = weight.toDoubleOrNull()
    val heightVal = height.toDoubleOrNull()
    val bmi = if (weightVal != null && heightVal != null && heightVal > 0) {
        weightVal / ((heightVal / 100.0) * (heightVal / 100.0))
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column {
            Text(
                text = "PHYSICAL INFORMATION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Initial Body Metrics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Your initial weight and height will be used to initialize BMI tracking and health progress.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { input ->
                            onWeightChange(input.filter { it.isDigit() || it == '.' })
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = height,
                        onValueChange = { input ->
                            onHeightChange(input.filter { it.isDigit() || it == '.' })
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Initial BMI Preview Card
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "INITIAL BMI METRIC PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        if (bmi != null && bmi in 5.0..100.0) {
                            val category = when {
                                bmi < 18.5 -> "Underweight"
                                bmi < 25.0 -> "Normal weight"
                                bmi < 30.0 -> "Overweight"
                                else -> "Obese"
                            }
                            Text(
                                text = "BMI: ${"%.1f".format(Locale.getDefault(), bmi)} ($category)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Enter valid weight and height above to preview initial BMI.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "This initial record will be saved under today's date for your health analytics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
