package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.PointRepository
import javax.inject.Inject

class ObservePointTransactionsUseCase @Inject constructor(private val repository: PointRepository) {
    operator fun invoke() = repository.observeAll()
}