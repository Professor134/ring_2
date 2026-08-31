package com.example.ring_2.data.dao

import androidx.room.*
import com.example.ring_2.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getRecentNotifications(sinceTimestamp: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE timestamp < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}
