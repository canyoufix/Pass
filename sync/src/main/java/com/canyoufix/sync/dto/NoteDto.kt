package com.canyoufix.sync.dto

data class NoteDto(
    val id: String,

    val title: String,
    val content: String,

    val lastModified: Long,
    val isDeleted: Boolean
)