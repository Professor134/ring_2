// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\CategoryConstants.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

// Handles the category constants logic for this component without modifying runtime behavior.
// Represents the category constants structure used by the application.
object CategoryConstants {
    const val GYM = "Gym"
    const val HEALTH = "Health"
    const val STUDY = "Study"
    const val PERSONAL = "Personal"
    const val WORK = "Work"

    val ALL_CATEGORIES = listOf(
        FixedCategory(GYM, 0xFFF44336.toInt(), "fitness"),
        FixedCategory(HEALTH, 0xFF4CAF50.toInt(), "self"),
        FixedCategory(STUDY, 0xFF2196F3.toInt(), "school"),
        FixedCategory(PERSONAL, 0xFFFFEB3B.toInt(), "person"),
        FixedCategory(WORK, 0xFF9C27B0.toInt(), "work")
    )

// Represents the fixed category structure used by the application.
    data class FixedCategory(
        val name: String,
        val color: Int,
        val icon: String
    )

// Manages the get color for category flow without changing runtime behavior.
    fun getColorForCategory(name: String): Int {
        return ALL_CATEGORIES.find { it.name.equals(name, ignoreCase = true) }?.color ?: 0xFF9E9E9E.toInt()
    }

// Manages the get icon for category flow without changing runtime behavior.
    fun getIconForCategory(name: String): String {
        return ALL_CATEGORIES.find { it.name.equals(name, ignoreCase = true) }?.icon ?: "flag"
    }
}
