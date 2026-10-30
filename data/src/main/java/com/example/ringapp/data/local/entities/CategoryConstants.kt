// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\CategoryConstants.kt
// Purpose: Category definitions with vibrant bright color schemes.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

object CategoryConstants {
    const val GYM = "Gym"
    const val HEALTH = "Health"
    const val STUDY = "Study"
    const val PERSONAL = "Personal"
    const val WORK = "Work"

    // Highly bright, vibrant electric color palette for habit categories
    val ALL_CATEGORIES = listOf(
        FixedCategory(GYM, 0xFFFF1744.toInt(), "fitness"),       // Vibrant Bright Electric Red
        FixedCategory(HEALTH, 0xFF00E676.toInt(), "self"),       // Vibrant Bright Electric Green
        FixedCategory(STUDY, 0xFF00B0FF.toInt(), "school"),      // Vibrant Bright Electric Blue
        FixedCategory(PERSONAL, 0xFFFFEA00.toInt(), "person"),   // Vibrant Bright Electric Yellow
        FixedCategory(WORK, 0xFFD500F9.toInt(), "work")          // Vibrant Bright Electric Magenta
    )

    data class FixedCategory(
        val name: String,
        val color: Int,
        val icon: String
    )

    fun getColorForCategory(name: String): Int {
        return ALL_CATEGORIES.find { it.name.equals(name, ignoreCase = true) }?.color ?: 0xFF00E5FF.toInt()
    }

    fun getIconForCategory(name: String): String {
        return ALL_CATEGORIES.find { it.name.equals(name, ignoreCase = true) }?.icon ?: "flag"
    }
}
