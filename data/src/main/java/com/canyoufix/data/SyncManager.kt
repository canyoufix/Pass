package com.canyoufix.data

import androidx.room.withTransaction
import com.canyoufix.data.database.AppDatabase
import com.canyoufix.data.entity.*
import com.canyoufix.data.mapping.DtoToEntity.toCardEntity
import com.canyoufix.data.mapping.DtoToEntity.toNoteEntity
import com.canyoufix.data.mapping.DtoToEntity.toPasswordEntity
import com.canyoufix.data.serialization.storageJson
import com.canyoufix.data.sync.SyncQueueProcessor
import com.canyoufix.data.sync.acceptRemoteChange
import com.canyoufix.data.sync.requireSuccess
import com.canyoufix.data.sync.upsertRemote
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.repository.SyncCoordinator
import com.canyoufix.domain.repository.SyncSettings
import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.dto.*
import com.canyoufix.sync.retrofit.*
import java.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class SyncManager(
    private val database: AppDatabase,
    private val settingsStore: SyncSettingsStore,
    private val clients: RetrofitClientProvider,
    private val queue: SyncQueueProcessor,
    private val dispatchers: CoroutineDispatchers,
    private val clock: Clock,
) : SyncCoordinator {
    private val mutex = Mutex()

    override suspend fun testConnection(settings: SyncSettings) =
        withContext(dispatchers.io) {
            settings.validateEndpoint()
            RetrofitClient(settings.ip.trim(), settings.port).pingApi.ping().requireSuccess()
        }

    override suspend fun sync() = mutex.withLock {
        withContext(dispatchers.io) {
            val settings = settingsStore.syncSettingsFlow.first()
            if (!settings.isEnabled) return@withContext
            val startedAt = clock.millis()
            val client = clients.getClient()
            // The original server implements full snapshots, not /sync?since= endpoints.
            val passwords =
                client.passwordApi.getAllPasswords().associateBy { it.id }.toMutableMap()
            val cards = client.cardApi.getAllCards().associateBy { it.id }.toMutableMap()
            val notes = client.noteApi.getAllNotes().associateBy { it.id }.toMutableMap()
            queue.flush(
                database.queueSyncDao().getAll().first(),
                { item ->
                    require(item.action in setOf("insert", "update", "delete")) {
                        "Неизвестное действие в очереди"
                    }
                    when (item.type) {
                        "password" -> {
                            val queued = storageJson.decodeFromString<PasswordEntity>(item.payload)
                            val e = database.passwordDao().getStoredById(queued.id) ?: queued
                            val dto =
                                PasswordDto(
                                    e.id,
                                    e.title,
                                    e.url,
                                    e.username,
                                    e.password,
                                    e.lastModified,
                                    e.isDeleted,
                                )
                            passwords[e.id] =
                                upsertRemote(
                                    dto,
                                    passwords[e.id],
                                    PasswordDto::lastModified,
                                    client.passwordApi::uploadPassword,
                                    { client.passwordApi.updatePassword(e.id, it) },
                                    { client.passwordApi.getById(e.id) },
                                )
                        }
                        "card" -> {
                            val queued = storageJson.decodeFromString<CardEntity>(item.payload)
                            val e = database.cardDao().getStoredById(queued.id) ?: queued
                            val dto =
                                CardDto(
                                    e.id,
                                    e.title,
                                    e.number,
                                    e.expiryDate,
                                    e.cvc,
                                    e.holderName,
                                    e.lastModified,
                                    e.isDeleted,
                                )
                            cards[e.id] =
                                upsertRemote(
                                    dto,
                                    cards[e.id],
                                    CardDto::lastModified,
                                    client.cardApi::uploadCard,
                                    { client.cardApi.updateCard(e.id, it) },
                                    { client.cardApi.getById(e.id) },
                                )
                        }
                        "note" -> {
                            val queued = storageJson.decodeFromString<NoteEntity>(item.payload)
                            val e = database.noteDao().getStoredById(queued.id) ?: queued
                            val dto = NoteDto(e.id, e.title, e.content, e.lastModified, e.isDeleted)
                            notes[e.id] =
                                upsertRemote(
                                    dto,
                                    notes[e.id],
                                    NoteDto::lastModified,
                                    client.noteApi::uploadNote,
                                    { client.noteApi.updateNote(e.id, it) },
                                    { client.noteApi.getById(e.id) },
                                )
                        }
                        else -> error("Неизвестный тип записи в очереди")
                    }
                },
                { database.queueSyncDao().delete(it) },
                identity = {
                    it.type +
                        ":" +
                        Json.parseToJsonElement(it.payload)
                            .jsonObject
                            .getValue("id")
                            .jsonPrimitive
                            .content
                },
            )
            database.withTransaction {
                val current = settingsStore.syncSettingsFlow.first()
                check(
                    current.isEnabled && current.ip == settings.ip && current.port == settings.port,
                ) {
                    "Настройки синхронизации изменились"
                }
                val pending =
                    database
                        .queueSyncDao()
                        .getAll()
                        .first()
                        .map {
                            it.type to
                                Json.parseToJsonElement(it.payload)
                                    .jsonObject
                                    .getValue("id")
                                    .jsonPrimitive
                                    .content
                        }
                        .toSet()
                for (dto in passwords.values) {
                    val local = database.passwordDao().getStoredById(dto.id)
                    if (
                        acceptRemoteChange(
                            local?.lastModified,
                            dto.lastModified,
                            ("password" to dto.id) in pending,
                        )
                    )
                        database.passwordDao().insert(dto.toPasswordEntity())
                }
                for (dto in cards.values) {
                    val local = database.cardDao().getStoredById(dto.id)
                    if (
                        acceptRemoteChange(
                            local?.lastModified,
                            dto.lastModified,
                            ("card" to dto.id) in pending,
                        )
                    )
                        database.cardDao().insert(dto.toCardEntity())
                }
                for (dto in notes.values) {
                    val local = database.noteDao().getStoredById(dto.id)
                    if (
                        acceptRemoteChange(
                            local?.lastModified,
                            dto.lastModified,
                            ("note" to dto.id) in pending,
                        )
                    )
                        database.noteDao().insert(dto.toNoteEntity())
                }
            }
            // This is a diagnostic timestamp; full snapshots do not rely on a client-clock cursor.
            settingsStore.saveLastSyncTime(startedAt, settings.ip, settings.port)
        }
    }
}
