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
import java.time.LocalDate
import java.time.ZoneId
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
        val now = System.currentTimeMillis()
        if (original.target != habit.target) {
            check((progressDao.getCurrent()?.currentPoints ?: 0) >= 10) { "Not enough Elite Points to change the target" }
            pointRepository.record(TransactionType.CHANGE_TARGET, -10, "habit-target-${habit.id}-${habit.target}", "Habit target changed", habitId = habit.id, timestamp = now)
        }
        dao.update(habit.copy(updatedAt = now))
    }

    suspend fun recordProgress(habit: HabitEntity, date: Long, actual: Double, note: String?) = database.withTransaction {
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
            val updatedHabit = habit.copy(
                currentStreak = habit.currentStreak + 1,
                bestStreak = maxOf(habit.bestStreak, habit.currentStreak + 1),
                totalCompletions = habit.totalCompletions + 1,
                updatedAt = now
            )
            dao.update(updatedHabit)
            val amount = 4
            pointRepository.record(TransactionType.HABIT_COMPLETE, amount, "habit-complete-${habit.id}-$date-$now", "Habit completed", habitId = habit.id, timestamp = now)
        } else if (!completed && existing?.completed == true) {
            val updatedHabit = habit.copy(
                currentStreak = (habit.currentStreak - 1).coerceAtLeast(0),
                totalCompletions = (habit.totalCompletions - 1).coerceAtLeast(0),
                updatedAt = now
            )
            dao.update(updatedHabit)
            val amount = -4
            pointRepository.record(TransactionType.HABIT_UNCOMPLETE, amount, "habit-uncomplete-${habit.id}-$date-$now", "Habit completion reverted", habitId = habit.id, timestamp = now)
        } else if (!completed && existing == null) {
            pointRepository.record(TransactionType.MISSED_TARGET, -5, "habit-missed-${habit.id}-$date", "Habit target missed", habitId = habit.id, timestamp = now)
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

    suspend fun seedDefaults() = database.withTransaction {
        val currentProgress = progressDao.getCurrent()
        if (currentProgress == null) {
            progressDao.insert(com.example.ringapp.data.local.entities.UserProgressEntity(currentPoints = 500, lifetimePoints = 500, level = 1, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
            pointRepository.record(TransactionType.STARTING_POINTS, 500, "initial-points", "Welcome bonus points")
        }
        
        val habitsList = dao.observeActive().first()
        if (habitsList.isNotEmpty()) return@withTransaction
        
        val categories = listOf(
            Triple("Health", 0xFF2E7D32.toInt(), "fitness"),
            Triple("Personal", 0xFF1565C0.toInt(), "person"),
            Triple("Work", 0xFFC62828.toInt(), "work"),
            Triple("Study", 0xFF6A1B9A.toInt(), "school"),
            Triple("Finance", 0xFFEF6C00.toInt(), "payments")
        )
        
        val categoryIds = categories.map { (name, color, icon) ->
            val now = System.currentTimeMillis()
            database.categoryDao().insert(com.example.ringapp.data.local.entities.CategoryEntity(name = name, color = color, icon = icon, createdAt = now, updatedAt = now))
        }
        
        val healthId = categoryIds[0]
        val personalId = categoryIds[1]
        
        val now = System.currentTimeMillis()
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val defaults = listOf(
            HabitEntity(name = "Drink Water", categoryId = healthId, type = com.example.ringapp.data.local.entities.HabitType.MEASURABLE, target = 8.0, unit = "Glasses", scheduleType = com.example.ringapp.data.local.entities.ScheduleType.DAILY, startDate = today, color = 0xFF2E7D32.toInt(), createdAt = now, updatedAt = now),
            HabitEntity(name = "Morning Exercise", categoryId = healthId, type = com.example.ringapp.data.local.entities.HabitType.YES_NO, target = 1.0, scheduleType = com.example.ringapp.data.local.entities.ScheduleType.DAILY, startDate = today, color = 0xFF2E7D32.toInt(), createdAt = now, updatedAt = now),
            HabitEntity(name = "Read 10 Pages", categoryId = personalId, type = com.example.ringapp.data.local.entities.HabitType.MEASURABLE, target = 10.0, unit = "Pages", scheduleType = com.example.ringapp.data.local.entities.ScheduleType.DAILY, startDate = today, color = 0xFF1565C0.toInt(), createdAt = now, updatedAt = now),
            HabitEntity(name = "Meditate", categoryId = personalId, type = com.example.ringapp.data.local.entities.HabitType.YES_NO, target = 1.0, scheduleType = com.example.ringapp.data.local.entities.ScheduleType.DAILY, startDate = today, color = 0xFF1565C0.toInt(), createdAt = now, updatedAt = now)
        )
        
        defaults.forEach { dao.insert(it) }
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
        pointRepository.record(TransactionType.HABIT_DELETE, -50, "habit-delete-$habitId-$deletedAt", "Habit deleted", habitId = habitId, timestamp = deletedAt)
        dao.softDelete(habitId, deletedAt)
    }
}
