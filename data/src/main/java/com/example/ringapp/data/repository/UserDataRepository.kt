// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\UserDataRepository.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import javax.inject.Inject

// Handles the user data repository logic for this component without modifying runtime behavior.
// Represents the user data repository structure used by the application.
class UserDataRepository @Inject constructor(private val database: AppDatabase) {
    suspend fun clearUserData() {
        database.withTransaction {
            database.clearAllTables()
        }
    }
}
