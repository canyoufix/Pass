package com.canyoufix.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.canyoufix.data.dao.CardDao
import com.canyoufix.data.dao.NoteDao
import com.canyoufix.data.dao.PasswordDao
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity

@Database(
    entities = [CardEntity::class, NoteEntity::class, PasswordEntity::class],
    version = 1,
    exportSchema = false
)
    abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun noteDao(): NoteDao
    abstract fun passwordDao(): PasswordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "secure_data_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}
