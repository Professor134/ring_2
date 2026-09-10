package com.example.ringapp.data.repository

import com.example.ringapp.data.local.dao.CategoryDao
import com.example.ringapp.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepository @Inject constructor(private val dao: CategoryDao) {
    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()
    suspend fun create(name: String, color: Int, icon: String? = null): Long {
        val now = System.currentTimeMillis()
        return dao.insert(CategoryEntity(name = name, color = color, icon = icon, createdAt = now, updatedAt = now))
    }
}
