// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ClearUserDataUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.UserDataRepository
import javax.inject.Inject

// Handles the clear user data use case logic for this component without modifying runtime behavior.
// Represents the clear user data use case structure used by the application.
class ClearUserDataUseCase @Inject constructor(private val repository: UserDataRepository) {
    suspend operator fun invoke() = repository.clearUserData()
}
