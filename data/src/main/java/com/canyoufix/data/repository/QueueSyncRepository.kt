package com.canyoufix.data.repository

import com.canyoufix.data.dao.QueueSyncDao
import com.canyoufix.data.entity.QueueSyncEntity
import kotlinx.coroutines.flow.Flow


class QueueSyncRepository(private val dao: QueueSyncDao) {

    fun getAll(): Flow<List<QueueSyncEntity>> = dao.getAll()

    suspend fun insert(entity: QueueSyncEntity) {
        dao.insert(entity)
    }

    suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    suspend fun delete(entity: QueueSyncEntity) {
        dao.delete(entity)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}