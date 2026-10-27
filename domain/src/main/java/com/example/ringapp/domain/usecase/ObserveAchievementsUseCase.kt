// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObserveAchievementsUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.AchievementRepository
import javax.inject.Inject

// Handles the observe achievements use case logic for this component without modifying runtime behavior.
// Represents the observe achievements use case structure used by the application.
class ObserveAchievementsUseCase @Inject constructor(private val repository: AchievementRepository) {
    operator fun invoke() = repository.observeAll()
}