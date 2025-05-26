package com.canyoufix.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.canyoufix.data.entity.QueueSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(queueItem: QueueSyncEntity)

    @Query("SELECT * FROM queue_sync")
    fun getAll(): Flow<List<QueueSyncEntity>>

    @Query("SELECT * FROM queue_sync WHERE id = :id")
    suspend fun getById(id: String): QueueSyncEntity?

    @Delete
    suspend fun delete(queueItem: QueueSyncEntity)

    @Query("DELETE FROM queue_sync WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM queue_sync")
    suspend fun clearAll()
}