package com.example.ring_2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val habits by viewModel.allHabits.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("ELITE", fontSize = 10.sp, color = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text("${profile?.elitePoints ?: 0}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(profile?.avatarColor ?: 0xFF00E676.toInt()), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(profile?.name?.take(1)?.uppercase() ?: "P", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile?.name?.ifEmpty { "User" } ?: "User", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Set your DOB", color = Color.Gray, fontSize = 14.sp)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00E676))
                        }
                    }
                }
            }

            item {
                LevelCard(profile?.level ?: 1, profile?.accumulatedPoints ?: 0)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileStatCard(Modifier.weight(1f), "${profile?.elitePoints ?: 0}", "Elite Points")
                    ProfileStatCard(Modifier.weight(1f), "12", "Best streak")
                    ProfileStatCard(Modifier.weight(1f), "${habits.size}", "Habits")
                }
            }

            item {
                SectionHeader("Achievements", onSeeAll = {})
                AchievementGrid()
            }
            
            item {
                SectionHeader("Elite Point rules", onSeeAll = {})
                ElitePointRules()
            }
        }
    }
}

@Composable
fun LevelCard(level: Int, accumulatedPoints: Int) {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Level $level", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { 0.1f }, // Dummy progress
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFF00E676),
                trackColor = Color(0xFF333333)
            )
            Spacer(Modifier.height(8.dp))
            Text("25 / 338 points to Level ${level + 1}", fontSize = 12.sp, color = Color.Gray)
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
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AchievementGrid() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AchievementCard(Modifier.weight(1f), "First Step", "Complete your first habit", isUnlocked = false)
        AchievementCard(Modifier.weight(1f), "Consistent", "Complete a habit for 7 days", isUnlocked = true)
    }
}

@Composable
fun AchievementCard(modifier: Modifier, title: String, desc: String, isUnlocked: Boolean) {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(120.dp).then(if (!isUnlocked) Modifier.alpha(0.5f) else Modifier)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Bottom) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Text(desc, fontSize = 10.sp, color = Color.Gray, lineHeight = 12.sp)
        }
    }
}

@Composable
fun ElitePointRules() {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RuleRow("Starting points", "+500")
            RuleRow("Create habit", "-25")
            RuleRow("Complete habit", "+4")
            RuleRow("10-day streak", "+8")
            RuleRow("20-day streak", "+15")
        }
    }
}

@Composable
fun RuleRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, color = if (value.startsWith("+")) Color(0xFF00E676) else Color.Red)
    }
}
