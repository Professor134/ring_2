package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class HabitType {
    YES_NO, MEASURABLE
}

@Serializable
sealed class HabitSchedule {
    @Serializable data object Daily : HabitSchedule()
    @Serializable data object OddDays : HabitSchedule()
    @Serializable data object EvenDays : HabitSchedule()
    @Serializable data class Weekly(val daysOfWeek: Set<Int>) : HabitSchedule() // 1=Sun, 7=Sat
    @Serializable data class Monthly(val dayOfMonth: Int) : HabitSchedule()
    @Serializable data class Yearly(val month: Int, val dayOfMonth: Int) : HabitSchedule()
    @Serializable data class Custom(val intervalDays: Int) : HabitSchedule()
}

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val icon: String,
    val categoryId: Long,
    val type: HabitType,
    val targetValue: Double = 1.0,
    val unit: String = "",
    val schedule: HabitSchedule,
    val startDate: Long, // timestamp
    val isActive: Boolean = true,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val color: Int
)
