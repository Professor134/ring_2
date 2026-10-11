// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\SubtaskItem.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

// Handles the subtask item logic for this component without modifying runtime behavior.
// Represents the subtask item structure used by the application.
data class SubtaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false
)
