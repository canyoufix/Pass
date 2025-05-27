package com.canyoufix.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.canyoufix.data.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity)

    @Query("""
        UPDATE cards SET 
            title = :title, 
            number = :number,
            expiry_date = :expiryDate,
            cvc = :cvc,
            holder_name = :holderName,
            last_modified = :timestamp 
        WHERE id = :id
    """)
    suspend fun update(
        id: String,
        title: String,
        number: String,
        expiryDate: String,
        cvc: String,
        holderName: String,
        timestamp: Long
    )

    @Query("UPDATE cards SET is_deleted = 1, last_modified = :timestamp WHERE id = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("SELECT * FROM cards WHERE is_deleted = 0 ORDER BY last_modified DESC")
    fun getAll(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun getById(id: String): Flow<CardEntity?>

    @Query("DELETE FROM cards")
    suspend fun clearAll()
}

