// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObserveCategoriesUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.CategoryEntity
import com.example.ringapp.data.repository.CategoryRepository
import javax.inject.Inject

// Handles the observe categories use case logic for this component without modifying runtime behavior.
// Represents the observe categories use case structure used by the application.
class ObserveCategoriesUseCase @Inject constructor(private val repository: CategoryRepository) {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<List<CategoryEntity>> = repository.observeAll()
}
