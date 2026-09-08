package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_achievements",
    foreignKeys = [ForeignKey(entity = AchievementEntity::class, parentColumns = ["id"], childColumns = ["achievementId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["achievementId"], unique = true)]
)
data class UserAchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val achievementId: Long,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val rewardTransactionId: Long? = null
)
