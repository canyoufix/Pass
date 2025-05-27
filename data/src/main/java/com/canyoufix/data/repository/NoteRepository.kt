package com.canyoufix.data.repository

import com.canyoufix.data.dao.NoteDao
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionAESKeyHolder
import com.canyoufix.data.entity.QueueSyncEntity
import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.dto.NoteDto
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.crypto.SecretKey


class NoteRepository(
    private val noteDao: NoteDao,
    private val retrofitClientProvider: RetrofitClientProvider,
    private val syncSettingsStore: SyncSettingsStore,
    private val queueSyncRepository: QueueSyncRepository
) {
    private val cryptoManager = CryptoManager

    val getAllNotes: Flow<List<NoteEntity>> = noteDao.getAll()
        .map { notes -> decryptNotes(notes) }

    val getAllEncryptedNotes: Flow<List<NoteEntity>> = noteDao.getAll()

    suspend fun insert(note: NoteEntity) {
        val encryptedNote = encryptNote(note)
        noteDao.insert(encryptedNote)

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "note",
                action = "insert",
                payload = Json.encodeToString(encryptedNote)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = noteToDto(encryptedNote)
                retrofit.noteApi.uploadNote(dto)

                // Удаляем из очереди, если успешно отправлено
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }

    suspend fun update(note: NoteEntity) {
        val encryptedNote = encryptNote(note)

        val timestamp = System.currentTimeMillis()
        noteDao.update(
            id = encryptedNote.id,
            title = encryptedNote.title,
            content = encryptedNote.content,
            timestamp = timestamp
        )

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "note",
                action = "update",
                payload = Json.encodeToString(encryptedNote)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = noteToDto(encryptedNote)
                retrofit.noteApi.updateNote(encryptedNote.id, dto)

                // Удаляем из очереди, если успешно отправлено
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }

    suspend fun delete(note: NoteEntity) {
        val encryptedNote = encryptNote(note)

        val timestamp = System.currentTimeMillis()
        noteDao.delete(
            id = encryptedNote.id,
            timestamp = timestamp
        )

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "note",
                action = "delete",
                payload = Json.encodeToString(encryptedNote)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                retrofit.noteApi.deleteNote(encryptedNote.id)

                // Удаляем из очереди, если успешно отправлено
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }


    fun getById(id: String): Flow<NoteEntity?> {
        return noteDao.getById(id)
            .map { it?.let { decryptNote(it) } }
    }

    private fun noteToDto(note: NoteEntity): NoteDto {
        return NoteDto(
            id = note.id,
            title = note.title,
            content = note.content
        )
    }

    private fun encryptNote(note: NoteEntity): NoteEntity {
        val key = SessionAESKeyHolder.key
        return encryptNote(note, key)
    }

    private fun encryptNote(note: NoteEntity, key: SecretKey): NoteEntity {
        return note.copy(
            title = cryptoManager.encrypt(note.title, key),
            content = cryptoManager.encrypt(note.content, key)
        )
    }

    private fun decryptNote(note: NoteEntity): NoteEntity {
        val key = SessionAESKeyHolder.key
        return decryptNote(note, key)
    }

    fun decryptNote(note: NoteEntity, key: SecretKey): NoteEntity {
        return note.copy(
            title = cryptoManager.decrypt(note.title, key) ?: "DECRYPTION_ERROR",
            content = cryptoManager.decrypt(note.content, key) ?: ""
        )
    }

    private fun decryptNotes(notes: List<NoteEntity>): List<NoteEntity> {
        val key = SessionAESKeyHolder.key
        return decryptNotes(notes, key)
    }

    private fun decryptNotes(notes: List<NoteEntity>, key: SecretKey): List<NoteEntity> {
        return notes.map { decryptNote(it, key) }
    }
}
