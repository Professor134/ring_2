package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streak_milestone",
    foreignKeys = [
        ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = StreakCycleEntity::class, parentColumns = ["id"], childColumns = ["cycleId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("habitId"),
        Index("cycleId"),
        Index(value = ["habitId", "cycleId", "milestoneLength"], unique = true)
    ]
)
data class StreakMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val cycleId: Long,
    val milestoneLength: Int,
    val achievedAt: Long,
    val pointsAwarded: Int,
    val createdAt: Long,
    val updatedAt: Long
)
