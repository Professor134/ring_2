package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streak_milestones",
    indices = [Index(value = ["streakCycleId", "milestone"], unique = true)]
)
data class StreakMilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val streakCycleId: Long,
    val milestone: Int, // 10, 20, 40 etc.
    val reward: Int,
    val awardedAt: Long = System.currentTimeMillis()
)
