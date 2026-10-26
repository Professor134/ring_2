package com.example.ringapp.data.repository

import androidx.room.withTransaction
import com.example.ringapp.data.db.AppDatabase
import javax.inject.Inject

class UserDataRepository @Inject constructor(private val database: AppDatabase) {
    suspend fun clearUserData() {
        database.withTransaction {
            database.clearAllTables()
        }
    }
}
