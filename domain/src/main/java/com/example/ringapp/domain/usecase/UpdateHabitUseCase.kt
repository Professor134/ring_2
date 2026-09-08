package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.HabitEntity
import com.example.ringapp.data.repository.HabitRepository
import javax.inject.Inject

class UpdateHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: HabitEntity) = repository.update(habit)
}