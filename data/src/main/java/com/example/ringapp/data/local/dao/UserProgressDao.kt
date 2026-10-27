// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\UserProgressDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ringapp.data.local.entities.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the user progress dao logic for this component without modifying runtime behavior.
// Represents the user progress dao structure used by the application.
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = 1 LIMIT 1")
    suspend fun getCurrent(): UserProgressEntity?

// Manages the observe current flow without changing runtime behavior.
    @Query("SELECT * FROM user_progress WHERE userId = 1 LIMIT 1")
// Manages the observe current flow without changing runtime behavior.
    fun observeCurrent(): Flow<UserProgressEntity?>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgressEntity)
}