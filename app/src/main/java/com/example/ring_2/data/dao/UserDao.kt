package com.example.ring_2.data.dao

import androidx.room.*
import com.example.ring_2.data.model.UserProfile
import com.example.ring_2.data.model.PointTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("SELECT * FROM point_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<PointTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PointTransaction)
}
