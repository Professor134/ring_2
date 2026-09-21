package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("categoryId")]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val categoryId: Long?,
    val type: HabitType,
    val target: Double,
    val unit: String? = null,
    val scheduleType: ScheduleType,
    val scheduleDays: List<Int>? = null,
    val startDate: Long,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val color: Int,
    val deletedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        const val STEPS_HABIT_ID = 1L
        const val STEPS_HABIT_NAME = "Steps"
        const val PLATINUM_COLOR = 0xFFE5E4E2.toInt()
    }
    
    fun isStepsHabit() = id == STEPS_HABIT_ID || name == STEPS_HABIT_NAME
}
