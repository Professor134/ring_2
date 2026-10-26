package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.HabitRepository
import javax.inject.Inject

class ObserveHabitTransactionsUseCase @Inject constructor(private val repository: HabitRepository) {
    operator fun invoke(habitId: Long) = repository.observeTransactions(habitId)
}
