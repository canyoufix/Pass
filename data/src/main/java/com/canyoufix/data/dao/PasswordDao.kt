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

    @Update
    suspend fun update(password: PasswordEntity)

    @Delete
    suspend fun delete(password: PasswordEntity)

    @Query("SELECT * FROM passwords ORDER BY title ASC")
    fun getAll(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<PasswordEntity?>

    @Query("DELETE FROM passwords")
    suspend fun clearAll()
}
