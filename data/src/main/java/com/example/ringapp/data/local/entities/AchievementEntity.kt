package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: AchievementCategory,
    val threshold: Int,
    val description: String,
    val icon: String? = null,
    val pointsAwarded: Int,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val currentProgress: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)
