// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\AppSettingsDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringapp.data.local.entities.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the app settings dao logic for this component without modifying runtime behavior.
// Represents the app settings dao structure used by the application.
interface AppSettingsDao {
// Manages the observe flow without changing runtime behavior.
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
// Manages the observe flow without changing runtime behavior.
    fun observe(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)
}
