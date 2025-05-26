package com.canyoufix.data

import android.util.Log
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.data.entity.QueueSyncEntity
import com.canyoufix.data.repository.QueueSyncRepository
import com.canyoufix.sync.dto.CardDto
import com.canyoufix.sync.dto.NoteDto
import com.canyoufix.sync.dto.PasswordDto
import com.canyoufix.sync.retrofit.RetrofitClient
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class SyncManager(
    private val queueSyncRepository: QueueSyncRepository,
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

    suspend fun sync() {
        if (!isServerAvailable()) {
            Log.w("SyncManager", "Server is not available, skipping sync")
            return
        }

        val items = queueSyncRepository.getAll().first() // Берём текущие данные один раз
        for (item in items) {
            try {
                val retrofit = retrofitClientProvider.getClient()

                when (item.type) {
                    "note" -> syncNote(item, retrofit)
                    "card" -> syncCard(item, retrofit)
                    "password" -> syncPassword(item, retrofit)
                }

                queueSyncRepository.delete(item)
            } catch (e: Exception) {
                when {
                    e is IOException -> {
                        Log.w("SyncManager", "Network error, will retry later", e)
                    }

                    e is HttpException && e.code() == 400 -> {
                        // handleConflict(item, e)
                    }

                    else -> {
                        Log.e("SyncManager", "Sync error for item ${item.id}", e)
                    }
                }
            }
        }
    }



    private suspend fun syncNote(item: QueueSyncEntity, retrofit: RetrofitClient) {
        val note = Json.decodeFromString<NoteEntity>(item.payload)
        val dto = NoteDto(note.id, note.title, note.content)

        when (item.action) {
            "insert" -> retrofit.noteApi.uploadNote(dto)
            "update" -> retrofit.noteApi.updateNote(note.id, dto)
            "delete" -> retrofit.noteApi.deleteNote(note.id)
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
            holderName = card.holderName
        )

        when (item.action) {
            "insert" -> retrofit.cardApi.uploadCard(dto)
            "update" -> retrofit.cardApi.updateCard(card.id, dto)
            "delete" -> retrofit.cardApi.deleteCard(card.id)
        }
    }

    private suspend fun syncPassword(item: QueueSyncEntity, retrofit: RetrofitClient) {
        val password = Json.decodeFromString<PasswordEntity>(item.payload)
        val dto = PasswordDto(
            id = password.id,
            title = password.title,
            url = password.url,
            username = password.username,
            password = password.password
        )

        when (item.action) {
            "insert" -> retrofit.passwordApi.uploadPassword(dto)
            "update" -> retrofit.passwordApi.updatePassword(password.id, dto)
            "delete" -> retrofit.passwordApi.deletePassword(password.id)
        }
    }
}