package com.example.ringapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.domain.usecase.ObserveAchievementsUseCase
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

data class ProfileUiState(val profile: ProfileEntity? = null, val points: Int = 0, val lifetimePoints: Int = 0, val level: Int = 1, val achievements: List<com.example.ringapp.data.local.entities.AchievementEntity> = emptyList(), val currentStreak: Int = 0, val bestStreak: Int = 0, val totalHabits: Int = 0, val completedTasks: Int = 0, val loading: Boolean = true, val transactions: List<com.example.ringapp.data.local.entities.PointTransactionEntity> = emptyList())

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    observeProgress: ObserveUserProgressUseCase,
    observeAchievements: ObserveAchievementsUseCase,
    observeHabits: ObserveHabitsUseCase,
    observeTasks: ObserveTasksUseCase,
    observeTransactions: ObservePointTransactionsUseCase,
    private val updateProfile: UpdateProfileUseCase
) : ViewModel() {
    val state: StateFlow<ProfileUiState> = combine(
        observeProfile(), observeProgress(), observeAchievements(), observeHabits(), observeTasks(), observeTransactions()
    ) { array ->
        val profile = array[0] as ProfileEntity?
        val progress = array[1] as com.example.ringapp.data.local.entities.UserProgressEntity?
        val achievements = array[2] as List<com.example.ringapp.data.local.entities.AchievementEntity>
        val habits = array[3] as List<com.example.ringapp.data.local.entities.HabitEntity>
        val tasks = array[4] as List<com.example.ringapp.data.local.entities.TaskEntity>
        val transactions = array[5] as List<com.example.ringapp.data.local.entities.PointTransactionEntity>

        ProfileUiState(
            profile,
            progress?.currentPoints ?: 0,
            progress?.lifetimePoints ?: 0,
            progress?.level ?: 1,
            achievements,
            habits.maxOfOrNull { it.currentStreak } ?: 0,
            habits.maxOfOrNull { it.bestStreak } ?: 0,
            habits.size,
            tasks.count { it.completed },
            false,
            transactions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun save(profile: ProfileEntity) = viewModelScope.launch { updateProfile(profile) }
}