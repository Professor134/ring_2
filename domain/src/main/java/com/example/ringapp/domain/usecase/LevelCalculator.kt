package com.example.ringapp.domain.usecase

object LevelCalculator {
    fun threshold(level: Int): Long {
        var total = 0.0
        var currentNeeded = 100.0
        for (i in 1 until level) {
            total += currentNeeded
            currentNeeded *= 1.25
        }
        return total.toLong()
    }

    fun levelForLifetimePoints(points: Long): Int {
        var level = 1
        while (threshold(level + 1) <= points && level < 1000) level++
        return level
    }
}
