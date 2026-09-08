package com.example.ringapp.domain.usecase

import kotlin.math.pow

object LevelCalculator {
    fun threshold(level: Int): Long = (1000.0 * 1.5.pow((level - 1).coerceAtLeast(0))).toLong()

    fun levelForLifetimePoints(points: Long): Int {
        var level = 1
        while (threshold(level + 1) <= points && level < 1000) level++
        return level
    }
}