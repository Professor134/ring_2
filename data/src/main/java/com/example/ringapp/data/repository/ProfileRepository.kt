// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\repository\ProfileRepository.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.ProfileDao
import com.example.ringapp.data.local.entities.ProfileEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

// Handles the profile repository logic for this component without modifying runtime behavior.
// Represents the profile repository structure used by the application.
class ProfileRepository @Inject constructor(private val dao: ProfileDao) {
// Manages the observe current flow without changing runtime behavior.
    fun observeCurrent(): Flow<ProfileEntity?> = dao.observeCurrent()
    suspend fun update(profile: ProfileEntity) = dao.insert(profile)
}