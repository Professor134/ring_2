package com.example.ring_2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    val profile by viewModel.userProfile.collectAsState()
    
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var selectedColor by remember { mutableIntStateOf(profile?.avatarColor ?: 0xFF00E676.toInt()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            profile?.let {
                                viewModel.updateProfile(it.copy(name = name, avatarColor = selectedColor))
                            }
                            onBack()
                        }
                    ) {
                        Text("Save", color = Color(0xFF00E676))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Photo / Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(selectedColor), CircleShape)
                    .clickable { /* Choose photo */ },
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Text("Tap to change photo", color = Color.Gray, fontSize = 12.sp)

            // Name Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NAME", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E676)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Avatar Color Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AVATAR COLOR", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val colors = listOf(0xFF00E676, 0xFFFF5252, 0xFF448AFF, 0xFFFFAB40, 0xFFE040FB)
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(color.toInt()), CircleShape)
                                .clip(CircleShape)
                                .clickable { selectedColor = color.toInt() }
                                .then(if (selectedColor == color.toInt()) Modifier.background(Color.White.copy(alpha = 0.3f)) else Modifier)
                        )
                    }
                }
            }
        }
    }
}
