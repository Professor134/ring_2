// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\AnalyticsDailyDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringapp.data.local.entities.AnalyticsDailyEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the analytics daily dao logic for this component without modifying runtime behavior.
// Represents the analytics daily dao structure used by the application.
interface AnalyticsDailyDao {
// Manages the observe range flow without changing runtime behavior.
    @Query("SELECT * FROM analytics_daily WHERE date BETWEEN :from AND :to ORDER BY date")
// Manages the observe range flow without changing runtime behavior.
    fun observeRange(from: Long, to: Long): Flow<List<AnalyticsDailyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: AnalyticsDailyEntity)

    @Query("DELETE FROM analytics_daily")
    suspend fun clear()
}
