package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.AchievementDao
import com.example.ringapp.data.local.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AchievementRepository @Inject constructor(private val dao: AchievementDao) {
    fun observeAll(): Flow<List<AchievementEntity>> = dao.observeAll()
    suspend fun seed(achievements: List<AchievementEntity>) = dao.insertAll(achievements)
    suspend fun update(achievement: AchievementEntity) = dao.update(achievement)
}