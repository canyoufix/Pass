package com.canyoufix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "queue_sync")
data class QueueSyncEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") val id: String, // ID записи, которую нужно синхронизировать

    @ColumnInfo(name = "type") val type: String,       // Тип сущности: "card", "password", "note"
    @ColumnInfo(name = "action") val action: String,   // Тип действия: "insert", "update", "delete"
    @ColumnInfo(name = "payload") val payload: String, // JSON-представление объекта
    @ColumnInfo(name = "timestamp") val timestamp: Long // Время добавления в очередь
)
