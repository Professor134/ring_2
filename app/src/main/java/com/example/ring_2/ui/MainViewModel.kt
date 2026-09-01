package com.example.ring_2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ring_2.data.MainRepository
import com.example.ring_2.data.model.*
import com.example.ring_2.logic.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    val allHabits = repository.allHabits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProgress = repository.userProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCategories = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val todayProgress = repository.getProgressForDate(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProgress = repository.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNotifications = repository.getNotificationsForLast3Days()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _taskFilter = MutableStateFlow("All")
    val taskFilter: StateFlow<String> = _taskFilter.asStateFlow()

    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    val filteredTasks = combine(allTasks, _taskFilter) { tasks, filter ->
        when (filter) {
            "Pending" -> tasks.filter { !it.completed }
            "Completed" -> tasks.filter { it.completed }
            "High Priority" -> tasks.filter { it.priority == TaskPriority.HIGH || it.priority == TaskPriority.URGENT }
            "Overdue" -> tasks.filter { !it.completed && (it.dueDate ?: Long.MAX_VALUE) < DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis()) }
            "Today" -> tasks.filter { it.dueDate == DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis()) }
            "Upcoming" -> tasks.filter { (it.dueDate ?: 0L) > DateTimeUtils.getMidnightTimestamp(System.currentTimeMillis()) }
            else -> tasks
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskStats = allTasks.map { list ->
        val total = list.size
        val completed = list.count { it.completed }
        val pending = total - completed
        Triple(pending, completed, total)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0, 0, 0))

    val profileStats = combine(allHabits, allTasks) { habits, tasks ->
        val currentStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        val bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
        val totalHabits = habits.size
        val completedTasks = tasks.count { it.completed }
        
        mapOf(
            "currentStreak" to currentStreak,
            "bestStreak" to bestStreak,
            "totalHabits" to totalHabits,
            "completedTasks" to completedTasks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getProgressForHabit(habitId: Long): Flow<List<HabitProgressEntity>> = repository.getProgressForHabit(habitId)
    fun getTransactionsForHabit(habitId: Long): Flow<List<PointTransactionEntity>> = repository.getTransactionsForHabit(habitId)

    init {
        viewModelScope.launch {
            repository.initializeUserData()
        }
    }

    fun addHabit(habit: HabitEntity) {
        viewModelScope.launch {
            try {
                repository.createHabit(habit)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            try {
                repository.updateHabit(habit)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun recordProgress(habit: HabitEntity, value: Double, note: String = "") {
        viewModelScope.launch {
            repository.recordHabitProgress(habit, System.currentTimeMillis(), value, note)
        }
    }

    fun recordProgressForDate(habit: HabitEntity, date: Long, value: Double, note: String = "") {
        viewModelScope.launch {
            repository.recordHabitProgress(habit, date, value, note)
        }
    }

    fun addTask(task: TaskEntity, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createTask(task)
            onResult(id)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.completeTask(task)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun updateThemePreference(theme: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.updateProfile(profile.copy(themePreference = theme))
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
