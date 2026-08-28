package com.example.ring_2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val dateOfBirth: Long? = null,
    val gender: String = "",
    val photoUri: String? = null,
    val avatarColor: Int = 0xFF00E676.toInt()
)
