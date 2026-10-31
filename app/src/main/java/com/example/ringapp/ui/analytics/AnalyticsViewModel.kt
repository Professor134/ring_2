// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\analytics\AnalyticsViewModel.kt
// Purpose: ViewModel for Insights and Personal Analytics screens.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.local.entities.CategoryEntity
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.domain.usecase.DeleteHabitUseCase
import com.example.ringapp.domain.usecase.HabitDetailData
import com.example.ringapp.domain.usecase.ObserveCategoriesUseCase
import com.example.ringapp.domain.usecase.ObserveHabitDetailUseCase
import com.example.ringapp.domain.usecase.ObserveHabitTransactionsUseCase
import com.example.ringapp.domain.usecase.ObserveHabitsUseCase
import com.example.ringapp.domain.usecase.ObserveProfileUseCase
import com.example.ringapp.domain.usecase.ObserveProgressRangeUseCase
import com.example.ringapp.domain.usecase.ObserveTasksUseCase
import com.example.ringapp.domain.usecase.ObserveUserProgressUseCase
import com.example.ringapp.domain.usecase.RecordHabitProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun startOfDay(date: LocalDate): Long = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

data class AnalyticsPoint(
    val date: LocalDate,
    val score: Int,
    val actual: Double = 0.0,
    val target: Double = 0.0,
    val completed: Boolean = false
)

data class AnalyticsUiState(
    val habits: List<HabitEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val progress: List<HabitProgressEntity> = emptyList(),
    val points: Int = 0,
    val bestStreak: Int = 0,
    val loading: Boolean = true,
    val error: String? = null
)

data class PersonalAnalyticsState(
    val habit: HabitEntity? = null,
    val category: CategoryEntity? = null,
    val profile: ProfileEntity? = null,
    val progress: List<HabitProgressEntity> = emptyList(),
    val transactions: List<PointTransactionEntity> = emptyList(),
    val points: Int = 0,
    val habitPoints: Int = 0,
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    observeHabits: ObserveHabitsUseCase,
    observeTasks: ObserveTasksUseCase,
    observeCategories: ObserveCategoriesUseCase,
    observeProgress: ObserveProgressRangeUseCase,
    observeUserProgress: ObserveUserProgressUseCase
) : ViewModel() {
    private val from = startOfDay(LocalDate.now().minusDays(365))
    private val to = startOfDay(LocalDate.now().plusDays(1)) - 1

    val state: StateFlow<AnalyticsUiState> = combine(
        observeHabits(),
        observeTasks(),
        observeCategories(),
        observeProgress(from, to),
        observeUserProgress()
    ) { habits, tasks, categories, progress, user ->
        AnalyticsUiState(
            habits = habits,
            tasks = tasks,
            categories = categories,
            progress = progress,
            points = user?.currentPoints ?: 0,
            bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0,
            loading = false
        )
    }.catch {
        emit(AnalyticsUiState(loading = false, error = it.message ?: "Unable to load analytics"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())
}

@HiltViewModel
class PersonalAnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDetail: ObserveHabitDetailUseCase,
    observeTransactions: ObserveHabitTransactionsUseCase,
    observeUserProgress: ObserveUserProgressUseCase,
    observeCategories: ObserveCategoriesUseCase,
    observeProfile: ObserveProfileUseCase,
    private val deleteHabit: DeleteHabitUseCase,
    private val recordHabitProgress: RecordHabitProgressUseCase
) : ViewModel() {
    private val habitId = checkNotNull(savedStateHandle.get<String>("habitId")).toLong()

    val state: StateFlow<PersonalAnalyticsState> = combine(
        observeDetail(habitId, 365),
        observeTransactions(habitId),
        observeUserProgress(),
        observeCategories(),
        observeProfile()
    ) { detail, transactions, user, categories, profile ->
        val habit = detail?.habit
        PersonalAnalyticsState(
            habit = habit,
            category = categories.find { it.id == habit?.categoryId },
            profile = profile,
            progress = detail?.progress.orEmpty(),
            transactions = transactions,
            points = user?.currentPoints ?: 0,
            habitPoints = transactions.sumOf { it.amount },
            loading = false
        )
    }.catch {
        emit(PersonalAnalyticsState(loading = false, error = it.message ?: "Unable to load habit analytics"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PersonalAnalyticsState())

    fun recordProgress(habit: HabitEntity, dateMillis: Long, actual: Double, note: String?) = viewModelScope.launch {
        recordHabitProgress(habit, dateMillis, actual, note)
    }

    fun delete() = viewModelScope.launch {
        deleteHabit(habitId)
    }
}

fun aggregateDaily(habits: List<HabitEntity>, progress: List<HabitProgressEntity>, date: LocalDate): AnalyticsPoint? {
    val active = habits.filter {
        !it.isBmiHabit() && com.example.ringapp.domain.engine.ScheduleEngine.isActiveOnDate(it, date)
    }
    if (active.isEmpty()) return null
    val timestamp = startOfDay(date)
    val activeHabitIds = active.mapTo(mutableSetOf()) { it.id }
    val dayRecords = progress.filter { it.date == timestamp && it.habitId in activeHabitIds }
    val totalPercentage = active.sumOf { habit ->
        dayRecords.firstOrNull { it.habitId == habit.id }?.percentage ?: 0
    }
    val score = totalPercentage / active.size
    return AnalyticsPoint(
        date = date,
        score = score,
        actual = dayRecords.sumOf { it.actual },
        target = active.sumOf { it.target },
        completed = active.all { habit -> dayRecords.find { it.habitId == habit.id }?.completed == true }
    )
}
