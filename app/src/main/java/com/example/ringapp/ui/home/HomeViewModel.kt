package com.example.ringapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.domain.engine.ScheduleEngine
import com.example.ringapp.domain.usecase.CompleteHabitUseCase
import com.example.ringapp.domain.usecase.CompleteTaskUseCase
import com.example.ringapp.domain.usecase.ObserveCategoriesUseCase
import com.example.ringapp.domain.usecase.ObserveHabitsUseCase
import com.example.ringapp.domain.usecase.ObserveProgressRangeUseCase
import com.example.ringapp.domain.usecase.ObserveProfileUseCase
import com.example.ringapp.domain.usecase.ObserveTodayDataUseCase
import com.example.ringapp.domain.usecase.ObserveUserProgressUseCase
import com.example.ringapp.domain.usecase.RecordHabitProgressUseCase
import com.example.ringapp.domain.usecase.ToggleHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHabits: ObserveHabitsUseCase,
    observeProfile: ObserveProfileUseCase,
    observeUserProgress: ObserveUserProgressUseCase,
    observeTodayData: ObserveTodayDataUseCase,
    observeCategories: ObserveCategoriesUseCase,
    observeProgressRange: ObserveProgressRangeUseCase,
    private val habitRepository: com.example.ringapp.data.repository.HabitRepository,
    private val taskRepository: com.example.ringapp.data.repository.TaskRepository,
    private val toggleHabit: ToggleHabitUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val recordProgress: RecordHabitProgressUseCase
) : ViewModel() {
    private val today = LocalDate.now()
    private val from = today.minusDays(13).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val to = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

    init {
        viewModelScope.launch {
            habitRepository.seedDefaults()
            taskRepository.cleanupOldTasks()
        }
    }
    
    val uiState: StateFlow<HomeUiState> = combine(
        observeHabits(), observeProfile(), observeUserProgress(), observeTodayData(),
        observeCategories(), observeProgressRange(from, to)
    ) { array ->
        val habits = array[0] as List<HabitEntity>
        val profile = array[1] as com.example.ringapp.data.local.entities.ProfileEntity?
        val progress = array[2] as com.example.ringapp.data.local.entities.UserProgressEntity?
        val todayData = array[3] as com.example.ringapp.domain.usecase.TodayData
        val categories = array[4] as List<com.example.ringapp.data.local.entities.CategoryEntity>
        val allProgress = array[5] as List<HabitProgressEntity>

        val todaysHabits = habits.filter { ScheduleEngine.isActiveOnDate(it, today) }
        val chart = (0..13).map { offset ->
            val date = today.minusDays((13 - offset).toLong())
            val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val activeHabits = habits.filter { ScheduleEngine.isActiveOnDate(it, date) }
            val dailyProgress = if (activeHabits.isEmpty()) 0 else {
                val dayRecords = allProgress.filter { it.date == timestamp }
                val totalPercentage = activeHabits.sumOf { habit ->
                    dayRecords.firstOrNull { it.habitId == habit.id }?.percentage ?: 0
                }
                totalPercentage / activeHabits.size
            }
            ChartPoint(
                date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())),
                dailyProgress
            )
        }
        HomeUiState(
            habits = todaysHabits,
            tasks = todayData.tasks,
            categories = categories,
            todayProgress = todayData.progress,
            allProgress = allProgress,
            chart = chart,
            greeting = greeting(),
            date = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
            points = progress?.currentPoints ?: 0,
            userName = profile?.name.orEmpty(),
            isLoading = false
        )
    }.catch { emit(HomeUiState(isLoading = false, errorMessage = it.message ?: "Unable to load your day")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.CompleteHabit -> viewModelScope.launch { toggleHabit(event.habit) }
            is HomeEvent.RecordProgress -> viewModelScope.launch { recordProgress(event.habit, event.date, event.value, event.note) }
            is HomeEvent.ToggleTask -> viewModelScope.launch { completeTask(event.task) }
            HomeEvent.AddHabit, HomeEvent.AddTask -> Unit
        }
    }

    private fun greeting(): String = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning,"
        in 12..17 -> "Good Afternoon,"
        else -> "Good Evening,"
    }
}

data class ChartPoint(val label: String, val score: Int)

data class HomeUiState(
    val habits: List<HabitEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val categories: List<com.example.ringapp.data.local.entities.CategoryEntity> = emptyList(),
    val todayProgress: List<HabitProgressEntity> = emptyList(),
    val allProgress: List<HabitProgressEntity> = emptyList(),
    val chart: List<ChartPoint> = emptyList(),
    val greeting: String = "Good Morning,",
    val date: String = "",
    val points: Int = 0,
    val userName: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed interface HomeEvent {
    data class CompleteHabit(val habit: HabitEntity) : HomeEvent
    data class RecordProgress(val habit: HabitEntity, val date: Long, val value: Double, val note: String?) : HomeEvent
    data class ToggleTask(val task: TaskEntity) : HomeEvent
    data object AddHabit : HomeEvent
    data object AddTask : HomeEvent
}
