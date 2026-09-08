package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.UserProgressRepository
import javax.inject.Inject

class ObserveUserProgressUseCase @Inject constructor(private val repository: UserProgressRepository) {
    operator fun invoke() = repository.observeCurrent()
}