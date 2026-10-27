// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\dao\PointTransactionDao.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringapp.data.local.entities.PointTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
// Handles the point transaction dao logic for this component without modifying runtime behavior.
// Represents the point transaction dao structure used by the application.
interface PointTransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: PointTransactionEntity): Long

    @Query("SELECT * FROM point_transaction WHERE uniqueReference = :reference LIMIT 1")
    suspend fun findByReference(reference: String): PointTransactionEntity?

// Manages the observe for habit flow without changing runtime behavior.
    @Query("SELECT * FROM point_transaction WHERE habitId = :habitId ORDER BY timestamp DESC")
// Manages the observe for habit flow without changing runtime behavior.
    fun observeForHabit(habitId: Long): Flow<List<PointTransactionEntity>>

// Manages the observe for task flow without changing runtime behavior.
    @Query("SELECT * FROM point_transaction WHERE taskId = :taskId ORDER BY timestamp DESC")
// Manages the observe for task flow without changing runtime behavior.
    fun observeForTask(taskId: Long): Flow<List<PointTransactionEntity>>

// Manages the observe all flow without changing runtime behavior.
    @Query("SELECT * FROM point_transaction ORDER BY timestamp DESC")
// Manages the observe all flow without changing runtime behavior.
    fun observeAll(): Flow<List<PointTransactionEntity>>
}
