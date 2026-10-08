package com.example.ringapp.domain.usecase

import com.example.ringapp.data.local.dao.PointTransactionDao
import com.example.ringapp.data.local.entities.PointTransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTaskTransactionsUseCase @Inject constructor(
    private val pointTransactionDao: PointTransactionDao
) {
    operator fun invoke(taskId: Long): Flow<List<PointTransactionEntity>> = pointTransactionDao.observeForTask(taskId)
}
