package com.canyoufix.domain.model

import java.util.UUID

sealed interface VaultEntry {
    val id: String
    val title: String
    val lastModified: Long
}

data class PasswordEntry(
    override val id: String = UUID.randomUUID().toString(),
    override val title: String,
    val url: String,
    val username: String,
    val password: String,
    override val lastModified: Long = System.currentTimeMillis(),
) : VaultEntry {
    override fun toString(): String = "PasswordEntry(id=$id, redacted)"
}

data class CardEntry(
    override val id: String = UUID.randomUUID().toString(),
    override val title: String,
    val number: String,
    val expiryDate: String,
    val cvc: String,
    val holderName: String,
    override val lastModified: Long = System.currentTimeMillis(),
) : VaultEntry {
    override fun toString(): String = "CardEntry(id=$id, redacted)"
}

data class NoteEntry(
    override val id: String = UUID.randomUUID().toString(),
    override val title: String,
    val content: String,
    override val lastModified: Long = System.currentTimeMillis(),
) : VaultEntry {
    override fun toString(): String = "NoteEntry(id=$id, redacted)"
}

enum class EntryKind {
    PASSWORD,
    CARD,
    NOTE,
}
