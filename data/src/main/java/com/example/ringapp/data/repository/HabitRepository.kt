// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\HabitRepository.kt
// Purpose: Handles habit creation, progress updates, streak calculations, and points rules.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.engine.StreakEngine
import com.example.ringapp.data.local.dao.HabitDao
import com.example.ringapp.data.local.dao.PointTransactionDao
import com.example.ringapp.data.local.dao.UserProgressDao
import com.example.ringapp.data.local.entities.CategoryEntity
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.local.entities.HabitType
import com.example.ringapp.data.local.entities.PointTransactionEntity
import com.example.ringapp.data.local.entities.ScheduleType
import com.example.ringapp.data.local.entities.TransactionType
import com.example.ringapp.data.local.entities.UserProgressEntity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HabitRepository @Inject constructor(
    private val dao: HabitDao,
    private val pointDao: PointTransactionDao,
    private val progressDao: UserProgressDao,
    private val database: AppDatabase,
    private val pointRepository: PointRepository
) {
    fun observeActive(): Flow<List<HabitEntity>> = dao.observeActive()
    fun observeById(habitId: Long): Flow<HabitEntity?> = dao.observeById(habitId)
    fun observeProgress(habitId: Long, from: Long, to: Long): Flow<List<HabitProgressEntity>> = dao.observeProgress(habitId, from, to)
    fun observeProgressForDate(date: Long): Flow<List<HabitProgressEntity>> = dao.observeProgressForDate(date)
    fun observeProgressRange(from: Long, to: Long): Flow<List<HabitProgressEntity>> = dao.observeProgressRange(from, to)
    fun observeTransactions(habitId: Long): Flow<List<PointTransactionEntity>> = pointDao.observeForHabit(habitId)

    suspend fun create(habit: HabitEntity): Long = database.withTransaction {
        val habitId = dao.insert(habit)
        pointRepository.record(
            type = TransactionType.HABIT_CREATE,
            amount = -25,
            reference = "habit-create-$habitId",
            description = "Created habit: ${habit.name}",
            habitId = habitId
        )
        habitId
    }

    suspend fun update(habit: HabitEntity) = database.withTransaction {
        val original = dao.observeById(habit.id).first() ?: return@withTransaction
        check(!original.isStepsHabit()) { "The Steps habit cannot be edited" }
        val now = System.currentTimeMillis()
        if (original.target != habit.target) {
            if (habit.target < original.target) {
                pointRepository.record(
                    type = TransactionType.CHANGE_TARGET,
                    amount = -10,
                    reference = "habit-target-${habit.id}-${habit.target}-$now",
                    description = "Lowered habit target for ${habit.name}",
                    habitId = habit.id,
                    timestamp = now
                )
            }
        }
        dao.update(habit.copy(updatedAt = now))
    }

    suspend fun getProgressForDate(habitId: Long, date: Long): HabitProgressEntity? = dao.progressForDate(habitId, date)

    suspend fun recordSteps(steps: Int) = database.withTransaction {
        val habit = dao.observeActive().first().find { it.isStepsHabit() } ?: return@withTransaction
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
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
        val calculatedCurrentStreak = StreakEngine.calculateCurrentStreak(allProgress)
        val calculatedBestStreak = StreakEngine.calculateBestStreak(allProgress)

        val updatedHabit = habit.copy(
            currentStreak = calculatedCurrentStreak,
            bestStreak = maxOf(habit.bestStreak, calculatedBestStreak),
            totalCompletions = allProgress.count { it.completed },
            updatedAt = now
        )
        dao.update(updatedHabit)

        // Step reward: 1 point per 1,000 steps multiplied by streak (minimum effective streak of 1)
        val effectiveStreak = if (updatedHabit.currentStreak > 0) updatedHabit.currentStreak else 1
        val pointsPerBucket = 1 * effectiveStreak
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(date))
        val completedBuckets = steps / 1000

        for (i in 1..completedBuckets) {
            pointRepository.record(
                type = TransactionType.HABIT_COMPLETE,
                amount = pointsPerBucket,
                reference = "steps-$dateStr-bucket-$i",
                description = "Walked ${i * 1000} steps (streak: $effectiveStreak)",
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
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(date))

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

        val allProgress = dao.getProgressForHabit(habit.id)
        val calculatedCurrentStreak = StreakEngine.calculateCurrentStreak(allProgress)
        val calculatedBestStreak = StreakEngine.calculateBestStreak(allProgress)

        val updatedHabit = habit.copy(
            currentStreak = calculatedCurrentStreak,
            bestStreak = maxOf(habit.bestStreak, calculatedBestStreak),
            totalCompletions = allProgress.count { it.completed },
            updatedAt = now
        )
        dao.update(updatedHabit)

        val effectiveStreak = updatedHabit.currentStreak
        val prevCompleted = existing?.completed == true
        val prevPartial = existing != null && existing.actual > 0.0 && !prevCompleted

        if (habit.type == HabitType.YES_NO) {
            if (completed && !prevCompleted) {
                pointRepository.record(
                    type = TransactionType.HABIT_COMPLETE,
                    amount = 3,
                    reference = "habit-complete-${habit.id}-$dateStr-$now",
                    description = "Completed habit: ${habit.name}",
                    habitId = habit.id,
                    timestamp = now
                )
            } else if (!completed && prevCompleted) {
                pointRepository.record(
                    type = TransactionType.HABIT_UNCOMPLETE,
                    amount = -3,
                    reference = "habit-uncomplete-${habit.id}-$dateStr-$now",
                    description = "Reverted completion for ${habit.name}",
                    habitId = habit.id,
                    timestamp = now
                )
            }
        } else {
            // Measurable Habit Points & Reconciliation
            val fullReward = if (effectiveStreak > 0) Math.round((1.5 * effectiveStreak) + 3).toInt() else 3
            val firstIncompleteReward = if (effectiveStreak > 0) (2 * effectiveStreak) + 3 else 3

            // 1. First-recorded-incomplete reward (granted once per habit-day)
            if (actual > 0.0 && actual < habit.target) {
                pointRepository.record(
                    type = TransactionType.HABIT_COMPLETE,
                    amount = firstIncompleteReward,
                    reference = "habit-first-incomplete-${habit.id}-$dateStr",
                    description = "First recorded incomplete progress",
                    habitId = habit.id,
                    timestamp = now
                )
            }

            // 2. Partial vs Full Completion Reward Reconciliation
            if (completed) {
                if (prevPartial) {
                    // Reverse partial completion reward
                    pointRepository.record(
                        type = TransactionType.HABIT_UNCOMPLETE,
                        amount = -1,
                        reference = "reversal-habit-partial-${habit.id}-$dateStr-$now",
                        description = "Replaced partial progress reward",
                        habitId = habit.id,
                        timestamp = now
                    )
                }
                // Award full completion reward
                pointRepository.record(
                    type = TransactionType.HABIT_COMPLETE,
                    amount = fullReward,
                    reference = "habit-complete-${habit.id}-$dateStr-$now",
                    description = "Completed measurable habit: ${habit.name}",
                    habitId = habit.id,
                    timestamp = now
                )
            } else if (actual > 0.0) { // Partial completion state
                if (prevCompleted) {
                    // Reverse full completion reward
                    pointRepository.record(
                        type = TransactionType.HABIT_UNCOMPLETE,
                        amount = -fullReward,
                        reference = "reversal-habit-complete-${habit.id}-$dateStr-$now",
                        description = "Reverted full completion reward",
                        habitId = habit.id,
                        timestamp = now
                    )
                }
                // Check if partial reward has already been awarded on this date
                val alreadyPartial = pointDao.findByReference("habit-partial-${habit.id}-$dateStr") != null
                if (!alreadyPartial || prevCompleted) {
                    pointRepository.record(
                        type = TransactionType.HABIT_COMPLETE,
                        amount = 1,
                        reference = "habit-partial-${habit.id}-$dateStr",
                        description = "Partial progress recorded",
                        habitId = habit.id,
                        timestamp = now
                    )
                }
            } else { // Zero progress state
                if (prevCompleted) {
                    pointRepository.record(
                        type = TransactionType.HABIT_UNCOMPLETE,
                        amount = -fullReward,
                        reference = "reversal-habit-complete-${habit.id}-$dateStr-$now",
                        description = "Reverted full completion reward",
                        habitId = habit.id,
                        timestamp = now
                    )
                } else if (prevPartial) {
                    pointRepository.record(
                        type = TransactionType.HABIT_UNCOMPLETE,
                        amount = -1,
                        reference = "reversal-habit-partial-${habit.id}-$dateStr-$now",
                        description = "Reverted partial progress reward",
                        habitId = habit.id,
                        timestamp = now
                    )
                }
            }
        }
    }

    suspend fun toggleHabit(habit: HabitEntity, date: Long) = database.withTransaction {
        val existing = dao.progressForDate(habit.id, date)
        if (existing?.completed == true) {
            recordProgress(habit, date, 0.0, existing.note)
        } else {
            recordProgress(habit, date, habit.target, existing?.note)
        }
    }

    suspend fun processSkippedDayPenalties() = database.withTransaction {
        val stepHabit = dao.observeActive().first().find { it.isStepsHabit() } ?: return@withTransaction
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

        // Fix: Only check past days ON OR AFTER stepHabit.startDate up to yesterday!
        // Prevents deducting penalties for days prior to app installation.
        val habitStartDate = java.time.Instant.ofEpochMilli(stepHabit.startDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val yesterday = today.minusDays(1)
        if (habitStartDate.isAfter(yesterday)) return@withTransaction

        var checkDate = habitStartDate
        while (!checkDate.isAfter(yesterday)) {
            val pastDateStr = checkDate.format(formatter)
            val pastDateMillis = checkDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val progress = dao.progressForDate(stepHabit.id, pastDateMillis)
            if (progress == null || progress.actual < 1000.0) {
                pointRepository.record(
                    type = TransactionType.MISSED_TARGET,
                    amount = -10,
                    reference = "steps-skipped-penalty-$pastDateStr",
                    description = "Skipped step tracking goal on $pastDateStr",
                    habitId = stepHabit.id,
                    timestamp = System.currentTimeMillis()
                )
            }
            checkDate = checkDate.plusDays(1)
        }
    }

    suspend fun seedDefaults() = database.withTransaction {
        val currentProgress = progressDao.getCurrent()
        if (currentProgress == null) {
            progressDao.insert(
                UserProgressEntity(
                    currentPoints = 500,
                    lifetimePoints = 500,
                    level = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
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
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val today = cal.timeInMillis

            val systemCategoryId = database.categoryDao().insert(
                CategoryEntity(
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
                    type = HabitType.MEASURABLE,
                    target = 0.0,
                    unit = "Steps",
                    scheduleType = ScheduleType.DAILY,
                    startDate = today,
                    color = HabitEntity.PLATINUM_COLOR,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun delete(habitId: Long, deletedAt: Long) = database.withTransaction {
        val habit = dao.observeById(habitId).first() ?: return@withTransaction
        check(!habit.isStepsHabit()) { "The Steps habit cannot be deleted" }
        pointRepository.record(
            type = TransactionType.HABIT_DELETE,
            amount = -30,
            reference = "habit-delete-$habitId-$deletedAt",
            description = "Deleted habit: ${habit.name}",
            habitId = habitId,
            timestamp = deletedAt
        )
        dao.softDelete(habitId, deletedAt)
    }
}
