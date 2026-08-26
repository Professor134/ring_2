package com.example.ring_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ring_2.data.model.Habit
import com.example.ring_2.data.model.HabitSchedule
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(HabitType.YES_NO) }
    var targetValue by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("New Habit", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("HABIT NAME", fontSize = 12.sp, color = Color.Gray)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Morning walk", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Text("DESCRIPTION (OPTIONAL)", fontSize = 12.sp, color = Color.Gray)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note", color = Color.Gray) }
            )

            Text("TYPE", fontSize = 12.sp, color = Color.Gray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeButton(Modifier.weight(1f), "Yes / No", selectedType == HabitType.YES_NO) { selectedType = HabitType.YES_NO }
                TypeButton(Modifier.weight(1f), "Measurable", selectedType == HabitType.MEASURABLE) { selectedType = HabitType.MEASURABLE }
            }

            if (selectedType == HabitType.MEASURABLE) {
                Text("TARGET", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(value = targetValue, onValueChange = { targetValue = it }, modifier = Modifier.fillMaxWidth())
                Text("UNIT", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(value = unit, onValueChange = { unit = it }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.weight(1f))
            
            Text("Creating this habit will cost 25 Elite Points.", color = Color(0xFF00E676), fontSize = 14.sp)
            
            Button(
                onClick = {
                    viewModel.addHabit(Habit(
                        name = name,
                        description = description,
                        type = selectedType,
                        targetValue = targetValue.toDoubleOrNull() ?: 1.0,
                        unit = unit,
                        schedule = HabitSchedule.Daily,
                        startDate = System.currentTimeMillis(),
                        icon = "default",
                        categoryId = 1,
                        color = 0xFF00E676.toInt()
                    ))
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Habit -25", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TypeButton(modifier: Modifier, text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF00E676) else Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = if (isSelected) Color.Black else Color.Gray)
    }
}
