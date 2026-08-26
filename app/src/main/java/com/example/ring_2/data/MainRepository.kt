package com.example.ring_2.data

import com.example.ring_2.data.dao.HabitDao
import com.example.ring_2.data.dao.TaskDao
import com.example.ring_2.data.dao.UserDao
import com.example.ring_2.data.model.*
import com.example.ring_2.logic.GamificationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MainRepository(
    private val habitDao: HabitDao,
    private val taskDao: TaskDao,
    private val userDao: UserDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()
    val transactions: Flow<List<PointTransaction>> = userDao.getAllTransactions()

    suspend fun createHabit(habit: Habit) {
        val profile = userProfile.first() ?: return
        if (profile.elitePoints < Math.abs(GamificationEngine.HABIT_CREATION_COST)) {
            throw Exception("Not enough points")
        }

        val transaction = GamificationEngine.createTransaction(
            TransactionType.CREATE_HABIT,
            GamificationEngine.HABIT_CREATION_COST,
            "Created habit: ${habit.name}"
        )
        
        userDao.insertTransaction(transaction)
        userDao.updateProfile(GamificationEngine.applyTransaction(profile, transaction))
        habitDao.insertHabit(habit)
    }

    suspend fun completeHabit(habit: Habit, date: Long, value: Double, note: String) {
        val record = HabitRecord(
            habitId = habit.id,
            date = date,
            value = value,
            isCompleted = true,
            note = note
        )
        habitDao.insertRecord(record)

        val profile = userProfile.first() ?: return
        val transaction = GamificationEngine.createTransaction(
            TransactionType.COMPLETE_HABIT,
            GamificationEngine.HABIT_COMPLETION_REWARD,
            "Completed habit: ${habit.name}",
            habit.id
        )
        
        userDao.insertTransaction(transaction)
        userDao.updateProfile(GamificationEngine.applyTransaction(profile, transaction))
        
        // Update habit streak and completions
        val updatedHabit = habit.copy(
            totalCompletions = habit.totalCompletions + 1,
            currentStreak = habit.currentStreak + 1, // Simple streak for now
            bestStreak = Math.max(habit.bestStreak, habit.currentStreak + 1)
        )
        habitDao.updateHabit(updatedHabit)
        
        // Milestone checks
        checkStreakMilestones(updatedHabit)
    }

    private suspend fun checkStreakMilestones(habit: Habit) {
        if (habit.currentStreak == 10) {
            awardMilestone(habit, TransactionType.STREAK_10_DAYS, GamificationEngine.STREAK_10_DAY_REWARD)
        } else if (habit.currentStreak % 20 == 0 && habit.currentStreak > 0) {
            awardMilestone(habit, TransactionType.STREAK_20_DAYS, GamificationEngine.STREAK_20_DAY_REWARD)
        }
    }

    private suspend fun awardMilestone(habit: Habit, type: TransactionType, amount: Int) {
        val profile = userProfile.first() ?: return
        val transaction = GamificationEngine.createTransaction(
            type,
            amount,
            "Reached ${habit.currentStreak}-day streak for ${habit.name}",
            habit.id
        )
        userDao.insertTransaction(transaction)
        userDao.updateProfile(GamificationEngine.applyTransaction(profile, transaction))
    }

    suspend fun createTask(task: Task) {
        val profile = userProfile.first() ?: return
        val transaction = GamificationEngine.createTransaction(
            TransactionType.CREATE_TASK,
            GamificationEngine.TASK_CREATION_COST,
            "Created task: ${task.title}"
        )
        userDao.insertTransaction(transaction)
        userDao.updateProfile(GamificationEngine.applyTransaction(profile, transaction))
        taskDao.insertTask(task)
    }

    suspend fun completeTask(task: Task) {
        if (task.isCompleted) return
        
        val profile = userProfile.first() ?: return
        val transaction = GamificationEngine.createTransaction(
            TransactionType.COMPLETE_TASK,
            GamificationEngine.TASK_COMPLETION_REWARD,
            "Completed task: ${task.title}",
            task.id
        )
        userDao.insertTransaction(transaction)
        userDao.updateProfile(GamificationEngine.applyTransaction(profile, transaction))
        taskDao.updateTask(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
    }
    
    suspend fun initializeProfile() {
        if (userProfile.first() == null) {
            userDao.insertProfile(UserProfile())
            val transaction = GamificationEngine.createTransaction(
                TransactionType.STARTING_POINTS,
                GamificationEngine.STARTING_POINTS,
                "Welcome bonus!"
            )
            userDao.insertTransaction(transaction)
        }
    }
}
