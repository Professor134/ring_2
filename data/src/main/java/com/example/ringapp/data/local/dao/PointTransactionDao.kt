package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringapp.data.local.entities.PointTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointTransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: PointTransactionEntity): Long

    @Query("SELECT * FROM point_transaction WHERE uniqueReference = :reference LIMIT 1")
    suspend fun findByReference(reference: String): PointTransactionEntity?

    @Query("SELECT * FROM point_transaction WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun observeForHabit(habitId: Long): Flow<List<PointTransactionEntity>>

    @Query("SELECT * FROM point_transaction ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<PointTransactionEntity>>
}