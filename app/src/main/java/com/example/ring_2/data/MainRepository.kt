package com.example.ring_2.data

import com.example.ring_2.data.dao.HabitDao
import com.example.ring_2.data.dao.TaskDao
import com.example.ring_2.data.dao.UserDao
import com.example.ring_2.data.dao.CategoryDao
import com.example.ring_2.data.model.*
import com.example.ring_2.logic.GamificationEngine
import com.example.ring_2.logic.StreakEngine
import com.example.ring_2.logic.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MainRepository(
    private val habitDao: HabitDao,
    private val taskDao: TaskDao,
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val notificationDao: com.example.ring_2.data.dao.NotificationDao,
    private val achievementDao: com.example.ring_2.data.dao.AchievementDao
) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val userProgress: Flow<UserProgressEntity?> = userDao.getUserProgress()
    val userProfile: Flow<ProfileEntity?> = userDao.getProfile()
    val transactions: Flow<List<PointTransactionEntity>> = userDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()

    private suspend fun checkAchievements() {
        val progress = userProgress.first() ?: return
        val habits = allHabits.first()
        val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        val points = progress.lifetimeEarnedPoints

        // Bronze: 7 day streak / 1000 pts
        if (maxStreak >= 7 || points >= 1000) {
            unlockAchievement("Bronze", "Achieved 7-day streak or 1000 points")
        }
        // Silver: 15 day / 3000 pts
        if (maxStreak >= 15 || points >= 3000) {
            unlockAchievement("Silver", "Achieved 15-day streak or 3000 points")
        }
        // Gold: 30 day / 5000 pts
        if (maxStreak >= 30 || points >= 5000) {
            unlockAchievement("Gold", "Achieved 30-day streak or 5000 points")
        }
    }

    private suspend fun unlockAchievement(title: String, description: String) {
        if (achievementDao.getAchievementByTitle(title) == null) {
            achievementDao.insertAchievement(Achievement(id = title.lowercase(), title = title, description = description, isUnlocked = true, unlockedAt = System.currentTimeMillis()))
            addNotification("Achievement Unlocked!", "You've earned the $title achievement!")
        }
    }


    fun getNotificationsForLast3Days(): Flow<List<NotificationEntity>> {
        val threeDaysAgo = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
        return notificationDao.getRecentNotifications(threeDaysAgo)
    }

    suspend fun addNotification(title: String, message: String) {
        notificationDao.insertNotification(NotificationEntity(title = title, message = message))
        // Cleanup old ones
        notificationDao.deleteOldNotifications(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)
    }


    fun getProgressForDate(date: Long): Flow<List<HabitProgressEntity>> = habitDao.getProgressForDateFlow(DateTimeUtils.getMidnightTimestamp(date))
    fun getRecentProgress(startDate: Long): Flow<List<HabitProgressEntity>> = habitDao.getRecentProgress(DateTimeUtils.getMidnightTimestamp(startDate))
    fun getAllProgress(): Flow<List<HabitProgressEntity>> = habitDao.getAllProgress()
    fun getProgressForHabit(habitId: Long): Flow<List<HabitProgressEntity>> = habitDao.getProgressForHabit(habitId)

    fun getTransactionsForHabit(habitId: Long): Flow<List<PointTransactionEntity>> = transactions.map { list ->
        list.filter { it.habitId == habitId }
    }

    /**
     * Centralized method to process any point-related event.
     * Prevents double processing of unique references.
     */
    private suspend fun processPointEvent(
        type: TransactionType,
        amount: Int,
        description: String,
        habitId: Long? = null,
        taskId: Long? = null,
        streakCycleId: Long? = null,
        uniqueReference: String? = null
    ) {
        val reference = uniqueReference ?: "event_${type.name}_${System.currentTimeMillis()}"
        
        // Check if this specific event has already been processed
        if (userDao.getTransactionByReference(reference) != null) return

        val progress = userProgress.first() ?: return
        
        // Check if user has enough points for negative transactions
        if (amount < 0 && progress.currentPoints < Math.abs(amount)) {
            // For creation/change events, we might want to throw an exception to the UI
            if (type == TransactionType.CREATE_HABIT || type == TransactionType.CHANGE_TARGET || type == TransactionType.CREATE_TASK) {
                throw Exception("Not enough Elite Points")
            }
        }

        val transaction = GamificationEngine.createTransaction(
            type, amount, description, habitId, taskId, streakCycleId, reference
        )
        
        userDao.insertTransaction(transaction)
        userDao.updateUserProgress(GamificationEngine.applyTransaction(progress, transaction))
        checkAchievements()
    }

    suspend fun createHabit(habit: HabitEntity) {
        val habitId = habitDao.insertHabit(habit.copy(startDate = DateTimeUtils.getMidnightTimestamp(habit.startDate)))
        
        processPointEvent(
            TransactionType.CREATE_HABIT,
            GamificationEngine.HABIT_CREATION_COST,
            "Created habit: ${habit.name}",
            habitId = habitId,
            uniqueReference = "habit_create_$habitId"
        )
        
        // Start first streak cycle
        val cycle = StreakCycleEntity(
            habitId = habitId,
            cycleNumber = 1,
            startedAt = System.currentTimeMillis()
        )
        habitDao.insertStreakCycle(cycle)
    }

    suspend fun updateHabit(updatedHabit: HabitEntity) {
        val originalHabit = habitDao.getHabitById(updatedHabit.id) ?: return

        // Only charge if target changed
        if (updatedHabit.type == HabitType.MEASURABLE && updatedHabit.target != originalHabit.target) {
            processPointEvent(
                TransactionType.CHANGE_TARGET,
                GamificationEngine.CHANGE_TARGET_COST,
                "Changed target for: ${updatedHabit.name}",
                habitId = updatedHabit.id,
                uniqueReference = "habit_${updatedHabit.id}_change_target_${System.currentTimeMillis()}"
            )
        }

        habitDao.updateHabit(updatedHabit.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun recordHabitProgress(habit: HabitEntity, date: Long, value: Double, note: String) {
        val isCompleted = value >= habit.target
        val percentage = (value / habit.target * 100).coerceAtMost(100.0)
        
        val normalizedDate = DateTimeUtils.getMidnightTimestamp(date)
        val existingProgress = habitDao.getProgress(habit.id, normalizedDate)
        
        val progressEntry = HabitProgressEntity(
            id = existingProgress?.id ?: 0,
            habitId = habit.id,
            date = normalizedDate,
            target = habit.target,
            actualValue = value,
            completed = isCompleted,
            percentage = percentage,
            note = note,
            updatedAt = System.currentTimeMillis()
        )
        
        habitDao.insertProgress(progressEntry)

        val completionRef = "habit_${habit.id}_complete_${normalizedDate}"

        if (isCompleted && existingProgress?.completed != true) {
            // New completion
            processPointEvent(
                TransactionType.COMPLETE_HABIT,
                GamificationEngine.HABIT_COMPLETION_REWARD,
                "Completed habit: ${habit.name}",
                habitId = habit.id,
                uniqueReference = completionRef
            )
            updateStreakAndMilestones(habit)
        } else if (!isCompleted && existingProgress?.completed == true) {
            // Un-completion (Toggle off)
            processPointEvent(
                TransactionType.MISSED_TARGET, // Using MISSED_TARGET to represent reversal for now
                -GamificationEngine.HABIT_COMPLETION_REWARD,
                "Habit un-ticked: ${habit.name}",
                habitId = habit.id,
                uniqueReference = "habit_${habit.id}_untick_${normalizedDate}"
            )
            updateStreakAndMilestones(habit)
        } else if (!isCompleted && habit.type == HabitType.MEASURABLE && existingProgress == null) {
            // Missed measurable target penalty (first time recording today)
            processPointEvent(
                TransactionType.MISSED_TARGET,
                GamificationEngine.TARGET_MISSED_PENALTY,
                "Target not achieved: ${habit.name}",
                habitId = habit.id,
                uniqueReference = "habit_${habit.id}_miss_${normalizedDate}"
            )
        }
    }

    private suspend fun updateStreakAndMilestones(habit: HabitEntity) {
        val allProgress = habitDao.getProgressForHabit(habit.id).first()
        val currentStreak = StreakEngine.calculateCurrentStreak(habit, allProgress)
        
        val activeCycle = habitDao.getActiveStreakCycle(habit.id) ?: return
        val updatedCycle = activeCycle.copy(
            currentLength = currentStreak,
            bestLength = Math.max(activeCycle.bestLength, currentStreak)
        )
        habitDao.updateStreakCycle(updatedCycle)
        
        val totalCompletionsCount = allProgress.count { it.completed }
        
        val updatedHabit = habit.copy(
            currentStreak = currentStreak,
            bestStreak = Math.max(habit.bestStreak, currentStreak),
            totalCompletions = totalCompletionsCount
        )
        habitDao.updateHabit(updatedHabit)
        
        checkMilestones(updatedHabit, updatedCycle)
    }

    private suspend fun checkMilestones(habit: HabitEntity, cycle: StreakCycleEntity) {
        val milestones = listOf(10, 20, 40, 60, 80, 100)
        milestones.forEach { m ->
            if (cycle.currentLength >= m) {
                val type = if (m == 10) TransactionType.STREAK_10_DAYS else TransactionType.STREAK_20_DAYS
                val amount = if (m == 10) GamificationEngine.STREAK_10_DAY_REWARD else GamificationEngine.STREAK_20_DAY_REWARD
                val ref = "habit_${habit.id}_cycle_${cycle.id}_milestone_$m"
                
                processPointEvent(
                    type, amount, "Reached $m-day streak for ${habit.name}",
                    habitId = habit.id,
                    streakCycleId = cycle.id,
                    uniqueReference = ref
                )

                // Optional: persist milestone record if not exists
                // habitDao.insertMilestone(...)
            }
        }
    }

    suspend fun createTask(task: TaskEntity): Long {
        val habitId = taskDao.insertTask(task)
        
        processPointEvent(
            TransactionType.CREATE_TASK,
            GamificationEngine.TASK_CREATION_COST,
            "Created task: ${task.title}",
            taskId = habitId,
            uniqueReference = "task_create_$habitId"
        )
        return habitId
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun completeTask(task: TaskEntity) {
        val completionRef = "task_${task.id}_complete"
        
        if (!task.completed) {
            // Mark as complete
            processPointEvent(
                TransactionType.COMPLETE_TASK,
                GamificationEngine.TASK_COMPLETION_REWARD,
                "Completed task: ${task.title}",
                taskId = task.id,
                uniqueReference = completionRef
            )
            taskDao.updateTask(task.copy(completed = true, completedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
        } else {
            // Mark as incomplete
            taskDao.updateTask(task.copy(completed = false, completedAt = null, updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun initializeUserData() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaultCategories = listOf(
                Category(name = "Health", icon = "health", color = 0xFF32FF57.toInt()),
                Category(name = "Gym", icon = "fitness_center", color = 0xFFFF0000.toInt()),
                Category(name = "Yoga", icon = "self_improvement", color = 0xFFFF00F4.toInt()),
                Category(name = "Study", icon = "school", color = 0xFF33E2FF.toInt()),
                Category(name = "Work", icon = "work", color = 0xFFFF9630.toInt()),
                Category(name = "Sleep", icon = "bedtime", color = 0xFF9158FF.toInt()),
                Category(name = "Finance", icon = "payments", color = 0xFFE5FF01.toInt()),
                Category(name = "Personal", icon = "person", color = 0xFF43D4CD.toInt())
            )
            defaultCategories.forEach { categoryDao.insertCategory(it) }
        }

        if (userProgress.first() == null) {
            userDao.insertUserProgress(UserProgressEntity())
            userDao.insertProfile(ProfileEntity())

            processPointEvent(
                TransactionType.STARTING_POINTS,
                GamificationEngine.STARTING_POINTS,
                "Starting balance",
                uniqueReference = "starting_points"
            )
        }
    }

    suspend fun deleteHabit(habitId: Long) {
        processPointEvent(
            TransactionType.DELETE_HABIT,
            GamificationEngine.DELETE_HABIT_COST,
            "Deleted habit",
            habitId = habitId,
            uniqueReference = "habit_delete_${habitId}_${System.currentTimeMillis()}"
        )
        habitDao.softDeleteHabit(habitId, System.currentTimeMillis())
    }

    suspend fun deleteTask(task: TaskEntity) {
        processPointEvent(
            TransactionType.DELETE_TASK,
            GamificationEngine.DELETE_TASK_COST,
            "Deleted task: ${task.title}",
            taskId = task.id,
            uniqueReference = "task_delete_${task.id}_${System.currentTimeMillis()}"
        )
        taskDao.deleteTask(task)
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        userDao.updateProfile(profile)
    }

    suspend fun clearAllData() {
        userDao.clearAllTransactions()
        notificationDao.clearAllNotifications()
        habitDao.clearAllProgress()
        habitDao.clearAllHabits()
        taskDao.clearAllTasks()
    }
}
