package com.example.ringapp.data.local.entities

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

    data class FixedCategory(
        val name: String,
        val color: Int,
        val icon: String
    )

    fun getColorForCategory(name: String): Int {
        return ALL_CATEGORIES.find { it.name.equals(name, ignoreCase = true) }?.color ?: 0xFF9E9E9E.toInt()
    }

    fun getIconForCategory(name: String): String {
        return ALL_CATEGORIES.find { it.name.equals(name, ignoreCase = true) }?.icon ?: "flag"
    }
}
