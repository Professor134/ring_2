// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\habits\AddHabitScreen.kt
// Purpose: Screen for creating and editing habits following exact field order and symmetric/asymmetric target configurations.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.habits

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ringapp.data.local.entities.CategoryConstants
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitType
import com.example.ringapp.data.local.entities.ScheduleType
import com.example.ringapp.domain.usecase.CreateHabitUseCase
import com.example.ringapp.domain.usecase.DeleteHabitUseCase
import com.example.ringapp.domain.usecase.ObserveCategoriesUseCase
import com.example.ringapp.domain.usecase.ObserveHabitsUseCase
import com.example.ringapp.domain.usecase.UpdateHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    observeHabits: ObserveHabitsUseCase,
    observeCategories: ObserveCategoriesUseCase,
    private val createHabit: CreateHabitUseCase,
    private val updateHabit: UpdateHabitUseCase,
    private val deleteHabit: DeleteHabitUseCase
) : ViewModel() {
    val habits = observeHabits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(habit: HabitEntity?, fields: HabitFields, onSaved: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        try {
            if (habit == null) {
                createHabit(
                    name = fields.name,
                    description = fields.description,
                    categoryId = fields.categoryId,
                    type = fields.type,
                    target = fields.target,
                    sets = fields.sets,
                    repsPerSet = fields.repsPerSet,
                    symmetric = fields.symmetric,
                    weeklyTargets = fields.weeklyTargets,
                    unit = fields.unit,
                    scheduleType = fields.schedule,
                    scheduleDays = fields.days,
                    startDate = fields.startDate
                )
            } else {
                updateHabit(
                    habit.copy(
                        name = fields.name,
                        description = fields.description.ifBlank { null },
                        categoryId = fields.categoryId,
                        type = fields.type,
                        target = fields.target,
                        sets = fields.sets,
                        repsPerSet = fields.repsPerSet,
                        symmetric = fields.symmetric,
                        weeklyTargets = fields.weeklyTargets,
                        unit = fields.unit.ifBlank { null },
                        scheduleType = fields.schedule,
                        scheduleDays = fields.days,
                        startDate = fields.startDate,
                        color = fields.color,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            onSaved()
        } catch (error: Exception) {
            onError(error.message ?: "Unable to save habit")
        }
    }

    fun delete(habitId: Long, onDeleted: () -> Unit) = viewModelScope.launch {
        deleteHabit(habitId)
        onDeleted()
    }
}

data class HabitFields(
    val name: String,
    val description: String,
    val categoryId: Long?,
    val type: HabitType,
    val target: Double,
    val sets: Int,
    val repsPerSet: Int,
    val symmetric: Boolean,
    val weeklyTargets: String?,
    val unit: String,
    val schedule: ScheduleType,
    val days: List<Int>,
    val startDate: Long,
    val color: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(
    habitId: Long? = null,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val existing = habits.firstOrNull { it.id == habitId }
    val context = LocalContext.current
    val isSteps = existing?.isStepsHabit() ?: false
    val editing = habitId != null

    // 1. First: Habit Name
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }

    // 2. Second: Description
    var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }

    // 3. Third: Category selector
    var categoryId by remember(existing, categories) {
        mutableStateOf(
            existing?.categoryId ?: categories.filter { it.name.lowercase() != "habits" }.firstOrNull()?.id ?: 0L
        )
    }

    // 4. Fourth: Repeat selector
    var schedule by remember(existing) { mutableStateOf(existing?.scheduleType ?: ScheduleType.DAILY) }

    // 5. Fifth: Weekday selector (for Weekly repetition)
    var days by remember(existing) { mutableStateOf(existing?.scheduleDays ?: listOf(1, 2, 3, 4, 5)) }

    // 6. Sixth: Habit Type selector (Symmetric vs Asymmetric)
    var isSymmetric by remember(existing) { mutableStateOf(existing?.symmetric ?: true) }

    // 7. Seventh: Measurable Habit Configuration (Unit & Targets)
    var unit by remember(existing) { mutableStateOf(existing?.unit ?: "Reps") }

    // Shared Reps and Sets for Symmetric habits
    var sharedRepsText by remember(existing) { mutableStateOf((existing?.repsPerSet ?: 1).toString()) }
    var sharedSetsText by remember(existing) { mutableStateOf((existing?.sets ?: 1).toString()) }

    // Per-weekday Reps and Sets map for Asymmetric Weekly habits (dayNum -> Pair(repsText, setsText))
    val weekdayTargetMap = remember(existing) {
        val initialMap = mutableStateMapOf<Int, Pair<String, String>>()
        val existingTargets = existing?.weeklyTargets
        if (!existingTargets.isNullOrBlank()) {
            try {
                val json = JSONObject(existingTargets)
                (1..7).forEach { dayNum ->
                    if (json.has(dayNum.toString())) {
                        val obj = json.getJSONObject(dayNum.toString())
                        val r = obj.optInt("reps", 1)
                        val s = obj.optInt("sets", 1)
                        initialMap[dayNum] = Pair(r.toString(), s.toString())
                    } else {
                        initialMap[dayNum] = Pair("1", "1")
                    }
                }
            } catch (e: Exception) {
                (1..7).forEach { initialMap[it] = Pair("1", "1") }
            }
        } else {
            (1..7).forEach { initialMap[it] = Pair("1", "1") }
        }
        initialMap
    }

    var startDate by remember(existing) { mutableStateOf(existing?.startDate ?: todayTimestamp()) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    // Auto-lock Asymmetric to Symmetric when repetition is NOT Weekly
    LaunchedEffect(schedule) {
        if (schedule != ScheduleType.WEEKLY) {
            isSymmetric = true
        }
    }

    val canSave = !isSteps && name.isNotBlank() && (schedule != ScheduleType.WEEKLY || days.isNotEmpty())

    fun performSave() {
        val finalSymmetric = if (schedule == ScheduleType.WEEKLY) isSymmetric else true
        val finalType: HabitType
        val finalTarget: Double
        var finalSets = 1
        var finalReps = 1
        var finalWeeklyTargetsJson: String? = null

        if (finalSymmetric) {
            val reps = sharedRepsText.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val sets = sharedSetsText.toIntOrNull()?.coerceAtLeast(1) ?: 1
            finalReps = reps
            finalSets = sets
            finalTarget = (reps * sets).toDouble()

            // For symmetric measurable habits with 1 set & 1 rep, automatically treat as Yes/No habit
            if (reps == 1 && sets == 1) {
                finalType = HabitType.YES_NO
            } else {
                finalType = HabitType.MEASURABLE
            }
        } else {
            finalType = HabitType.MEASURABLE
            val jsonMap = JSONObject()
            var maxTarget = 0.0

            days.forEach { dayNum ->
                val pair = weekdayTargetMap[dayNum] ?: Pair("1", "1")
                val reps = pair.first.toIntOrNull()?.coerceAtLeast(1) ?: 1
                val sets = pair.second.toIntOrNull()?.coerceAtLeast(1) ?: 1
                val dayTarget = (reps * sets).toDouble()
                if (dayTarget > maxTarget) maxTarget = dayTarget

                val dayObj = JSONObject().apply {
                    put("reps", reps)
                    put("sets", sets)
                    put("target", dayTarget)
                }
                jsonMap.put(dayNum.toString(), dayObj)
            }
            finalTarget = if (maxTarget > 0) maxTarget else 1.0
            finalWeeklyTargetsJson = jsonMap.toString()
        }

        val category = categories.firstOrNull { it.id == categoryId }
        val categoryName = if (isSteps) "System" else (category?.name ?: "Personal")
        val color = if (isSteps) HabitEntity.PLATINUM_COLOR else CategoryConstants.getColorForCategory(categoryName)

        val fields = HabitFields(
            name = name.trim(),
            description = description.trim(),
            categoryId = categoryId,
            type = finalType,
            target = finalTarget,
            sets = finalSets,
            repsPerSet = finalReps,
            symmetric = finalSymmetric,
            weeklyTargets = finalWeeklyTargetsJson,
            unit = unit.trim().ifBlank { "Reps" },
            schedule = schedule,
            days = days,
            startDate = startDate,
            color = color
        )

        viewModel.save(existing, fields, onSaved) { error = it }
    }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor,
        contentColor = textColor
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                }
                Text(
                    text = if (isSteps) "Steps Habit" else if (editing) "Edit Habit" else "Create Habit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (!isSteps) {
                    TextButton(
                        onClick = { if (canSave) performSave() },
                        enabled = canSave
                    ) {
                        Text(
                            text = if (editing) "Save" else "Create",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(Modifier.width(48.dp))
                }
            }

            if (isSteps) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(HabitEntity.PLATINUM_COLOR).copy(alpha = 0.1f))) {
                    Text(
                        "This is an automated step tracking habit. It cannot be edited or deleted.",
                        Modifier.padding(16.dp),
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            // =========================================================================
            // REQUIRED FIELD ORDER
            // =========================================================================

            // 1. FIRST: Habit Name Field
            FormSection("Habit Name") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Habit Name") },
                    singleLine = true,
                    enabled = !isSteps,
                    isError = name.isBlank() && error != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = textColor.copy(alpha = 0.7f)
                    )
                )
            }

            // 2. SECOND: Description Field
            FormSection("Description") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description / Note (Optional)") },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )
            }

            // 3. THIRD: Category Selector
            FormSection("Category") {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.filter { it.name.lowercase() != "habits" }.forEach { item ->
                        FilterChip(
                            selected = categoryId == item.id,
                            onClick = { categoryId = item.id },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        iconForCategory(CategoryConstants.getIconForCategory(item.name)),
                                        null,
                                        Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(item.name)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = textColor,
                                selectedLabelColor = Color.White,
                                selectedContainerColor = Color(CategoryConstants.getColorForCategory(item.name))
                            )
                        )
                    }
                }
            }

            // 4. FOURTH: Repeat Selector
            FormSection("Repeat") {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        ScheduleType.DAILY,
                        ScheduleType.WEEKLY,
                        ScheduleType.EVEN_DAYS,
                        ScheduleType.ODD_DAYS
                    ).forEach { option ->
                        FilterChip(
                            selected = schedule == option,
                            onClick = { schedule = option },
                            label = { Text(scheduleLabelText(option)) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = textColor,
                                selectedLabelColor = Color.White,
                                selectedContainerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // 5. FIFTH: Weekday Selector (Displayed ONLY when Weekly repetition is selected)
            if (schedule == ScheduleType.WEEKLY) {
                FormSection("Select Weekdays") {
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val weekdays = listOf(
                            "Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4,
                            "Fri" to 5, "Sat" to 6, "Sun" to 7
                        )
                        weekdays.forEach { (label, dayNum) ->
                            val isSelected = days.contains(dayNum)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    days = if (isSelected) {
                                        if (days.size > 1) days - dayNum else days
                                    } else {
                                        days + dayNum
                                    }
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    labelColor = textColor,
                                    selectedLabelColor = Color.White,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // 6. SIXTH: Habit Type Selector (Symmetric vs Asymmetric)
            FormSection("Habit Type") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isSymmetric,
                        onClick = { isSymmetric = true },
                        label = { Text("Symmetric Measurable") },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = textColor,
                            selectedLabelColor = Color.White,
                            selectedContainerColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Asymmetric option available ONLY for Weekly repetition
                    if (schedule == ScheduleType.WEEKLY) {
                        FilterChip(
                            selected = !isSymmetric,
                            onClick = { isSymmetric = false },
                            label = { Text("Asymmetric Measurable") },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = textColor,
                                selectedLabelColor = Color.White,
                                selectedContainerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // 7. SEVENTH: Measurable Habit Configuration Section
            FormSection("Target Configuration") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Unit Field
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Unit (e.g. Reps, Glasses, Pages, Mins)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )

                    // Target Configuration Table
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)
                        ),
                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Table Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Days",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = "Reps / Set",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Sets",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (isSymmetric) {
                                // Symmetric Target Row
                                val scheduleDisplay = when (schedule) {
                                    ScheduleType.WEEKLY -> {
                                        val dayNames = mapOf(
                                            1 to "Mon", 2 to "Tue", 3 to "Wed",
                                            4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun"
                                        )
                                        days.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
                                    }
                                    ScheduleType.EVEN_DAYS -> "Even Days"
                                    ScheduleType.ODD_DAYS -> "Odd Days"
                                    else -> "Daily"
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = scheduleDisplay,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    OutlinedTextField(
                                        value = sharedRepsText,
                                        onValueChange = { input ->
                                            sharedRepsText = input.filter { it.isDigit() }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 4.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textColor,
                                            unfocusedTextColor = textColor
                                        )
                                    )
                                    OutlinedTextField(
                                        value = sharedSetsText,
                                        onValueChange = { input ->
                                            sharedSetsText = input.filter { it.isDigit() }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 4.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textColor,
                                            unfocusedTextColor = textColor
                                        )
                                    )
                                }
                            } else {
                                // Asymmetric Target Rows (One for each selected weekday)
                                val dayNames = mapOf(
                                    1 to "Monday", 2 to "Tuesday", 3 to "Wednesday",
                                    4 to "Thursday", 5 to "Friday", 6 to "Saturday", 7 to "Sunday"
                                )

                                days.sorted().forEach { dayNum ->
                                    val currentPair = weekdayTargetMap[dayNum] ?: Pair("1", "1")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dayNames[dayNum] ?: "Day $dayNum",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        OutlinedTextField(
                                            value = currentPair.first,
                                            onValueChange = { input ->
                                                val clean = input.filter { it.isDigit() }
                                                weekdayTargetMap[dayNum] = Pair(clean, currentPair.second)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 4.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = textColor,
                                                unfocusedTextColor = textColor
                                            )
                                        )
                                        OutlinedTextField(
                                            value = currentPair.second,
                                            onValueChange = { input ->
                                                val clean = input.filter { it.isDigit() }
                                                weekdayTargetMap[dayNum] = Pair(currentPair.first, clean)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 4.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = textColor,
                                                unfocusedTextColor = textColor
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Start Date Selector
            FormSection("Start Date") {
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                startDate = Calendar.getInstance().apply {
                                    set(year, month, day, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(startDate)),
                        color = textColor
                    )
                }
            }

            // Point Costs Info
            if (!editing) {
                Text(
                    text = "Create Habit (Costs 25 Points)",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold
                )
            } else if (existing != null) {
                Text(
                    text = "Edit Habit",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold
                )
            }

            if (editing && existing != null && !isSteps) {
                Button(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Habit (Costs 30 Points)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(72.dp))
        }
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete Habit?") },
            text = { Text("Deleting this habit costs 30 Elite Points. Are you sure?", color = Color.Red) },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(existing.id) { onBack() } }) {
                    Text("Delete (30 Pts)", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

private fun scheduleLabelText(type: ScheduleType) = when (type) {
    ScheduleType.ODD_DAYS -> "Odd Days"
    ScheduleType.EVEN_DAYS -> "Even Days"
    ScheduleType.WEEKLY -> "Weekly"
    else -> "Daily"
}

private fun iconForCategory(value: String): ImageVector = when (value.lowercase()) {
    "fitness" -> Icons.Default.FitnessCenter
    "school" -> Icons.Default.School
    "work" -> Icons.Default.Work
    "book" -> Icons.AutoMirrored.Filled.MenuBook
    "self" -> Icons.Default.SelfImprovement
    "payments" -> Icons.Default.Payments
    "bedtime" -> Icons.Default.Bedtime
    "person" -> Icons.Default.Person
    else -> Icons.Default.Flag
}

private fun todayTimestamp(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
