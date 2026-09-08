package com.example.ringapp.ui.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.usecase.*
import com.example.ringapp.ui.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(appViewModel: AppViewModel, onEdit: () -> Unit, onAppearance: () -> Unit = {}, onPointHistory: () -> Unit = {}, onNotifications: () -> Unit = {}, onBackup: () -> Unit = {}, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); val profile = state.profile ?: return
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { ProfileAvatar(profile); Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(profile.name.ifBlank { "Your name" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Level ${state.level}"); Text("${state.points} Elite Points", color = MaterialTheme.colorScheme.primary) }; Button(onClick = onEdit) { Text("Edit Profile") } } }
        item { Card { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Level ${state.level}", style = MaterialTheme.typography.titleLarge); val progress = levelProgress(state.lifetimePoints); LinearProgressIndicator({ progress }, Modifier.fillMaxWidth()); Text("${state.lifetimePoints} lifetime earned points toward the next level", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Level depends on lifetime earned Elite Points and never decreases when current points are spent.", style = MaterialTheme.typography.bodySmall) } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { StatCard("Current Streak", state.currentStreak.toString(), Modifier.weight(1f)); StatCard("Best Streak", state.bestStreak.toString(), Modifier.weight(1f)) }; Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { StatCard("Total Habits", state.totalHabits.toString(), Modifier.weight(1f)); StatCard("Completed Tasks", state.completedTasks.toString(), Modifier.weight(1f)) } }
        item { Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(state.achievements) { achievement ->
            Card {
                ListItem(
                    leadingContent = { Icon(if (achievement.unlocked) Icons.Default.Star else Icons.Default.Lock, null, tint = if (achievement.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                    headlineContent = { Text(achievement.description) },
                    supportingContent = { Text(if (achievement.unlocked) "Unlocked" else "${achievement.currentProgress} / ${achievement.threshold}") }
                )
            }
        }
        item { ActionButton("Elite Point History", Icons.Default.List, onPointHistory) }
        item { Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { ActionButton("Appearance", Icons.Default.Settings, onAppearance) }
        item { ActionButton("Notifications", Icons.Default.Notifications, onNotifications) }
        item { ActionButton("Backup & Restore", Icons.Default.CloudUpload, onBackup) }
        item { Text("Theme: ${appViewModel.state.collectAsStateWithLifecycle().value.theme}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("RING", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold); Text("Offline Habit & Productivity Tracker", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Version 1.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@Composable
private fun ProfileAvatar(profile: ProfileEntity) { val uri = profile.photoUri?.let(Uri::parse); Surface(Modifier.size(92.dp).clip(CircleShape), color = Color(profile.avatarColor), border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) { if (uri != null) AsyncImage(model = uri, contentDescription = "Profile photo", modifier = Modifier.fillMaxSize(), onError = {}) else Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, "Default avatar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(52.dp)) } } }
@Composable private fun StatCard(label: String, value: String, modifier: Modifier) { Card(modifier) { Column(Modifier.padding(14.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } } }
@Composable private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { OutlinedButton(onClick = onClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(icon, null); Spacer(Modifier.width(10.dp)); Text(label) } }
private fun levelProgress(lifetime: Int): Float { val level = calculateLevel(lifetime); val current = threshold(level - 1); val next = threshold(level); return ((lifetime - current).toFloat() / (next - current).coerceAtLeast(1)).coerceIn(0f, 1f) }
private fun calculateLevel(points: Int): Int { var level = 1; while (points >= threshold(level) && level < 1000) level++; return level }
private fun threshold(level: Int): Int { if (level <= 1) return 100; var value = 100.0; repeat(level - 1) { value *= 1.5 }; return value.toInt() }

@Composable
fun EditProfileScreen(onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); val profile = state.profile ?: return; val context = LocalContext.current
    var name by remember(profile) { mutableStateOf(profile.name) }; var photoUri by remember(profile) { mutableStateOf(profile.photoUri) }; var dob by remember(profile) { mutableStateOf(profile.dateOfBirth) }; var gender by remember(profile) { mutableStateOf(profile.gender.orEmpty()) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION); photoUri = it.toString() } }
    Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text("Edit Profile", style = MaterialTheme.typography.headlineSmall); TextButton(onClick = { if (name.isNotBlank()) { viewModel.save(profile.copy(name = name, photoUri = photoUri, dateOfBirth = dob, gender = gender.ifBlank { null })); onBack() } }) { Text("Save") } }
        Surface(Modifier.size(150.dp).align(Alignment.CenterHorizontally).clip(CircleShape), color = Color(profile.avatarColor), border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) { if (photoUri != null) AsyncImage(Uri.parse(photoUri), "Profile photo", Modifier.fillMaxSize()) else Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, "Default avatar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(82.dp)) } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onClick = { picker.launch(arrayOf("image/*")) }, Modifier.weight(1f)) { Text("Change Photo") }; OutlinedButton(onClick = { photoUri = null }, Modifier.weight(1f), enabled = photoUri != null) { Text("Remove Photo") } }
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true, isError = name.isBlank())
        Text("Date of Birth", style = MaterialTheme.typography.titleMedium); OutlinedButton(onClick = { val now = Calendar.getInstance(); DatePickerDialog(context, { _, year, month, day -> dob = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }.timeInMillis }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show() }, Modifier.fillMaxWidth()) { Text(dob?.let { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Choose date") }
        Text("Age: ${dob?.let(::calculateAge) ?: "Not set"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Gender", style = MaterialTheme.typography.titleMedium); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("Male", "Female", "Other", "Prefer not to say").forEach { option -> FilterChip(gender == option, { gender = option }, label = { Text(option) }) } }
        Spacer(Modifier.height(64.dp))
    }
}

@Composable fun PointHistoryScreen(onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) { val state by viewModel.state.collectAsStateWithLifecycle(); LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text("Elite Point History", style = MaterialTheme.typography.headlineSmall) }; Text("Current balance: ${state.points}  •  Lifetime earned: ${state.lifetimePoints}") }; items(state.transactions) { transaction -> ListItem(headlineContent = { Text(transaction.description) }, supportingContent = { Text(SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(transaction.timestamp))) }, trailingContent = { Text(if (transaction.amount >= 0) "+${transaction.amount}" else transaction.amount.toString(), color = if (transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }) } } }
@Composable fun ProfileInfoScreen(title: String, description: String, onBack: () -> Unit) { Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text(title, style = MaterialTheme.typography.headlineSmall) }; Card { Text(description, Modifier.padding(20.dp)) } } }
private fun calculateAge(timestamp: Long): Int { val birth = java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate(); return Period.between(birth, LocalDate.now()).years }
