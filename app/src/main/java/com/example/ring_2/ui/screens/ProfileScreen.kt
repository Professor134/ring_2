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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Bold) },
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
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(profile?.avatarColor ?: 0xFF00E676.toInt()), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile?.photoUri != null) {
                            // In a real app, use Coil to load photoUri
                            Text(profile?.name?.take(1)?.uppercase() ?: "P", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Black)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(profile?.name?.ifEmpty { "User" } ?: "User", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                AchievementRow()
            }

            // SECTION 4: ELITE POINT HISTORY
            item {
                Button(
                    onClick = onPointHistory,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color(0xFF00E676))
                        Spacer(Modifier.width(12.dp))
                        Text("Elite Point History", color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            // SECTION 5 & 6: SETTINGS & THEME
            item {
                SectionHeader("Settings")
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsItem("Appearance", Icons.Default.Settings, onAppearance)
                    SettingsItem("Notifications", Icons.Default.Notifications, onNotifications)
                    SettingsItem("Backup & Restore", Icons.Default.Refresh, onBackupRestore)
                }
            }

            item {
                ThemeSelection()
            }

            // SECTION 7: DATA
            item {
                SectionHeader("Data")
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataButton("Export Backup", Icons.Default.Share, Color.White) {}
                    DataButton("Import Backup", Icons.Default.AddCircle, Color.White) {}
                    DataButton("Clear All Data", Icons.Default.Delete, Color.Red) {
                        viewModel.clearAllData()
                    }
                }
            }

            // SECTION 8: ABOUT
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RING", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    Text("Offline Habit & Productivity Tracker", color = Color.Gray, fontSize = 12.sp)
                    Text("Version 1.0.0", color = Color.DarkGray, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun LevelCard(level: Int, lifetimeEarned: Int) {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Level Progress", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
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
                trackColor = Color(0xFF333333)
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
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AchievementRow() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item { AchievementCard("First Habit", "Create your first habit", Icons.Default.Star, isUnlocked = true) }
        item { AchievementCard("Consistent", "7-Day Streak achieved", Icons.Default.ThumbUp, isUnlocked = false) }
        item { AchievementCard("Elite", "Earn 1000 Points", Icons.Default.Favorite, isUnlocked = false) }
    }
}

@Composable
fun AchievementCard(title: String, desc: String, icon: ImageVector, isUnlocked: Boolean) {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(width = 140.dp, height = 140.dp).then(if (!isUnlocked) Modifier.alpha(0.4f) else Modifier)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (isUnlocked) Color(0xFF00E676) else Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(desc, fontSize = 10.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
fun SettingsItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.DarkGray)
        }
    }
}

@Composable
fun ThemeSelection() {
    var selectedTheme by remember { mutableStateOf("Dark") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("THEME", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Dark", "Light", "System").forEach { theme ->
                val isSelected = selectedTheme == theme
                Button(
                    onClick = { selectedTheme = theme },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF00E676) else Color(0xFF1E1E1E)),
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
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = color, fontSize = 14.sp)
        }
    }
}
