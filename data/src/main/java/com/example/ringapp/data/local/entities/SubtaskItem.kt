package com.example.ringapp.data.local.entities

data class SubtaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false
)
