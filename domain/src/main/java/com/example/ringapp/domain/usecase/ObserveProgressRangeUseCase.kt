package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.HabitProgressEntity
import com.example.ringapp.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProgressRangeUseCase @Inject constructor(private val repository: HabitRepository) {
    operator fun invoke(from: Long, to: Long): Flow<List<HabitProgressEntity>> = repository.observeProgressRange(from, to)
}