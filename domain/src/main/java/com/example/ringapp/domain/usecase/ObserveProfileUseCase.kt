// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObserveProfileUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.ProfileRepository
import javax.inject.Inject

// Handles the observe profile use case logic for this component without modifying runtime behavior.
// Represents the observe profile use case structure used by the application.
class ObserveProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke() = repository.observeCurrent()
}