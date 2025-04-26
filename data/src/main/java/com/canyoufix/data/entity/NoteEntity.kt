package com.canyoufix.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String
)