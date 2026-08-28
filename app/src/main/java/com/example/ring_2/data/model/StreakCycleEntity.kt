package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_cycles")
data class StreakCycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val cycleNumber: Int,
    val startedAt: Long,
    val endedAt: Long? = null,
    val currentLength: Int = 0,
    val bestLength: Int = 0,
    val isActive: Boolean = true,
    val breakReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
