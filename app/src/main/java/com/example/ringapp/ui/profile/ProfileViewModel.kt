// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\profile\ProfileViewModel.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.domain.engine.AchievementEngine
import com.example.ringapp.domain.engine.AchievementProgress
import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.domain.usecase.ObserveProfileUseCase
import com.example.ringapp.domain.usecase.ObserveUserProgressUseCase
import com.example.ringapp.domain.usecase.ObserveHabitsUseCase
import com.example.ringapp.domain.usecase.ObserveTasksUseCase
import com.example.ringapp.domain.usecase.ObservePointTransactionsUseCase
import com.example.ringapp.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Handles the profile ui state logic for this component without modifying runtime behavior.
// Represents the profile ui state structure used by the application.
data class ProfileUiState(val profile: ProfileEntity? = null, val points: Int = 0, val lifetimePoints: Int = 0, val level: Int = 1, val achievements: List<AchievementProgress> = emptyList(), val currentStreak: Int = 0, val bestStreak: Int = 0, val totalHabits: Int = 0, val completedTasks: Int = 0, val loading: Boolean = true, val transactions: List<com.example.ringapp.data.local.entities.PointTransactionEntity> = emptyList())

@HiltViewModel
// Handles the profile view model logic for this component without modifying runtime behavior.
// Represents the profile view model structure used by the application.
class ProfileViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    observeProgress: ObserveUserProgressUseCase,
    observeHabits: ObserveHabitsUseCase,
    observeTasks: ObserveTasksUseCase,
    observeTransactions: ObservePointTransactionsUseCase,
    private val updateProfile: UpdateProfileUseCase
) : ViewModel() {
    val state: StateFlow<ProfileUiState> = combine(
        observeProfile(), observeProgress(), observeHabits(), observeTasks(), observeTransactions()
    ) { array ->
        val profile = array[0] as ProfileEntity?
        val progress = array[1] as com.example.ringapp.data.local.entities.UserProgressEntity?
        val habits = array[2] as List<com.example.ringapp.data.local.entities.HabitEntity>
        val tasks = array[3] as List<com.example.ringapp.data.local.entities.TaskEntity>
        val transactions = array[4] as List<com.example.ringapp.data.local.entities.PointTransactionEntity>
        val currentStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        val level = progress?.level ?: 1
        val lifetimePoints = progress?.lifetimePoints ?: 0

        ProfileUiState(
            profile,
            progress?.currentPoints ?: 0,
            lifetimePoints,
            level,
            AchievementEngine.buildProfileProgress(
                longestStreak = habits.maxOfOrNull { it.bestStreak } ?: currentStreak,
                habitsCreated = habits.size,
                completedTasks = tasks.count { it.completed },
                lifetimePoints = lifetimePoints,
                level = level,
                habitCompletions = habits.sumOf { it.totalCompletions }
            ),
            currentStreak,
            habits.maxOfOrNull { it.bestStreak } ?: 0,
            habits.size,
            tasks.count { it.completed },
            false,
            transactions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

// Manages the save flow without changing runtime behavior.
    fun save(profile: ProfileEntity) = viewModelScope.launch { updateProfile(profile) }
}