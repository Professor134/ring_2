package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.ProfileDao
import com.example.ringapp.data.local.entities.ProfileEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ProfileRepository @Inject constructor(private val dao: ProfileDao) {
    fun observeCurrent(): Flow<ProfileEntity?> = dao.observeCurrent()
    suspend fun update(profile: ProfileEntity) = dao.insert(profile)
}