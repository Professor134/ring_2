// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\ui\profile\ProfileViewModel.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.domain.engine.AchievementEngine
import com.example.ringapp.domain.engine.AchievementProgress
import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.data.local.entities.TransactionType
import com.example.ringapp.data.repository.PointRepository
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

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
    private val updateProfile: UpdateProfileUseCase,
    private val pointRepository: PointRepository
) : ViewModel() {
    private val profileState = combine(
        observeProfile(), observeProgress(), observeHabits(), observeTasks(), observeTransactions()
    ) { profile, progress, allHabits, tasks, transactions ->
        val habits = allHabits.filterNot {
            it.isStepsHabit() || it.isBmiHabit()
        }
        val currentStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        val level = progress?.level ?: 1
        val lifetimePoints = progress?.lifetimePoints ?: 0
        val earnedTierReferences = transactions
            .filter { it.type == TransactionType.ACHIEVEMENT_REWARD }
            .mapTo(mutableSetOf()) { it.uniqueReference }
        val achievementPoints = transactions
            .filter { transaction ->
                transaction.amount > 0 &&
                    transaction.type != TransactionType.STARTING_POINTS &&
                    transaction.type != TransactionType.ACHIEVEMENT_REWARD &&
                    (transaction.type != TransactionType.LEVEL_UP_REWARD ||
                        transaction.habitId != null || transaction.taskId != null) &&
                    transaction.habitId != HabitEntity.STEPS_HABIT_ID &&
                    transaction.habitId != HabitEntity.BMI_HABIT_ID
            }
            .sumOf { it.amount }
        val achievementLevel = pointRepository.calculateLevel(achievementPoints)

        ProfileUiState(
            profile,
            progress?.currentPoints ?: 0,
            lifetimePoints,
            level,
            AchievementEngine.buildProfileProgress(
                longestStreak = habits.maxOfOrNull { it.bestStreak } ?: currentStreak,
                habitsCreated = habits.size,
                completedTasks = tasks.count { it.completed },
                lifetimePoints = achievementPoints,
                level = achievementLevel,
                habitCompletions = habits.sumOf { it.totalCompletions },
                earnedTierReferences = earnedTierReferences
            ),
            currentStreak,
            habits.maxOfOrNull { it.bestStreak } ?: 0,
            habits.size,
            tasks.count { it.completed },
            false,
            transactions
        )
    }

    val state: StateFlow<ProfileUiState> = profileState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProfileUiState()
    )

    init {
        viewModelScope.launch {
            profileState.collect { profile ->
                profile.achievements.forEach { achievement ->
                    achievement.tiers
                        .filter { it.unlocked && !it.earned && achievement.value >= it.target }
                        .forEach { tier ->
                            try {
                                pointRepository.record(
                                    type = TransactionType.ACHIEVEMENT_REWARD,
                                    amount = tier.rewardPoints,
                                    reference = tier.rewardReference,
                                    description = "${achievement.title} ${tier.tier.label} achievement reward"
                                )
                            } catch (failure: Exception) {
                                if (failure is CancellationException) throw failure
                                Log.e("ProfileViewModel", "Unable to award ${tier.rewardReference}", failure)
                            }
                        }
                }
            }
        }
    }

// Manages the save flow without changing runtime behavior.
    fun save(profile: ProfileEntity) = viewModelScope.launch { updateProfile(profile) }
}