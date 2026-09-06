package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "point_transaction",
    foreignKeys = [
        ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = StreakCycleEntity::class, parentColumns = ["id"], childColumns = ["cycleId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("habitId"), Index("taskId"), Index("cycleId"), Index(value = ["uniqueReference"], unique = true)]
)
data class PointTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val habitId: Long? = null,
    val taskId: Long? = null,
    val cycleId: Long? = null,
    val uniqueReference: String,
    val timestamp: Long,
    val deviceId: String? = null,
    val createdAt: Long
)
