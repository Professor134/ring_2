package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: Int = 1,
    val currentPoints: Int,
    val lifetimePoints: Int,
    val level: Int,
    val createdAt: Long,
    val updatedAt: Long
)
