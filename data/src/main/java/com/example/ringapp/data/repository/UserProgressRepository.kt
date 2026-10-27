// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\UserProgressRepository.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.UserProgressDao
import com.example.ringapp.data.local.entities.UserProgressEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

// Handles the user progress repository logic for this component without modifying runtime behavior.
// Represents the user progress repository structure used by the application.
class UserProgressRepository @Inject constructor(private val dao: UserProgressDao) {
// Manages the observe current flow without changing runtime behavior.
    fun observeCurrent(): Flow<UserProgressEntity?> = dao.observeCurrent()
}