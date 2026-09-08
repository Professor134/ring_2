package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.AchievementRepository
import javax.inject.Inject

class ObserveAchievementsUseCase @Inject constructor(private val repository: AchievementRepository) {
    operator fun invoke() = repository.observeAll()
}