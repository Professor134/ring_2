package com.example.ringapp.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf("SYSTEM") }
    val steps = listOf("Welcome", "Name", "Avatar", "Goals", "Habits", "Notifications", "Theme")
    val isLastStep = step == steps.lastIndex

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RING", style = MaterialTheme.typography.displaySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text("Step ${step + 1} of ${steps.size}", style = MaterialTheme.typography.labelLarge)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(steps[step], style = MaterialTheme.typography.headlineMedium)
                when (step) {
                    0 -> Text("Build a rhythm that lasts. Track habits, tasks, streaks, and progress in one quiet place.")
                    1 -> androidx.compose.material3.OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Your name (optional)") })
                    2 -> Text("Choose an avatar later from your profile. You can continue with the default.")
                    3 -> Text("Choose goals that matter to you. This step can be skipped.")
                    4 -> Text("Start with habits such as Exercise, Study, Read, Sleep, or Meditation.")
                    5 -> Text("Reminders stay on your device and can be changed later in Settings.")
                    6 -> { Text("Choose your preferred appearance."); listOf("SYSTEM", "LIGHT", "DARK").forEach { theme -> Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = selectedTheme == theme, onClick = { selectedTheme = theme }); Text(theme.lowercase().replaceFirstChar { it.uppercase() }) } } }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            OutlinedButton(onClick = onComplete) { Text("Skip") }
            Button(
                modifier = Modifier.semantics { contentDescription = if (isLastStep) "Finish onboarding" else "Continue onboarding" },
                onClick = { if (isLastStep) onComplete() else step++ }
            ) { Text(if (isLastStep) "Finish" else "Continue") }
        }
    }
}
