package com.canyoufix.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.canyoufix.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Query("""
        UPDATE notes SET 
            title = :title, 
            content = :content, 
            last_modified = :timestamp 
        WHERE id = :id
    """)
    suspend fun update(id: String, title: String, content: String, timestamp: Long)


    @Query("UPDATE notes SET is_deleted = 1, last_modified = :timestamp WHERE id = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("SELECT * FROM notes WHERE is_deleted = 0 ORDER BY last_modified DESC")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun getById(id: String): Flow<NoteEntity?>

    @Query("DELETE FROM notes")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)
}
