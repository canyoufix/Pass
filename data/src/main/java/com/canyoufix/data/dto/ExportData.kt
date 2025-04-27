package com.canyoufix.data.dto

import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable


@Serializable
data class ExportData(
    val encrypted: String?,
    val salt: String,
    val passwords: List<PasswordEntity>,
    val cards: List<CardEntity>,
    val notes: List<NoteEntity>,
    val meta: MetaInfo
)