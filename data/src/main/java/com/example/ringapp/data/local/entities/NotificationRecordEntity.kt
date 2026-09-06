package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notification", indices = [Index("timestamp")])
data class NotificationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedEntityId: Long? = null,
    val timestamp: Long,
    val createdAt: Long
)
