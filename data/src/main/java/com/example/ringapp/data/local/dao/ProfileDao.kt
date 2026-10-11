// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\ProfileDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringapp.data.local.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the profile dao logic for this component without modifying runtime behavior.
// Represents the profile dao structure used by the application.
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE userId = 1 LIMIT 1")
    suspend fun getCurrent(): ProfileEntity?

// Manages the observe current flow without changing runtime behavior.
    @Query("SELECT * FROM profile WHERE userId = 1 LIMIT 1")
// Manages the observe current flow without changing runtime behavior.
    fun observeCurrent(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity)
}