// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\CategoryRepository.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.CategoryDao
import com.example.ringapp.data.local.entities.CategoryConstants
import com.example.ringapp.data.local.entities.CategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// Handles the category repository logic for this component without modifying runtime behavior.
// Represents the category repository structure used by the application.
class CategoryRepository @Inject constructor(private val dao: CategoryDao) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var seedingTriggered = false

// Manages the observe all flow without changing runtime behavior.
    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll().map { categories ->
        val existingNames = categories.map { it.name.lowercase() }
        val missing = CategoryConstants.ALL_CATEGORIES.filter { it.name.lowercase() !in existingNames }
        
        if (missing.isNotEmpty() && !seedingTriggered) {
            seedingTriggered = true
            seedMissingCategories(missing)
        }
        
        categories.filter { cat -> 
            CategoryConstants.ALL_CATEGORIES.any { it.name.equals(cat.name, ignoreCase = true) } 
        }
    }

// Manages the seed missing categories flow without changing runtime behavior.
    private fun seedMissingCategories(missing: List<CategoryConstants.FixedCategory>) {
        scope.launch {
            val now = System.currentTimeMillis()
            val entities = missing.map { 
                CategoryEntity(name = it.name, color = it.color, icon = it.icon, createdAt = now, updatedAt = now)
            }
            dao.insertAll(entities)
        }
    }

    suspend fun create(name: String, color: Int, icon: String? = null): Long {
        val now = System.currentTimeMillis()
        return dao.insert(CategoryEntity(name = name, color = color, icon = icon, createdAt = now, updatedAt = now))
    }
}
