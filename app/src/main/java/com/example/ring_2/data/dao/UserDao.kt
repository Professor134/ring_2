package com.example.ring_2.data.dao

import androidx.room.*
import com.example.ring_2.data.model.UserProgressEntity
import com.example.ring_2.data.model.ProfileEntity
import com.example.ring_2.data.model.PointTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateUserProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM profile WHERE id = 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("SELECT * FROM point_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<PointTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PointTransactionEntity)
    
    @Query("SELECT * FROM point_transactions WHERE uniqueReference = :reference")
    suspend fun getTransactionByReference(reference: String): PointTransactionEntity?

    @Query("DELETE FROM point_transactions")
    suspend fun clearAllTransactions()
}
