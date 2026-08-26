package com.example.ring_2.data

import androidx.room.TypeConverter
import com.example.ring_2.data.model.HabitSchedule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromHabitSchedule(value: HabitSchedule): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toHabitSchedule(value: String): HabitSchedule {
        return Json.decodeFromString(value)
    }
}
