package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1, // Only one profile
    val name: String = "",
    val dob: Long? = null, // timestamp
    val gender: String = "",
    val avatarColor: Int = 0xFF00E676.toInt(), // default green
    val profilePictureUri: String? = null,
    val elitePoints: Int = 500,
    val level: Int = 1,
    val accumulatedPoints: Int = 0 // total points earned over time for leveling
)
