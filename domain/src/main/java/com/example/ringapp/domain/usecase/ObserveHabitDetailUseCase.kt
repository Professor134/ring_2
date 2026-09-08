package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

data class HabitDetailData(val habit: HabitEntity, val progress: List<HabitProgressEntity>)

class ObserveHabitDetailUseCase @Inject constructor(private val repository: HabitRepository) {
    operator fun invoke(habitId: Long, rangeDays: Int): Flow<HabitDetailData?> {
        val end = startOfDay(System.currentTimeMillis())
        val start = end - rangeDays * DAY_MILLIS
        return combine(repository.observeById(habitId), repository.observeProgress(habitId, start, end)) { habit, progress ->
            habit?.let { HabitDetailData(it, progress) }
        }
    }

    private fun startOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    companion object { private const val DAY_MILLIS = 86_400_000L }
}