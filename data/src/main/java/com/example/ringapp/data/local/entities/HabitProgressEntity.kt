package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_progress",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: Long,
    val target: Int,
    val actual: Int,
    val percentage: Int,
    val completed: Boolean,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
