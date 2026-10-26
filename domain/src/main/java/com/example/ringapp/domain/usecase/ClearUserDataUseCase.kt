package com.example.ringapp.domain.usecase

import com.example.ringapp.data.repository.UserDataRepository
import javax.inject.Inject

class ClearUserDataUseCase @Inject constructor(private val repository: UserDataRepository) {
    suspend operator fun invoke() = repository.clearUserData()
}
