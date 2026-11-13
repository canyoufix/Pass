package com.canyoufix.domain.repository

import com.canyoufix.domain.model.*
import kotlinx.coroutines.flow.Flow

interface EntryRepository<T : VaultEntry> {
    fun observeAll(): Flow<List<T>>

    fun observeById(id: String): Flow<T?>

    suspend fun save(entry: T)

    suspend fun delete(entry: T)
}

interface PasswordRepository : EntryRepository<PasswordEntry> {
    suspend fun findByDomain(domain: String): List<PasswordEntry>
}

interface CardRepository : EntryRepository<CardEntry>

interface NoteRepository : EntryRepository<NoteEntry>
