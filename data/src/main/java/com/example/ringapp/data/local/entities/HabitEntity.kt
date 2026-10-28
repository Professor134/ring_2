// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\HabitEntity.kt
// Purpose: Entity representing a habit with symmetric/asymmetric target configurations.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
    val sets: Int = 1,
    val repsPerSet: Int = 1,
    val symmetric: Boolean = true,
    val targetSequence: String? = null,
    val weeklyTargets: String? = null,
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
        const val BMI_HABIT_ID = 2L
        const val BMI_HABIT_NAME = "BMI"
        const val PLATINUM_COLOR = 0xFFE5E4E2.toInt()
    }

    fun isStepsHabit() = id == STEPS_HABIT_ID || name == STEPS_HABIT_NAME

    fun isBmiHabit() = id == BMI_HABIT_ID || name.equals(BMI_HABIT_NAME, ignoreCase = true)

    fun getTargetForDate(date: LocalDate): Double {
        if (!symmetric && !weeklyTargets.isNullOrBlank()) {
            try {
                val json = JSONObject(weeklyTargets)
                val dayKey = date.dayOfWeek.value.toString()
                if (json.has(dayKey)) {
                    val obj = json.getJSONObject(dayKey)
                    return obj.optDouble("target", target)
                }
            } catch (e: Exception) {
                // Fallback to default target
            }
        }
        return target
    }

    fun getTargetForTimestamp(millis: Long): Double {
        val date = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return getTargetForDate(date)
    }
}
