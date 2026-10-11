// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\main\java\com\example\ringapp\domain\usecase\ObserveTaskTransactionsUseCase.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.dao.PointTransactionDao
import com.example.ringapp.data.local.entities.PointTransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Handles the observe task transactions use case logic for this component without modifying runtime behavior.
// Represents the observe task transactions use case structure used by the application.
class ObserveTaskTransactionsUseCase @Inject constructor(
    private val pointTransactionDao: PointTransactionDao
) {
    operator fun invoke(taskId: Long): Flow<List<PointTransactionEntity>> = pointTransactionDao.observeForTask(taskId)
}
