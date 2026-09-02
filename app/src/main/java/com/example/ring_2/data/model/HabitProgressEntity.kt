package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "habit_progress",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: Long, // midnight timestamp
    val target: Double, // target at the time of entry
    val actualValue: Double,
    val completed: Boolean,
    val percentage: Double,
    val note: String = "",
    val pointsAwarded: Int = 0,
    val penaltyApplied: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
