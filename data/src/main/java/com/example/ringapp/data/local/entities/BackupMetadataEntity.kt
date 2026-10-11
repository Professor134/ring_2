// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\BackupMetadataEntity.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_metadata")
// Handles the backup metadata entity logic for this component without modifying runtime behavior.
// Represents the backup metadata entity structure used by the application.
data class BackupMetadataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: BackupType,
    val status: BackupStatus,
    val filePath: String? = null,
    val sizeBytes: Long? = null,
    val errorMessage: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
