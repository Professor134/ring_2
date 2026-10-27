// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\CategoryDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ringapp.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the category dao logic for this component without modifying runtime behavior.
// Represents the category dao structure used by the application.
interface CategoryDao {
// Manages the observe all flow without changing runtime behavior.
    @Query("SELECT * FROM category WHERE deletedAt IS NULL ORDER BY name")
// Manages the observe all flow without changing runtime behavior.
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert
    suspend fun insert(category: CategoryEntity): Long
}