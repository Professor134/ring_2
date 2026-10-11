// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\LevelCalculator.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

// Handles the level calculator logic for this component without modifying runtime behavior.
// Represents the level calculator structure used by the application.
object LevelCalculator {
// Manages the threshold flow without changing runtime behavior.
    fun threshold(level: Int): Long {
        var total = 0.0
        var currentNeeded = 100.0
        for (i in 1 until level) {
            total += currentNeeded
            currentNeeded *= 1.25
        }
        return total.toLong()
    }

// Manages the level for lifetime points flow without changing runtime behavior.
    fun levelForLifetimePoints(points: Long): Int {
        var level = 1
        while (threshold(level + 1) <= points && level < 1000) level++
        return level
    }
}
