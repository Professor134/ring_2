// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\HabitRepository.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.HabitDao
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import kotlinx.coroutines.flow.Flow
import com.example.ringapp.data.db.AppDatabase
import androidx.room.withTransaction
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.TransactionType
import com.example.ringapp.data.local.dao.PointTransactionDao
import com.example.ringapp.data.local.dao.UserProgressDao
import javax.inject.Inject
import kotlinx.coroutines.flow.first

// Handles the habit repository logic for this component without modifying runtime behavior.
// Represents the habit repository structure used by the application.
class HabitRepository @Inject constructor(
    private val dao: HabitDao,
    private val pointDao: PointTransactionDao,
    private val progressDao: UserProgressDao,
    private val database: AppDatabase,
    private val pointRepository: PointRepository
) {
// Manages the observe active flow without changing runtime behavior.
    fun observeActive(): Flow<List<HabitEntity>> = dao.observeActive()
// Manages the observe by id flow without changing runtime behavior.
    fun observeById(habitId: Long): Flow<HabitEntity?> = dao.observeById(habitId)
// Manages the observe progress flow without changing runtime behavior.
    fun observeProgress(habitId: Long, from: Long, to: Long): Flow<List<HabitProgressEntity>> = dao.observeProgress(habitId, from, to)
// Manages the observe progress for date flow without changing runtime behavior.
    fun observeProgressForDate(date: Long): Flow<List<HabitProgressEntity>> = dao.observeProgressForDate(date)
// Manages the observe progress range flow without changing runtime behavior.
    fun observeProgressRange(from: Long, to: Long): Flow<List<HabitProgressEntity>> = dao.observeProgressRange(from, to)
// Manages the observe transactions flow without changing runtime behavior.
    fun observeTransactions(habitId: Long): Flow<List<PointTransactionEntity>> = pointDao.observeForHabit(habitId)
    suspend fun create(habit: HabitEntity): Long = database.withTransaction {
        val current = progressDao.getCurrent()
        check((current?.currentPoints ?: 0) >= 25) { "Not enough Elite Points to create a habit" }
        val habitId = dao.insert(habit)
        pointRepository.record(TransactionType.HABIT_CREATE, -25, "habit-create-$habitId", "Habit created", habitId = habitId)
        habitId
    }
    suspend fun update(habit: HabitEntity) = database.withTransaction {
        val original = dao.observeById(habit.id).first() ?: return@withTransaction
        check(!original.isStepsHabit()) { "The Steps habit cannot be edited" }
        val now = System.currentTimeMillis()
        if (original.target != habit.target) {
            if (habit.target > original.target) {
                // If new target is greater than old, no point reduction
            } else {
                check((progressDao.getCurrent()?.currentPoints ?: 0) >= 10) { "Not enough Elite Points to change the target" }
                pointRepository.record(TransactionType.CHANGE_TARGET, -10, "habit-target-${habit.id}-${habit.target}-$now", "Habit target changed", habitId = habit.id, timestamp = now)
            }
        }
        dao.update(habit.copy(updatedAt = now))
    }

    suspend fun getProgressForDate(habitId: Long, date: Long): HabitProgressEntity? = dao.progressForDate(habitId, date)

    suspend fun recordSteps(steps: Int) = database.withTransaction {
        val habit = dao.observeActive().first().find { it.isStepsHabit() } ?: return@withTransaction
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val date = cal.timeInMillis
        val now = System.currentTimeMillis()
        val existing = dao.progressForDate(habit.id, date)
        
        val progress = HabitProgressEntity(
            id = existing?.id ?: 0,
            habitId = habit.id,
            date = date,
            target = 10000.0, 
            actual = steps.toDouble(),
            percentage = (steps * 100 / 10000).coerceIn(0, 100),
            completed = steps >= 10000,
            note = null,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        dao.upsertProgress(progress)
        
        val allProgress = dao.getProgressForHabit(habit.id)
        val calculatedCurrentStreak = com.example.ringapp.data.engine.StreakEngine.calculateCurrentStreak(allProgress)
        val calculatedBestStreak = com.example.ringapp.data.engine.StreakEngine.calculateBestStreak(allProgress)

        val updatedHabit = habit.copy(
            currentStreak = calculatedCurrentStreak,
            bestStreak = maxOf(habit.bestStreak, calculatedBestStreak),
            totalCompletions = allProgress.count { it.completed },
            updatedAt = now
        )
        dao.update(updatedHabit)

        // Points: 1 point per 1000 steps
        val currentPointsToAward = steps / 1000
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        for (i in 1..currentPointsToAward) {
            pointRepository.record(
                TransactionType.HABIT_COMPLETE, 
                1, 
                "steps-$dateStr-bucket-$i", 
                "Walked ${i * 1000} steps", 
                habitId = habit.id, 
                timestamp = now
            )
        }
    }

    suspend fun recordProgress(habit: HabitEntity, date: Long, actual: Double, note: String?) = database.withTransaction {
        check(!habit.isStepsHabit()) { "Steps are recorded automatically" }
        val now = System.currentTimeMillis()
        val existing = dao.progressForDate(habit.id, date)
        val completed = actual >= habit.target
        val progress = HabitProgressEntity(
            id = existing?.id ?: 0,
            habitId = habit.id,
            date = date,
            target = habit.target,
            actual = actual.coerceAtLeast(0.0),
            percentage = if (habit.target > 0) ((actual * 100) / habit.target).toInt().coerceIn(0, 100) else 0,
            completed = completed,
            note = note,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        dao.upsertProgress(progress)
        
        if (completed && existing?.completed != true) {
            val amount = 4
            pointRepository.record(TransactionType.HABIT_COMPLETE, amount, "habit-complete-${habit.id}-$date-$now", "Habit completed", habitId = habit.id, timestamp = now)
        } else if (!completed && existing?.completed == true) {
            val amount = -4
            pointRepository.record(TransactionType.HABIT_UNCOMPLETE, amount, "habit-uncomplete-${habit.id}-$date-$now", "Habit completion reverted", habitId = habit.id, timestamp = now)
        } else if (!completed && existing == null) {
            pointRepository.record(TransactionType.MISSED_TARGET, -5, "habit-missed-${habit.id}-$date", "Habit target missed", habitId = habit.id, timestamp = now)
        }

        val allProgress = dao.getProgressForHabit(habit.id)
        val calculatedCurrentStreak = com.example.ringapp.data.engine.StreakEngine.calculateCurrentStreak(allProgress)
        val calculatedBestStreak = com.example.ringapp.data.engine.StreakEngine.calculateBestStreak(allProgress)

        val updatedHabit = habit.copy(
            currentStreak = calculatedCurrentStreak,
            bestStreak = maxOf(habit.bestStreak, calculatedBestStreak),
            totalCompletions = allProgress.count { it.completed },
            updatedAt = now
        )
        dao.update(updatedHabit)
    }

    suspend fun toggleHabit(habit: HabitEntity, date: Long) = database.withTransaction {
        val existing = dao.progressForDate(habit.id, date)
        if (existing?.completed == true) {
            recordProgress(habit, date, 0.0, existing.note)
        } else {
            recordProgress(habit, date, habit.target, existing?.note)
        }
    }

    suspend fun seedDefaults() = database.withTransaction {
        val currentProgress = progressDao.getCurrent()
        if (currentProgress == null) {
            progressDao.insert(com.example.ringapp.data.local.entities.UserProgressEntity(currentPoints = 500, lifetimePoints = 500, level = 1, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
            pointRepository.record(TransactionType.STARTING_POINTS, 500, "initial-points", "Welcome bonus points")
        }
        
        val allHabits = dao.observeActive().first()
        
        val defaultNames = listOf("Drink Water", "Morning Exercise", "Read 10 Pages", "Meditate", "Morning Walk")
        allHabits.filter { active ->
            defaultNames.any { it.equals(active.name, ignoreCase = true) }
        }.forEach { 
            dao.softDelete(it.id, System.currentTimeMillis()) 
        }

        val stepsExists = allHabits.any { it.isStepsHabit() }
        if (!stepsExists) {
            val now = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val today = cal.timeInMillis
            
            val systemCategoryId = database.categoryDao().insert(
                com.example.ringapp.data.local.entities.CategoryEntity(
                    name = "System", 
                    color = 0xFFE5E4E2.toInt(), 
                    icon = "directions_run", 
                    createdAt = now, 
                    updatedAt = now
                )
            )

            dao.insert(
                HabitEntity(
                    id = HabitEntity.STEPS_HABIT_ID,
                    name = HabitEntity.STEPS_HABIT_NAME,
                    categoryId = systemCategoryId,
                    type = com.example.ringapp.data.local.entities.HabitType.MEASURABLE,
                    target = 0.0, 
                    unit = "Steps",
                    scheduleType = com.example.ringapp.data.local.entities.ScheduleType.DAILY,
                    startDate = today,
                    color = HabitEntity.PLATINUM_COLOR,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun recordProgress(progress: HabitProgressEntity) = database.withTransaction {
        dao.upsertProgress(progress)
        val habit = dao.observeById(progress.habitId).first() ?: return@withTransaction
        val allProgress = dao.getProgressForHabit(habit.id)
        val calculatedCurrentStreak = com.example.ringapp.data.engine.StreakEngine.calculateCurrentStreak(allProgress)
        val calculatedBestStreak = com.example.ringapp.data.engine.StreakEngine.calculateBestStreak(allProgress)

        val updatedHabit = habit.copy(
            currentStreak = calculatedCurrentStreak,
            bestStreak = maxOf(habit.bestStreak, calculatedBestStreak),
            totalCompletions = allProgress.count { it.completed },
            updatedAt = progress.updatedAt
        )
        dao.update(updatedHabit)
    }

    suspend fun complete(habit: HabitEntity, progress: HabitProgressEntity) = database.withTransaction {
        if (dao.progressForDate(habit.id, progress.date) != null) return@withTransaction false
        dao.upsertProgress(progress)
        val allProgress = dao.getProgressForHabit(habit.id)
        val calculatedCurrentStreak = com.example.ringapp.data.engine.StreakEngine.calculateCurrentStreak(allProgress)
        val calculatedBestStreak = com.example.ringapp.data.engine.StreakEngine.calculateBestStreak(allProgress)

        val updatedHabit = habit.copy(
            currentStreak = calculatedCurrentStreak,
            bestStreak = maxOf(habit.bestStreak, calculatedBestStreak),
            totalCompletions = allProgress.count { it.completed },
            updatedAt = progress.updatedAt
        )
        dao.update(updatedHabit)

        val now = progress.updatedAt
        val amount = 4
        pointRepository.record(TransactionType.HABIT_COMPLETE, amount, "habit-complete-${habit.id}-${progress.date}-$now", "Habit completed", habitId = habit.id, timestamp = now)
        true
    }
    suspend fun delete(habitId: Long, deletedAt: Long) = database.withTransaction {
        val habit = dao.observeById(habitId).first() ?: return@withTransaction
        check(!habit.isStepsHabit()) { "The Steps habit cannot be deleted" }
        pointRepository.record(TransactionType.HABIT_DELETE, -30, "habit-delete-$habitId-$deletedAt", "Habit deleted", habitId = habitId, timestamp = deletedAt)
        dao.softDelete(habitId, deletedAt)
    }
}
