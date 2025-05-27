package com.canyoufix.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.canyoufix.data.entity.PasswordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(password: PasswordEntity)

    @Query("""
        UPDATE passwords SET 
            title = :title, 
            url = :url, 
            username = :username, 
            password = :password, 
            last_modified = :timestamp 
        WHERE id = :id
    """)
    suspend fun update(
        id: String,
        title: String,
        url: String,
        username: String,
        password: String,
        timestamp: Long
    )

    @Query("UPDATE passwords SET is_deleted = 1, last_modified = :timestamp WHERE id = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("SELECT * FROM passwords WHERE is_deleted = 0 ORDER BY last_modified DESC")
    fun getAll(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun getById(id: String): Flow<PasswordEntity?>

    @Query("DELETE FROM passwords")
    suspend fun clearAll()
}


