package com.example.ringapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_metadata")
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
