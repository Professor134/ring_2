package com.example.ringapp.ui.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
    
    val textColor = Color.White
    val bgColor = Color.Black

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 40.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { ProfileAvatar(profile); Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(profile.name.ifBlank { "Your name" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = textColor); Text("Level ${state.level}", color = textColor); Text("${state.points} Elite Points", color = MaterialTheme.colorScheme.primary) }; Button(onClick = onEdit) { Text("Edit Profile") } } }
            item { 
                Card { 
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { 
                        Text("Level ${state.level}", style = MaterialTheme.typography.titleLarge)
                        val level = state.level
                        val currentLevelTotal = totalThreshold(level)
                        val prevLevelTotal = totalThreshold(level - 1)
                        val pointsInCurrentLevel = state.lifetimePoints - prevLevelTotal
                        val pointsNeededForNext = currentLevelTotal - state.lifetimePoints
                        
                        val progress = if (currentLevelTotal > prevLevelTotal) {
                            (pointsInCurrentLevel.toFloat() / (currentLevelTotal - prevLevelTotal)).coerceIn(0f, 1f)
                        } else 1f
                        
                        LinearProgressIndicator({ progress }, Modifier.fillMaxWidth())
                        Text("${state.lifetimePoints} / $currentLevelTotal lifetime points", fontWeight = FontWeight.Bold)
                        Text("$pointsNeededForNext points needed for Level ${level + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Level depends on lifetime earned Elite Points and never decreases when current points are spent.", style = MaterialTheme.typography.bodySmall) 
                    } 
                } 
            }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { StatCard("Current Streak", state.currentStreak.toString(), Modifier.weight(1f)); StatCard("Best Streak", state.bestStreak.toString(), Modifier.weight(1f)) }; Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { StatCard("Total Habits", state.totalHabits.toString(), Modifier.weight(1f)); StatCard("Completed Tasks", state.completedTasks.toString(), Modifier.weight(1f)) } }
            item { Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor) }
            items(state.achievements) { achievement ->
                Card {
                    ListItem(
                        leadingContent = { Icon(if (achievement.unlocked) Icons.Default.Star else Icons.Default.Lock, null, tint = if (achievement.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                        headlineContent = { Text(achievement.description) },
                        supportingContent = { Text(if (achievement.unlocked) "Unlocked" else "${achievement.currentProgress} / ${achievement.threshold}") }
                    )
                }
            }
            item { ActionButton("Elite Point History", Icons.Default.List, onPointHistory, textColor) }
            item { Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor) }
            item { ActionButton("Appearance", Icons.Default.Settings, onAppearance, textColor) }
            item { ActionButton("Notifications", Icons.Default.Notifications, onNotifications, textColor) }
            item { ActionButton("Backup & Restore", Icons.Default.CloudUpload, onBackup, textColor) }
            item { Text("Theme: ${appViewModel.state.collectAsStateWithLifecycle().value.theme}", color = textColor.copy(alpha = 0.6f)) }
            item { Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("RING", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = textColor); Text("Offline Habit & Productivity Tracker", color = textColor.copy(alpha = 0.6f)); Text("Version 1.0", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.4f)) } }
        }
    }
}

@Composable
private fun ProfileAvatar(profile: ProfileEntity) { val uri = profile.photoUri?.let(Uri::parse); Surface(Modifier.size(92.dp).clip(CircleShape), color = Color(profile.avatarColor), border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) { if (uri != null) AsyncImage(model = uri, contentDescription = "Profile photo", modifier = Modifier.fillMaxSize(), onError = {}) else Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, "Default avatar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(52.dp)) } } }
@Composable private fun StatCard(label: String, value: String, modifier: Modifier) { Card(modifier) { Column(Modifier.padding(14.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } } }
@Composable private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, textColor: Color) { OutlinedButton(onClick = onClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(icon, null, tint = textColor); Spacer(Modifier.width(10.dp)); Text(label, color = textColor) } }
private fun calculateLevel(points: Int): Int { 
    var level = 1
    while (points >= totalThreshold(level) && level < 1000) level++
    return level 
}

private fun neededForLevel(level: Int): Int {
    var needed = 100.0
    repeat(level - 1) { needed *= 1.25 }
    return needed.toInt()
}

private fun totalThreshold(level: Int): Int {
    var total = 0
    for (i in 1..level) {
        total += neededForLevel(i)
    }
    return total
}

@Composable
fun EditProfileScreen(onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); val profile = state.profile ?: return; val context = LocalContext.current
    var name by remember(profile) { mutableStateOf(profile.name) }; var photoUri by remember(profile) { mutableStateOf(profile.photoUri) }; var dob by remember(profile) { mutableStateOf(profile.dateOfBirth) }; var gender by remember(profile) { mutableStateOf(profile.gender.orEmpty()) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION); photoUri = it.toString() } }
    
    val textColor = Color.White
    val bgColor = Color.Black

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                Text("Edit Profile", style = MaterialTheme.typography.headlineSmall, color = textColor)
                TextButton(onClick = { if (name.isNotBlank()) { viewModel.save(profile.copy(name = name, photoUri = photoUri, dateOfBirth = dob, gender = gender.ifBlank { null })); onBack() } }) { Text("Save", color = MaterialTheme.colorScheme.primary) }
            }
            Surface(Modifier.size(150.dp).align(Alignment.CenterHorizontally).clip(CircleShape), color = Color(profile.avatarColor), border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) { if (photoUri != null) AsyncImage(Uri.parse(photoUri), "Profile photo", Modifier.fillMaxSize()) else Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, "Default avatar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(82.dp)) } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onClick = { picker.launch(arrayOf("image/*")) }, Modifier.weight(1f)) { Text("Change Photo") }; OutlinedButton(onClick = { photoUri = null }, Modifier.weight(1f), enabled = photoUri != null) { Text("Remove Photo", color = textColor) } }
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true, isError = name.isBlank(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
            Text("Date of Birth", style = MaterialTheme.typography.titleMedium, color = textColor); OutlinedButton(onClick = { val now = Calendar.getInstance(); DatePickerDialog(context, { _, year, month, day -> dob = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }.timeInMillis }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show() }, Modifier.fillMaxWidth()) { Text(dob?.let { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Choose date", color = textColor) }
            Text("Age: ${dob?.let(::calculateAge) ?: "Not set"}", color = textColor.copy(alpha = 0.6f))
            Text("Gender", style = MaterialTheme.typography.titleMedium, color = textColor); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("Male", "Female").forEach { option -> FilterChip(gender == option, { gender = option }, label = { Text(option) }, colors = FilterChipDefaults.filterChipColors(labelColor = textColor, selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)) } }
            Spacer(Modifier.height(64.dp))
        }
    }
}

@Composable fun PointHistoryScreen(onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) { 
    val state by viewModel.state.collectAsStateWithLifecycle()
    val textColor = Color.White
    val bgColor = Color.Black

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 40.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { 
            item { 
                Row(verticalAlignment = Alignment.CenterVertically) { 
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                    Text("Elite Point History", style = MaterialTheme.typography.headlineSmall, color = textColor) 
                }
                Text("Current balance: ${state.points}  •  Lifetime earned: ${state.lifetimePoints}", color = textColor.copy(alpha = 0.6f)) 
            }
            items(state.transactions) { transaction -> 
                Card(colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surfaceVariant)) {
                    ListItem(
                        headlineContent = { Text(transaction.description, color = textColor) }, 
                        supportingContent = { Text(SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(transaction.timestamp)), color = textColor.copy(alpha = 0.5f)) }, 
                        trailingContent = { Text(if (transaction.amount >= 0) "+${transaction.amount}" else transaction.amount.toString(), color = if (transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            } 
        } 
    }
}

@Composable fun ProfileInfoScreen(title: String, description: String, onBack: () -> Unit) { 
    val textColor = Color.White
    val bgColor = Color.Black

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Column(Modifier.fillMaxSize().padding(20.dp)) { 
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { 
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                Text(title, style = MaterialTheme.typography.headlineSmall, color = textColor) 
            }
            Spacer(Modifier.height(16.dp))
            Card { Text(description, Modifier.padding(20.dp)) }
        } 
    }
}
private fun calculateAge(timestamp: Long): Int { val birth = java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate(); return Period.between(birth, LocalDate.now()).years }
