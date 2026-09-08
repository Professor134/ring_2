package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.entities.ProfileEntity
import com.example.ringapp.data.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(profile: ProfileEntity) = repository.update(profile.copy(updatedAt = System.currentTimeMillis()))
}