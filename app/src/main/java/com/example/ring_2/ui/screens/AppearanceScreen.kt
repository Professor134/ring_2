package com.example.ring_2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val currentTheme = profile?.themePreference ?: "System"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Appearance", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("CHOOSE THEME", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ThemeOptionItem("Dark", "Standard dark theme", currentTheme == "Dark") {
                        viewModel.updateThemePreference("Dark")
                    }
                    ThemeOptionItem("Light", "Clean light theme", currentTheme == "Light") {
                        viewModel.updateThemePreference("Light")
                    }
                    ThemeOptionItem("System", "Follow system settings", currentTheme == "System") {
                        viewModel.updateThemePreference("System")
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            Text("Preview", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            // Just a preview card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFF00E676), RoundedCornerShape(8.dp)))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("This is how it looks", fontWeight = FontWeight.Bold)
                        Text("Adaptive colors are used everywhere.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionItem(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E676))
            )
        }
    }
}
