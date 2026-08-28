package com.example.ring_2.data

import androidx.room.TypeConverter
import com.example.ring_2.data.model.HabitSchedule
import com.example.ring_2.data.model.HabitType
import com.example.ring_2.data.model.TaskPriority
import com.example.ring_2.data.model.TransactionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromHabitSchedule(value: HabitSchedule): String = Json.encodeToString(value)
    @TypeConverter
    fun toHabitSchedule(value: String): HabitSchedule = Json.decodeFromString(value)

    @TypeConverter
    fun fromHabitType(value: HabitType): String = value.name
    @TypeConverter
    fun toHabitType(value: String): HabitType = HabitType.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name
    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name
    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
