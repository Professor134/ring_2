package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.repository.TaskRepository
import com.example.ringapp.domain.engine.ScheduleEngine
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class TodayData(val progress: List<com.example.ringapp.data.local.entities.HabitProgressEntity>, val tasks: List<com.example.ringapp.data.local.entities.TaskEntity>)

class ObserveTodayDataUseCase @Inject constructor(private val habits: HabitRepository, private val tasks: TaskRepository) {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<TodayData> {
        val today = LocalDate.now()
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return combine(habits.observeProgressForDate(start), tasks.observeActive()) { progress, allTasks ->
            val todayTasks = allTasks.filter { ScheduleEngine.isActiveOnDate(it, today) }
            TodayData(progress, todayTasks)
        }
    }
}
