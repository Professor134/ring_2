// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\CreateCategoryUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.CategoryRepository
import javax.inject.Inject

// Handles the create category use case logic for this component without modifying runtime behavior.
// Represents the create category use case structure used by the application.
class CreateCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String, color: Int, icon: String? = null): Long = repository.create(name.trim(), color, icon)
}