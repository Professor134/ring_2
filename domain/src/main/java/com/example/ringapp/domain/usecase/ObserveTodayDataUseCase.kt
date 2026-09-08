package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.repository.TaskRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class TodayData(val progress: List<com.example.ringapp.data.local.entities.HabitProgressEntity>, val tasks: List<com.example.ringapp.data.local.entities.TaskEntity>)

class ObserveTodayDataUseCase @Inject constructor(private val habits: HabitRepository, private val tasks: TaskRepository) {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<TodayData> {
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return kotlinx.coroutines.flow.combine(habits.observeProgressForDate(start), tasks.observeToday(start, end)) { progress, todayTasks -> TodayData(progress, todayTasks) }
    }
}