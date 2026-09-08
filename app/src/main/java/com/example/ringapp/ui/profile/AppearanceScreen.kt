package com.example.ringapp.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.ui.AppViewModel

@Composable
fun AppearanceScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Appearance", style = MaterialTheme.typography.headlineMedium)
        Text("Theme", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("SYSTEM", "LIGHT", "DARK").forEach { theme -> FilterChip(selected = state.theme == theme, onClick = { viewModel.setTheme(theme) }, label = { Text(theme.lowercase().replaceFirstChar { it.uppercase() }) }) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Dynamic color"); Switch(checked = state.dynamicColor, onCheckedChange = { viewModel.setDynamicColor(it) }) }
        Text("Preview", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("Primary", color = MaterialTheme.colorScheme.primary); Text("Surface", color = MaterialTheme.colorScheme.onSurface); Text("Button", color = MaterialTheme.colorScheme.primary) }
    }
}