package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_records")
data class HabitRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: Long, // timestamp (midnight of the date)
    val value: Double, // current progress
    val isCompleted: Boolean,
    val note: String = ""
)
