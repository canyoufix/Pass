package com.canyoufix.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.canyoufix.data.dao.CardDao
import com.canyoufix.data.dao.NoteDao
import com.canyoufix.data.dao.PasswordDao
import com.canyoufix.data.dao.QueueSyncDao
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.data.entity.QueueSyncEntity

@Database(
    entities = [CardEntity::class, NoteEntity::class, PasswordEntity::class, QueueSyncEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun noteDao(): NoteDao
    abstract fun passwordDao(): PasswordDao
    abstract fun queueSyncDao(): QueueSyncDao
}
