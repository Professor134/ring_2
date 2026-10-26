package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.CategoryEntity
import com.example.ringapp.data.repository.CategoryRepository
import javax.inject.Inject

class ObserveCategoriesUseCase @Inject constructor(private val repository: CategoryRepository) {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<List<CategoryEntity>> = repository.observeAll()
}
