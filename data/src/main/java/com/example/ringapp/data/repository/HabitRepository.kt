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
            check((progressDao.getCurrent()?.currentPoints ?: 0) >= 10) { "Not enough Elite Points to change the target" }
            pointRepository.record(TransactionType.CHANGE_TARGET, -10, "habit-target-${habit.id}-${habit.target}", "Habit target changed", habitId = habit.id, timestamp = now)
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
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getDefault()
        val completedDatesStr = allProgress.filter { it.completed }.map { sdf.format(java.util.Date(it.date)) }.toSet()

        val todayCal = java.util.Calendar.getInstance()
        val todayStr = sdf.format(todayCal.time)
        
        var calculatedCurrentStreak = 0
        val checkCal = java.util.Calendar.getInstance()
        if (completedDatesStr.contains(todayStr)) {
            checkCal.time = todayCal.time
            while (completedDatesStr.contains(sdf.format(checkCal.time))) {
                calculatedCurrentStreak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
        } else {
            checkCal.time = todayCal.time
            checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            while (completedDatesStr.contains(sdf.format(checkCal.time))) {
                calculatedCurrentStreak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
        }

        val sortedTimestamps = allProgress.filter { it.completed }.map { it.date }.distinct().sorted()
        var calculatedBestStreak = 0
        var currentRun = 0
        var lastCal: java.util.Calendar? = null
        
        for (ts in sortedTimestamps) {
            val currentCal = java.util.Calendar.getInstance()
            currentCal.timeInMillis = ts
            
            if (lastCal == null) {
                currentRun = 1
            } else {
                val prevCal = java.util.Calendar.getInstance()
                prevCal.time = lastCal.time
                prevCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                if (sdf.format(currentCal.time) == sdf.format(prevCal.time)) {
                    currentRun++
                } else if (sdf.format(currentCal.time) != sdf.format(lastCal.time)) {
                    currentRun = 1
                }
            }
            calculatedBestStreak = maxOf(calculatedBestStreak, currentRun)
            lastCal = currentCal
        }
        calculatedBestStreak = maxOf(calculatedBestStreak, calculatedCurrentStreak)

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
        
        // Remove specific old default habits
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
            
            // Create a "System" category for internal use
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

    suspend fun recordProgress(progress: HabitProgressEntity) = dao.upsertProgress(progress)
    suspend fun complete(habit: HabitEntity, progress: HabitProgressEntity) = database.withTransaction {
        if (dao.progressForDate(habit.id, progress.date) != null) return@withTransaction false
        dao.update(habit)
        dao.upsertProgress(progress)
        val now = progress.updatedAt
        val amount = 4
        pointRepository.record(TransactionType.HABIT_COMPLETE, amount, "habit-complete-${habit.id}-${progress.date}-$now", "Habit completed", habitId = habit.id, timestamp = now)
        true
    }
    suspend fun delete(habitId: Long, deletedAt: Long) = database.withTransaction {
        val habit = dao.observeById(habitId).first() ?: return@withTransaction
        check(!habit.isStepsHabit()) { "The Steps habit cannot be deleted" }
        pointRepository.record(TransactionType.HABIT_DELETE, -50, "habit-delete-$habitId-$deletedAt", "Habit deleted", habitId = habitId, timestamp = deletedAt)
        dao.softDelete(habitId, deletedAt)
    }
}
