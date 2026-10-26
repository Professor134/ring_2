package com.example.ringapp.ui.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.usecase.*
import com.example.ringapp.ui.AppViewModel
import com.example.ringapp.ui.theme.CyberNeonGreen
import com.example.ringapp.ui.theme.SpaceBlack
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile ?: return
    
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp), 
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp), 
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { 
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                    ProfileAvatar(profile)
                    Column(Modifier.weight(1f).padding(horizontal = 16.dp)) { 
                        Text(profile.name.ifBlank { "NEURAL SUBJECT" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 1.sp)
                        Text("LEVEL ${state.level}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                        Text("${state.points} ELITE POINTS", color = textColor.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) 
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)) { Icon(Icons.Default.Edit, "Edit", tint = SpaceBlack) } 
                } 
            }
            
            item { 
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
                ) { 
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { 
                        Text("SYNC PROGRESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.5f), letterSpacing = 2.sp)
                        val level = state.level
                        val currentLevelTotal = totalThreshold(level)
                        val prevLevelTotal = totalThreshold(level - 1)
                        val pointsInCurrentLevel = state.lifetimePoints - prevLevelTotal
                        
                        val progress = if (currentLevelTotal > prevLevelTotal) {
                            (pointsInCurrentLevel.toFloat() / (currentLevelTotal - prevLevelTotal)).coerceIn(0f, 1f)
                        } else 1f
                        
                        LinearProgressIndicator(
                            progress = { progress }, 
                            Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${state.lifetimePoints} XP", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
                            Text("NEXT: $currentLevelTotal XP", color = textColor.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    } 
                } 
            }
            
            item { 
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                    StatCard("STREAK", state.currentStreak.toString(), Modifier.weight(1f), isDark)
                    StatCard("BEST", state.bestStreak.toString(), Modifier.weight(1f), isDark) 
                }
            }
            
            item { Text("ACHIEVEMENTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.5f), letterSpacing = 2.sp) }
            
            items(state.achievements) { achievement ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161920) else Color.White),
                    border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
                ) {
                    ListItem(
                        leadingContent = { 
                            Surface(
                                color = if (achievement.unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else textColor.copy(alpha = 0.05f),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (achievement.unlocked) Icons.Default.Star else Icons.Default.Lock, 
                                        null, 
                                        tint = if (achievement.unlocked) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.2f),
                                        modifier = Modifier.size(20.dp)
                                    ) 
                                }
                            }
                        },
                        headlineContent = { Text(achievement.description.uppercase(), fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, color = textColor) },
                        supportingContent = { Text(if (achievement.unlocked) "ENCRYPTED & SYNCED" else "LOCKED: ${achievement.currentProgress} / ${achievement.threshold}", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            
            item { Text("SYSTEM CONFIG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.5f), letterSpacing = 2.sp) }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Visual Protocol", Icons.Default.Palette, onAppearance, textColor)
                    ActionButton("Neural Alerts", Icons.Default.Notifications, onNotifications, textColor)
                    ActionButton("Cloud Synchronization", Icons.Default.CloudUpload, onBackup, textColor)
                    ActionButton("Transaction Logs", Icons.Default.History, onPointHistory, textColor)
                }
            }
            
            item { 
                Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) { 
                    Text("RING", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 4.sp)
                    Text("NEURAL PRODUCTIVITY INTERFACE", color = textColor.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                } 
            }
        }
    }
}

@Composable
private fun ProfileAvatar(profile: ProfileEntity) { 
    val uri = profile.photoUri?.let(Uri::parse)
    Surface(
        Modifier.size(80.dp).clip(CircleShape), 
        color = Color(profile.avatarColor), 
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) { 
        if (uri != null) AsyncImage(model = uri, contentDescription = "Profile photo", modifier = Modifier.fillMaxSize(), onError = {}) 
        else Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = SpaceBlack, modifier = Modifier.size(40.dp)) } 
    } 
}

@Composable private fun StatCard(label: String, value: String, modifier: Modifier, isDark: Boolean) { 
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161920) else Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) { 
        Column(Modifier.padding(16.dp)) { 
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Black); 
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) 
        } 
    } 
}

@Composable private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, textColor: Color) { 
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
    ) { 
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) { 
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(label.uppercase(), color = textColor, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp) 
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = textColor.copy(alpha = 0.2f))
        } 
    } 
}

private fun totalThreshold(level: Int): Int {
    var total = 0
    for (i in 1..level) {
        var needed = 100.0
        repeat(i - 1) { needed *= 1.25 }
        total += needed.toInt()
    }
    return total
}

@Composable
fun EditProfileScreen(onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); val profile = state.profile ?: return; val context = LocalContext.current
    var name by remember(profile) { mutableStateOf(profile.name) }; var photoUri by remember(profile) { mutableStateOf(profile.photoUri) }; var dob by remember(profile) { mutableStateOf(profile.dateOfBirth) }; var gender by remember(profile) { mutableStateOf(profile.gender.orEmpty()) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION); photoUri = it.toString() } }
    
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                Text("PROFILE CONFIG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 2.sp)
                TextButton(onClick = { if (name.isNotBlank()) { viewModel.save(profile.copy(name = name, photoUri = photoUri, dateOfBirth = dob, gender = gender.ifBlank { null })); onBack() } }) { Text("SYNC", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) }
            }
            Surface(Modifier.size(120.dp).align(Alignment.CenterHorizontally).clip(CircleShape), color = Color(profile.avatarColor), border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) { if (photoUri != null) AsyncImage(Uri.parse(photoUri), "Profile photo", Modifier.fillMaxSize()) else Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = SpaceBlack, modifier = Modifier.size(60.dp)) } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = { picker.launch(arrayOf("image/*")) }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("UPDATE") }; OutlinedButton(onClick = { photoUri = null }, Modifier.weight(1f), enabled = photoUri != null, shape = RoundedCornerShape(12.dp)) { Text("REMOVE", color = textColor) } }
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("NAME") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor))
            Text("DATA OF BIRTH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.5f), letterSpacing = 2.sp); OutlinedButton(onClick = { val now = Calendar.getInstance(); DatePickerDialog(context, { _, year, month, day -> dob = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }.timeInMillis }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show() }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text(dob?.let { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "NOT SET", color = textColor) }
            Text("GENDER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = textColor.copy(alpha = 0.5f), letterSpacing = 2.sp); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Male", "Female", "Other").forEach { option -> FilterChip(gender == option, { gender = option }, label = { Text(option.uppercase()) }, shape = RoundedCornerShape(10.dp), colors = FilterChipDefaults.filterChipColors(labelColor = textColor.copy(alpha = 0.6f), selectedLabelColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary)) } }
            Spacer(Modifier.height(64.dp))
        }
    }
}

@Composable fun PointHistoryScreen(onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) { 
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { 
            item { 
                Row(verticalAlignment = Alignment.CenterVertically) { 
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                    Text("TRANSACTION LOGS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 2.sp) 
                }
            }
            items(state.transactions) { transaction -> 
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161920) else Color.White),
                    border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
                ) {
                    ListItem(
                        headlineContent = { Text(transaction.description.uppercase(), color = textColor, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium) }, 
                        supportingContent = { Text(SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(transaction.timestamp)), color = textColor.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall) }, 
                        trailingContent = { Text(if (transaction.amount >= 0) "+${transaction.amount}" else transaction.amount.toString(), color = if (transaction.amount >= 0) CyberNeonGreen else Color.Red, fontWeight = FontWeight.Black) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            } 
        } 
    }
}

@Composable fun ProfileInfoScreen(title: String, description: String, onBack: () -> Unit) { 
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor, contentColor = textColor) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) { 
            Row(verticalAlignment = Alignment.CenterVertically) { 
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                Text(title.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 2.sp) 
            }
            Spacer(Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161920) else Color.White),
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.05f))
            ) { 
                Text(description, Modifier.padding(20.dp), color = textColor.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyLarge) 
            }
        } 
    }
}
