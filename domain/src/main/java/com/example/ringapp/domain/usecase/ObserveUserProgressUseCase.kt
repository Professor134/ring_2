// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObserveUserProgressUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.UserProgressRepository
import javax.inject.Inject

// Handles the observe user progress use case logic for this component without modifying runtime behavior.
// Represents the observe user progress use case structure used by the application.
class ObserveUserProgressUseCase @Inject constructor(private val repository: UserProgressRepository) {
    operator fun invoke() = repository.observeCurrent()
}