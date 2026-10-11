// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObservePointTransactionsUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.PointRepository
import javax.inject.Inject

// Handles the observe point transactions use case logic for this component without modifying runtime behavior.
// Represents the observe point transactions use case structure used by the application.
class ObservePointTransactionsUseCase @Inject constructor(private val repository: PointRepository) {
    operator fun invoke() = repository.observeAll()
}