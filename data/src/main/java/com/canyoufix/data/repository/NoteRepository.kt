package com.canyoufix.data.repository

import com.canyoufix.data.dao.NoteDao
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionKeyHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(private val noteDao: NoteDao) {
    private val cryptoManager = CryptoManager

    // Все заметки автоматически расшифровываются при получении
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAll()
        .map { notes -> decryptNotes(notes) }

    suspend fun insert(note: NoteEntity) {
        val encryptedNote = encryptNote(note)
        noteDao.insert(encryptedNote)
    }

    suspend fun update(note: NoteEntity) {
        val encryptedNote = encryptNote(note)
        noteDao.update(encryptedNote)
    }

    suspend fun delete(note: NoteEntity) {
        val encryptedNote = encryptNote(note)
        noteDao.delete(encryptedNote)
    }

    fun getById(id: String): Flow<NoteEntity?> {
        return noteDao.getById(id)
            .map { it?.let { decryptNote(it) } }
    }

    private fun encryptNote(note: NoteEntity): NoteEntity {
        val key = SessionKeyHolder.key ?: throw SecurityException("No active session key")

        return note.copy(
            title = cryptoManager.encrypt(note.title, key),
            content = cryptoManager.encrypt(note.content, key)
        )
    }

    private fun decryptNote(note: NoteEntity): NoteEntity {
        val key = SessionKeyHolder.key ?: throw SecurityException("No active session key")

        return note.copy(
            title = cryptoManager.decrypt(note.title, key) ?: "DECRYPTION_ERROR",
            content = cryptoManager.decrypt(note.content, key) ?: ""
        )
    }

    private fun decryptNotes(notes: List<NoteEntity>): List<NoteEntity> {
        return notes.map { decryptNote(it) }
    }

}