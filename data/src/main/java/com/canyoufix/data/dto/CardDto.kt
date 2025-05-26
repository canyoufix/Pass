package com.canyoufix.data.dto

data class CardDto(
    val id: String,
    val title: String,
    val number: String,
    val expiryDate: String,
    val cvc: String,
    val holderName: String
)