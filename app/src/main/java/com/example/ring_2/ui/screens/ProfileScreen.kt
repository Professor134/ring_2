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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import com.example.ring_2.R
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
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
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
                        avatarColor = Color(profile?.avatarColor ?: MaterialTheme.colorScheme.primary.toArgb()),
                        photoUri = profile?.photoUri
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(profile?.name?.ifEmpty { "User" } ?: "User", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Level ${userProg?.level ?: 1}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    Text("${userProg?.currentPoints ?: 0} Elite Points", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }

            // SECTION 1: LEVEL
            item {
                LevelCard(userProg?.lifetimeEarnedPoints ?: 0)
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
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Elite Point History", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsItem("Appearance", Icons.Default.Settings, onAppearance)
                    SettingsItem("Notifications", Icons.Default.Notifications, onNotifications)
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
                    DataButton("Clear All Data", Icons.Default.Delete, MaterialTheme.colorScheme.error) {
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
                    Text("Offline Habit & Productivity Tracker", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("Version 1.2.0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp)
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
                    Text("Clear Everything", color = MaterialTheme.colorScheme.error)
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
fun LevelCard(lifetimeEarned: Int) {
    val (lvl, currentInLevel, totalInLevel, progress) = com.example.ring_2.logic.GamificationEngine.getLevelProgress(lifetimeEarned)
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Level Progress", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lv $lvl", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                Text("Lv ${lvl + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "$currentInLevel / $totalInLevel",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Level depends on lifetime earned Elite Points. Your level never decreases.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AchievementRow(stats: Map<String, Int>, lifetimePoints: Int) {
    val streak = stats["currentStreak"] ?: 0
    val bestStreak = stats["bestStreak"] ?: 0
    val totalHabits = stats["totalHabits"] ?: 0
    val completedTasks = stats["completedTasks"] ?: 0
    val (lvl, _, _, _) = com.example.ring_2.logic.GamificationEngine.getLevelProgress(lifetimePoints)
    
    // Derived stats for achievements
    val categories = listOf("Streak", "CreateHabit", "CompleteHabit", "CreateTask", "CompleteTask", "Points", "Level")
    
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { category ->
            val currentValue = when(category) {
                "Streak" -> bestStreak
                "CreateHabit" -> totalHabits
                "CompleteHabit" -> stats["totalHabits"] ?: 0 // Simplified: using total completions from stats if available
                "CreateTask" -> stats["completedTasks"] ?: 0 // Simplified
                "CompleteTask" -> completedTasks
                "Points" -> lifetimePoints
                "Level" -> lvl
                else -> 0
            }
            
            val tiers = com.example.ring_2.logic.GamificationEngine.ACHIEVEMENTS_MAP[category] ?: emptyList()
            
            // Find highest unlocked tier
            var currentTier = tiers.first()
            var unlockedLevel: com.example.ring_2.logic.GamificationEngine.AchievementLevel? = null
            
            for (tier in tiers) {
                if (currentValue >= tier.requirement) {
                    currentTier = tier
                    unlockedLevel = tier.level
                } else {
                    // This is the next target
                    if (unlockedLevel == null) {
                        currentTier = tier
                    } else {
                        // We found the next one after the last unlocked
                        currentTier = tier
                    }
                    break
                }
            }

            val isFullyCompleted = currentValue >= tiers.last().requirement
            if (isFullyCompleted) currentTier = tiers.last()

            item {
                AchievementCard(
                    title = currentTier.title,
                    desc = currentTier.description,
                    icon = when(currentTier.category) {
                        "Streak" -> Icons.Default.Star
                        "Points" -> Icons.Default.CheckCircle
                        "Level" -> Icons.Default.Person
                        else -> Icons.Default.ThumbUp
                    },
                    level = unlockedLevel,
                    isUnlocked = unlockedLevel != null
                )
            }
        }
    }
}

@Composable
fun AchievementCard(title: String, desc: String, icon: ImageVector, level: com.example.ring_2.logic.GamificationEngine.AchievementLevel?, isUnlocked: Boolean) {
    val levelColor = when(level) {
        com.example.ring_2.logic.GamificationEngine.AchievementLevel.BRONZE -> colorResource(R.color.achievement_bronze)
        com.example.ring_2.logic.GamificationEngine.AchievementLevel.SILVER -> colorResource(R.color.achievement_silver)
        com.example.ring_2.logic.GamificationEngine.AchievementLevel.GOLD -> colorResource(R.color.achievement_gold)
        null -> Color.Gray
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(width = 140.dp, height = 140.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, levelColor.copy(alpha = if (isUnlocked) 1f else 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (isUnlocked) levelColor else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 12.sp)
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
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DataButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = color, fontSize = 14.sp)
        }
    }
}
