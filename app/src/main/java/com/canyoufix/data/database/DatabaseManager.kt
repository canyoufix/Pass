package com.canyoufix.data.database

import androidx.room.withTransaction
import com.canyoufix.domain.CoroutineDispatchers
import kotlinx.coroutines.withContext

class DatabaseManager(
    private val database: AppDatabase,
    private val dispatchers: CoroutineDispatchers,
) {
    suspend fun clearAllData() =
        withContext(dispatchers.io) {
            database.withTransaction {
                database.passwordDao().clearAll()
                database.cardDao().clearAll()
                database.noteDao().clearAll()
                database.queueSyncDao().clearAll()
            }
        }
}
