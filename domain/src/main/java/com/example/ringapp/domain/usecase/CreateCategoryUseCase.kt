package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {
    suspend operator fun invoke(name: String, color: Int): Long = repository.create(name.trim(), color)
}