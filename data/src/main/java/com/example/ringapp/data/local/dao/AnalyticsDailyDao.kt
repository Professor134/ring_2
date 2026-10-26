package com.example.ringapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringapp.data.local.entities.AnalyticsDailyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDailyDao {
    @Query("SELECT * FROM analytics_daily WHERE date BETWEEN :from AND :to ORDER BY date")
    fun observeRange(from: Long, to: Long): Flow<List<AnalyticsDailyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: AnalyticsDailyEntity)

    @Query("DELETE FROM analytics_daily")
    suspend fun clear()
}
