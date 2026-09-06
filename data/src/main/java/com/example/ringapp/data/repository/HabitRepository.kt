package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.HabitDao
import com.example.ringapp.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val dao: HabitDao
) {
    fun observeActive(): Flow<List<HabitEntity>> = dao.observeActive()
    suspend fun create(habit: HabitEntity): Long = dao.insert(habit)
    suspend fun update(habit: HabitEntity) = dao.update(habit)
    suspend fun delete(habitId: Long, deletedAt: Long) = dao.softDelete(habitId, deletedAt)
}
