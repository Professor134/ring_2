package com.example.ring_2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ring_2.data.MainRepository
import com.example.ring_2.data.model.Habit
import com.example.ring_2.data.model.Task
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    val allHabits = repository.allHabits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeProfile()
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.createHabit(habit)
            } catch (e: Exception) {
                // Handle error (e.g. not enough points)
            }
        }
    }

    fun completeHabit(habit: Habit, value: Double = 1.0, note: String = "") {
        viewModelScope.launch {
            repository.completeHabit(habit, System.currentTimeMillis(), value, note)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.createTask(task)
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            repository.completeTask(task)
        }
    }
}
