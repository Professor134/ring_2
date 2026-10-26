package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.UserProgressDao
import com.example.ringapp.data.local.entities.UserProgressEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class UserProgressRepository @Inject constructor(private val dao: UserProgressDao) {
    fun observeCurrent(): Flow<UserProgressEntity?> = dao.observeCurrent()
}