// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\UpdateProfileUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.data.repository.ProfileRepository
import javax.inject.Inject

// Handles the update profile use case logic for this component without modifying runtime behavior.
// Represents the update profile use case structure used by the application.
class UpdateProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(profile: ProfileEntity) = repository.update(profile.copy(updatedAt = System.currentTimeMillis()))
}