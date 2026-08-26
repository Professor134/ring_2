package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    STARTING_POINTS,
    CREATE_HABIT,
    COMPLETE_HABIT,
    STREAK_10_DAYS,
    STREAK_20_DAYS,
    MISSED_TARGET,
    CHANGE_TARGET,
    CREATE_TASK,
    COMPLETE_TASK
}

@Entity(tableName = "point_transactions")
data class PointTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val relatedId: Long? = null, // Habit or Task ID
    val reference: String // Unique reference (e.g. UUID)
)
