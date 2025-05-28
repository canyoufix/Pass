package com.canyoufix.data

import android.util.Log
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.data.entity.QueueSyncEntity
import com.canyoufix.data.repository.CardRepository
import com.canyoufix.data.repository.NoteRepository
import com.canyoufix.data.repository.PasswordRepository
import com.canyoufix.data.repository.QueueSyncRepository
import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.dto.CardDto
import com.canyoufix.sync.dto.NoteDto
import com.canyoufix.sync.dto.PasswordDto
import com.canyoufix.sync.retrofit.RetrofitClient
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class SyncManager(
    private val passwordRepository: PasswordRepository,
    private val cardRepository: CardRepository,
    private val noteRepository: NoteRepository,
    private val queueSyncRepository: QueueSyncRepository,
    private val syncSettingsStore: SyncSettingsStore,
    private val retrofitClientProvider: RetrofitClientProvider
) {

    suspend fun isServerAvailable(): Boolean {
        return try {
            val response = retrofitClientProvider.getClient().pingApi.ping()
            Log.d("SyncManager", "Ping status code: ${response.code()}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SyncManager", "Ping failed", e)
            false
        }
    }

    suspend fun startSync() {
        if (!isServerAvailable()) {
            Log.d("SyncManager", "Server is not available, skipping sync")
            return
        }

        val retrofit = retrofitClientProvider.getClient()

        // Получаем время синхронизации ДО начала операций
        val lastSyncTime = syncSettingsStore.getLastSyncTime()
        Log.d("SyncManager", "Starting sync with lastSyncTime: $lastSyncTime")

        var sendSuccess = false
        var pullSuccess = false

        // 1. Отправка локальных изменений
        try {
            val items = queueSyncRepository.getAll().first()
            for (item in items) {
                try {
                    when (item.type) {
                        "note" -> syncNote(item, retrofit)
                        "card" -> syncCard(item, retrofit)
                        "password" -> syncPassword(item, retrofit)
                    }
                    queueSyncRepository.delete(item)
                } catch (e: Exception) {
                    Log.e("SyncManager", "Ошибка при отправке item: ${item.id}", e)
                    // оставляем в очереди
                }
            }
            sendSuccess = true
        } catch (e: Exception) {
            Log.e("SyncManager", "Ошибка при отправке очереди", e)
        }

        // 2. Получение новых данных
        try {
            Log.d("SyncManager", "Pulling changes since: $lastSyncTime")

            val newPasswords = retrofit.passwordApi.getPasswordsSince(lastSyncTime)
            val newNotes = retrofit.noteApi.getNotesSince(lastSyncTime)
            val newCards = retrofit.cardApi.getCardsSince(lastSyncTime)

            for (dto in newPasswords) {
                passwordRepository.insertFromServer(dto)
            }
            for (dto in newNotes) {
                noteRepository.insertFromServer(dto)
            }
            for (dto in newCards) {
                cardRepository.insertFromServer(dto)
            }

            pullSuccess = true
        } catch (e: Exception) {
            Log.e("SyncManager", "Ошибка при получении данных с сервера", e)
        }

        // 3. Сохраняем время только если всё успешно
        val newSyncTime = System.currentTimeMillis()
        if (sendSuccess && pullSuccess) {
            syncSettingsStore.saveLastSyncTime(newSyncTime)
            Log.d("SyncManager", "Синхронизация успешно завершена, новое время: $newSyncTime")
        } else {
            Log.d("SyncManager", "Синхронизация НЕ завершена, время не обновлено")
        }
    }




    private suspend fun syncNote(item: QueueSyncEntity, retrofit: RetrofitClient) {
        val note = Json.decodeFromString<NoteEntity>(item.payload)
        val dto = NoteDto(
            id = note.id,
            title = note.title,
            content = note.content,
            lastModified = note.lastModified,
            isDeleted = note.isDeleted
        )

        when (item.action) {
            "insert" -> retrofit.noteApi.uploadNote(dto)
            "update" -> retrofit.noteApi.updateNote(note.id, dto)
            "delete" -> retrofit.noteApi.deleteNote(note.id, dto)
        }
    }

    private suspend fun syncCard(item: QueueSyncEntity, retrofit: RetrofitClient) {
        val card = Json.decodeFromString<CardEntity>(item.payload)
        val dto = CardDto(
            id = card.id,
            title = card.title,
            number = card.number,
            expiryDate = card.expiryDate,
            cvc = card.cvc,
            holderName = card.holderName,
            lastModified = card.lastModified,
            isDeleted = card.isDeleted
        )

        when (item.action) {
            "insert" -> retrofit.cardApi.uploadCard(dto)
            "update" -> retrofit.cardApi.updateCard(card.id, dto)
            "delete" -> retrofit.cardApi.deleteCard(card.id, dto)
        }
    }

    private suspend fun syncPassword(item: QueueSyncEntity, retrofit: RetrofitClient) {
        val password = Json.decodeFromString<PasswordEntity>(item.payload)
        val dto = PasswordDto(
            id = password.id,
            title = password.title,
            url = password.url,
            username = password.username,
            password = password.password,
            lastModified = password.lastModified,
            isDeleted = password.isDeleted
        )

        when (item.action) {
            "insert" -> retrofit.passwordApi.uploadPassword(dto)
            "update" -> retrofit.passwordApi.updatePassword(password.id, dto)
            "delete" -> retrofit.passwordApi.deletePassword(password.id, dto)
        }
    }
}