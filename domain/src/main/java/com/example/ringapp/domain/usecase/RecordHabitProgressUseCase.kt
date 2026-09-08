package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.repository.HabitRepository
import javax.inject.Inject

class RecordHabitProgressUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: HabitEntity, date: Long, actual: Int, note: String?) = repository.recordProgress(habit, date, actual, note)
}