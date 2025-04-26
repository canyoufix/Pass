package com.canyoufix.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "card_number") val cardNumber: String,
    @ColumnInfo(name = "expiry_date") val expiryDate: String,
    @ColumnInfo(name = "cvc") val cvc: String,
    @ColumnInfo(name = "card_holder") val cardHolder: String
)