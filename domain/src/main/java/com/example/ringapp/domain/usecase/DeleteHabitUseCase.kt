package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import javax.inject.Inject

class DeleteHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long) = repository.delete(habitId, System.currentTimeMillis())
}