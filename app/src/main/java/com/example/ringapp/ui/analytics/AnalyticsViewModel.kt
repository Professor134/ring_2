package com.example.ringapp.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.local.entities.*
import com.example.ringapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private fun startOfDay(date: LocalDate): Long = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

data class AnalyticsPoint(val date: LocalDate, val score: Int, val actual: Int = 0, val target: Int = 0, val completed: Boolean = false)
data class AnalyticsUiState(val habits: List<HabitEntity> = emptyList(), val tasks: List<TaskEntity> = emptyList(), val categories: List<CategoryEntity> = emptyList(), val progress: List<HabitProgressEntity> = emptyList(), val points: Int = 0, val bestStreak: Int = 0, val loading: Boolean = true, val error: String? = null)

data class PersonalAnalyticsState(val habit: HabitEntity? = null, val progress: List<HabitProgressEntity> = emptyList(), val transactions: List<PointTransactionEntity> = emptyList(), val points: Int = 0, val habitPoints: Int = 0, val loading: Boolean = true, val error: String? = null)

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
    val state: StateFlow<AnalyticsUiState> = combine(observeHabits(), observeTasks(), observeCategories(), observeProgress(from, to), observeUserProgress()) { habits, tasks, categories, progress, user ->
        AnalyticsUiState(habits, tasks, categories, progress, user?.currentPoints ?: 0, habits.maxOfOrNull { it.bestStreak } ?: 0, false)
    }.catch { emit(AnalyticsUiState(loading = false, error = it.message ?: "Unable to load analytics")) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())
}

@HiltViewModel
class PersonalAnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDetail: ObserveHabitDetailUseCase,
    observeTransactions: ObserveHabitTransactionsUseCase,
    observeUserProgress: ObserveUserProgressUseCase,
    private val deleteHabit: DeleteHabitUseCase
) : ViewModel() {
    private val habitId = checkNotNull(savedStateHandle.get<String>("habitId")).toLong()
    val state: StateFlow<PersonalAnalyticsState> = combine(observeDetail(habitId, 365), observeTransactions(habitId), observeUserProgress()) { detail, transactions, user -> 
        PersonalAnalyticsState(
            habit = detail?.habit, 
            progress = detail?.progress.orEmpty(), 
            transactions = transactions, 
            points = user?.currentPoints ?: 0, 
            habitPoints = transactions.sumOf { it.amount },
            loading = false
        ) 
    }
        .catch { emit(PersonalAnalyticsState(loading = false, error = it.message ?: "Unable to load habit analytics")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PersonalAnalyticsState())

    fun delete() = viewModelScope.launch { deleteHabit(habitId) }
}

fun aggregateDaily(habits: List<HabitEntity>, progress: List<HabitProgressEntity>, date: LocalDate): AnalyticsPoint? {
    val active = habits.filter { com.example.ringapp.domain.engine.ScheduleEngine.isActiveOnDate(it, date) }
    if (active.isEmpty()) return null
    val timestamp = startOfDay(date)
    val dayRecords = progress.filter { it.date == timestamp }
    val totalPercentage = active.sumOf { habit ->
        dayRecords.firstOrNull { it.habitId == habit.id }?.percentage ?: 0
    }
    val score = totalPercentage / active.size
    return AnalyticsPoint(date, score, dayRecords.sumOf { it.actual }, active.sumOf { it.target }, active.all { habit -> dayRecords.find { it.habitId == habit.id }?.completed == true })
}
