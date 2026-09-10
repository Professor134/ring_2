package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.domain.engine.ScheduleEngine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ToggleHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: HabitEntity) {
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayDate = LocalDate.now()
        check(ScheduleEngine.isActiveOnDate(habit, todayDate)) { "This habit is not scheduled today" }
        repository.toggleHabit(habit, today)
    }
}
