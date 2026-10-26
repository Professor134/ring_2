package com.example.ringapp.ui.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.ui.AppViewModel
import com.example.ringapp.ui.theme.CyberNeonGreen
import com.example.ringapp.ui.theme.SpaceBlack

@Composable
fun AppearanceScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "THEME ENGINE", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Select your visual interface", 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(Modifier.height(48.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ThemeToggleButton(
                selected = state.theme == "LIGHT",
                label = "LIGHT PROTOCOL",
                icon = Icons.Default.LightMode,
                onClick = { viewModel.setTheme("LIGHT") }
            )
            
            ThemeToggleButton(
                selected = state.theme == "DARK",
                label = "DARK PROTOCOL",
                icon = Icons.Default.DarkMode,
                onClick = { viewModel.setTheme("DARK") }
            )
        }
        
        Spacer(Modifier.height(40.dp))
        
        // Preview Card
        Card(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, CyberNeonGreen.copy(alpha = 0.2f))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "PREVIEW ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ThemeToggleButton(selected: Boolean, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (selected) SpaceBlack else MaterialTheme.colorScheme.onSurface
    
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = contentColor, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(16.dp))
                Text(label, fontWeight = FontWeight.Black, color = contentColor, letterSpacing = 1.sp)
            }
            if (selected) {
                Icon(Icons.Default.Check, null, tint = contentColor)
            }
        }
    }
}
