package com.example.ring_2.logic

import java.util.Calendar

object DateTimeUtils {
    fun getMidnightTimestamp(time: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
