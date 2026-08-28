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
    COMPLETE_TASK,
    DELETE_TASK,
    DELETE_HABIT
}

@Entity(tableName = "point_transactions")
data class PointTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val habitId: Long? = null,
    val taskId: Long? = null,
    val streakCycleId: Long? = null,
    val uniqueReference: String // e.g. habit_12_cycle_2_milestone_10
)
