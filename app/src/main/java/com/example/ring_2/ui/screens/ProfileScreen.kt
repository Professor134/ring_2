package com.example.ring_2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.components.ProfileAvatar
import com.example.ring_2.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onEditProfile: () -> Unit,
    onPointHistory: () -> Unit,
    onAppearance: () -> Unit,
    onNotifications: () -> Unit,
    onBackupRestore: () -> Unit,
) {
    val profile by viewModel.userProfile.collectAsState()
    val userProg by viewModel.userProgress.collectAsState()
    val profileStats by viewModel.profileStats.collectAsState()
    
    var showClearDataConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFF00E676))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // TOP PROFILE AREA
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileAvatar(
                        name = profile?.name?.ifEmpty { "User" } ?: "User",
                        avatarColor = Color(profile?.avatarColor ?: 0xFF00E676.toInt()),
                        photoUri = profile?.photoUri
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(profile?.name?.ifEmpty { "User" } ?: "User", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Level ${userProg?.level ?: 1}", color = Color(0xFF00E676), fontWeight = FontWeight.Medium)
                    Text("${userProg?.currentPoints ?: 0} Elite Points", color = Color.Gray, fontSize = 14.sp)
                }
            }

            // SECTION 1: LEVEL
            item {
                LevelCard(userProg?.level ?: 1, userProg?.lifetimeEarnedPoints ?: 0)
            }

            // SECTION 2: STATISTICS
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileStatCard(Modifier.weight(1f), (profileStats["currentStreak"] ?: 0).toString(), "Current Streak")
                    ProfileStatCard(Modifier.weight(1f), (profileStats["bestStreak"] ?: 0).toString(), "Best Streak")
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileStatCard(Modifier.weight(1f), (profileStats["totalHabits"] ?: 0).toString(), "Total Habits")
                    ProfileStatCard(Modifier.weight(1f), (profileStats["completedTasks"] ?: 0).toString(), "Completed Tasks")
                }
            }

            // SECTION 3: ACHIEVEMENTS
            item {
                SectionHeader("Achievements")
                Spacer(Modifier.height(12.dp))
                AchievementRow(profileStats, userProg?.lifetimeEarnedPoints ?: 0)
            }

            // SECTION 4: ELITE POINT HISTORY
            item {
                Button(
                    onClick = onPointHistory,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color(0xFF00E676))
                        Spacer(Modifier.width(12.dp))
                        Text("Elite Point History", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsItem("Appearance", Icons.Default.Settings, onAppearance)
                    SettingsItem("Notifications", Icons.Default.Notifications, onNotifications)
                    SettingsItem("Data Management", Icons.Default.Info, onBackupRestore)
                }
            }

            // SECTION 7: DATA
            item {
                SectionHeader("Data Management")
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataButton("Manage Import/Export", Icons.Default.Share, MaterialTheme.colorScheme.onSurface) {
                        onBackupRestore()
                    }
                    DataButton("Clear All Data", Icons.Default.Delete, Color.Red) {
                        showClearDataConfirmation = true
                    }
                }
            }

            // SECTION 8: ABOUT
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RING", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("Offline Habit & Productivity Tracker", color = Color.Gray, fontSize = 12.sp)
                    Text("Version 1.2.0", color = Color.DarkGray, fontSize = 10.sp)
                }
            }
        }
    }
    
    if (showClearDataConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmation = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all your habits, tasks, and point history. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showClearDataConfirmation = false
                }) {
                    Text("Clear Everything", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LevelCard(level: Int, lifetimeEarned: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Level Progress", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lv $level", fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                Spacer(Modifier.weight(1f))
                Text("Lv ${level + 1}", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            
            val progress = (lifetimeEarned % 100).toFloat() / 100f
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFF00E676),
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Level depends on lifetime earned Elite Points. Your level never decreases.",
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun ProfileStatCard(modifier: Modifier, value: String, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AchievementRow(stats: Map<String, Int>, lifetimePoints: Int) {
    val streak = stats["bestStreak"] ?: 0
    val totalHabits = stats["totalHabits"] ?: 0
    
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        com.example.ring_2.logic.GamificationEngine.ACHIEVEMENTS.forEach { tier ->
            val isUnlocked = when(tier.category) {
                "Streak" -> streak >= tier.requirement
                "Points" -> lifetimePoints >= tier.requirement
                "Habits" -> totalHabits >= tier.requirement
                else -> false
            }
            item {
                AchievementCard(
                    title = tier.title,
                    desc = "${tier.requirement} ${tier.category}",
                    icon = when(tier.level) {
                        com.example.ring_2.logic.GamificationEngine.AchievementLevel.BRONZE -> Icons.Default.Star
                        com.example.ring_2.logic.GamificationEngine.AchievementLevel.SILVER -> Icons.Default.ThumbUp
                        com.example.ring_2.logic.GamificationEngine.AchievementLevel.GOLD -> Icons.Default.Favorite
                    },
                    levelColor = when(tier.level) {
                        com.example.ring_2.logic.GamificationEngine.AchievementLevel.BRONZE -> Color(0xFFCD7F32)
                        com.example.ring_2.logic.GamificationEngine.AchievementLevel.SILVER -> Color(0xFFC0C0C0)
                        com.example.ring_2.logic.GamificationEngine.AchievementLevel.GOLD -> Color(0xFFFFD700)
                    },
                    isUnlocked = isUnlocked
                )
            }
        }
    }
}

@Composable
fun AchievementCard(title: String, desc: String, icon: ImageVector, levelColor: Color, isUnlocked: Boolean) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(width = 140.dp, height = 140.dp).then(if (!isUnlocked) Modifier.alpha(0.4f) else Modifier),
        border = if (isUnlocked) androidx.compose.foundation.BorderStroke(1.dp, levelColor) else null
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (isUnlocked) levelColor else Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(desc, fontSize = 10.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
fun SettingsItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.DarkGray)
        }
    }
}

@Composable
fun ThemeSelection(currentTheme: String, onThemeChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("THEME", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Dark", "Light", "System").forEach { theme ->
                val isSelected = currentTheme == theme
                Button(
                    onClick = { onThemeChange(theme) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF00E676) else MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(theme, color = if (isSelected) Color.Black else Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DataButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = color, fontSize = 14.sp)
        }
    }
}
