package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streak_cycle",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("habitId")]
)
data class StreakCycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val cycleNumber: Int,
    val startTime: Long,
    val endTime: Long? = null,
    val currentLength: Int,
    val bestLength: Int,
    val breakReason: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
