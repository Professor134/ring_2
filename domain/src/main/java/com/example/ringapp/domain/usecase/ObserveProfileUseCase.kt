package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.ProfileRepository
import javax.inject.Inject

class ObserveProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke() = repository.observeCurrent()
}