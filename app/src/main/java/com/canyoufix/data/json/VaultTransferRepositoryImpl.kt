package com.canyoufix.data.json

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.room.withTransaction
import com.canyoufix.crypto.*
import com.canyoufix.data.database.AppDatabase
import com.canyoufix.data.dto.ExportData
import com.canyoufix.data.dto.MetaInfo
import com.canyoufix.data.entity.*
import com.canyoufix.data.mapping.*
import com.canyoufix.data.sync.nextLocalTimestamp
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.repository.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class VaultTransferRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase,
    private val preferences: CredentialStore,
    private val crypto: CryptoEngine,
    private val codec: EntryCodec,
    private val dispatchers: CoroutineDispatchers,
    private val clock: Clock,
) : VaultTransferRepository {
    private val resolver
        get() = context.contentResolver

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override suspend fun export(destination: String, encrypted: Boolean) =
        withContext(dispatchers.io) {
            val cipher = codec.capture()
            val snapshot = database.withTransaction {
                cipher.requireActive()
                ExportData(
                    encrypted =
                        if (encrypted)
                            preferences.getEncryptedTestBlock() ?: error("Нет проверочного блока")
                        else "",
                    salt =
                        if (encrypted) preferences.getSalt() ?: error("Нет соли шифрования")
                        else "",
                    passwords = database.passwordDao().getAll().first(),
                    cards = database.cardDao().getAll().first(),
                    notes = database.noteDao().getAll().first(),
                    meta =
                        MetaInfo(
                            "1.0",
                            Instant.ofEpochMilli(clock.millis()).toString(),
                            Build.MODEL,
                            Build.VERSION.RELEASE,
                        ),
                )
            }
            val data =
                if (encrypted) snapshot
                else
                    snapshot.copy(
                        passwords =
                            snapshot.passwords.map {
                                it.toDomain { v -> cipher.decode(v) }.toEntity { v -> v }
                            },
                        cards =
                            snapshot.cards.map {
                                it.toDomain { v -> cipher.decode(v) }.toEntity { v -> v }
                            },
                        notes =
                            snapshot.notes.map {
                                it.toDomain { v -> cipher.decode(v) }.toEntity { v -> v }
                            },
                    )
            val content = json.encodeToString(data)
            currentCoroutineContext().ensureActive()
            cipher.requireActive()
            resolver.openOutputStream(Uri.parse(destination), "wt")?.bufferedWriter()?.use {
                it.write(content)
            } ?: throw IOException("Не удалось открыть документ для записи")
        }

    override suspend fun importData(source: String, mode: ImportMode, sourcePassword: String) =
        withContext(dispatchers.io) {
            val cipher = codec.capture()
            val content =
                resolver.openInputStream(Uri.parse(source))?.use { input ->
                    val buffer = ByteArray(8192)
                    val bytes = ByteArrayOutputStream()
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        require(bytes.size() + count <= MAX_IMPORT_BYTES) {
                            "Файл импорта превышает 16 МиБ"
                        }
                        bytes.write(buffer, 0, count)
                    }
                    bytes.toString(Charsets.UTF_8.name())
                } ?: throw IOException("Не удалось прочитать документ")
            val imported =
                try {
                    json.decodeFromString<ExportData>(content)
                } catch (_: kotlinx.serialization.SerializationException) {
                    throw IllegalArgumentException("Неверный формат файла импорта")
                }
            require(imported.meta.version == "1.0") { "Неподдерживаемая версия файла импорта" }
            val sourceKey =
                if (!imported.encrypted.isNullOrBlank()) {
                    val key = crypto.deriveKeyFromPassword(sourcePassword, imported.salt)
                    require(crypto.decrypt(imported.encrypted, key) == SecurityConfig.TEST_BLOCK) {
                        "Неверный пароль файла импорта"
                    }
                    key
                } else null
            val decode: (String) -> String = {
                if (sourceKey == null) it else codec.decode(it, sourceKey)
            }
            // Validate and decrypt the entire file before mutating any local table.
            val passwords = imported.passwords.map { it.toDomain(decode).toEntity(cipher::encode) }
            val cards = imported.cards.map { it.toDomain(decode).toEntity(cipher::encode) }
            val notes = imported.notes.map { it.toDomain(decode).toEntity(cipher::encode) }
            require(
                passwords.map { it.id }.distinct().size == passwords.size &&
                    cards.map { it.id }.distinct().size == cards.size &&
                    notes.map { it.id }.distinct().size == notes.size,
            ) {
                "Файл содержит повторяющиеся идентификаторы"
            }
            require(
                (passwords.map { it.id } + cards.map { it.id } + notes.map { it.id }).all {
                    it.isNotBlank() && it.length <= 200
                },
            ) {
                "Некорректный идентификатор записи"
            }
            currentCoroutineContext().ensureActive()
            cipher.requireActive()
            database.withTransaction {
                cipher.requireActive()
                val now = clock.millis()
                if (mode == ImportMode.REPLACE) {
                    database
                        .passwordDao()
                        .getAll()
                        .first()
                        .filter { local -> passwords.none { it.id == local.id } }
                        .forEach {
                            val deleted =
                                it.copy(
                                    isDeleted = true,
                                    lastModified = nextLocalTimestamp(now, it.lastModified),
                                )
                            database.passwordDao().insert(deleted)
                            enqueue("password", "delete", json.encodeToString(deleted))
                        }
                    database
                        .cardDao()
                        .getAll()
                        .first()
                        .filter { local -> cards.none { it.id == local.id } }
                        .forEach {
                            val deleted =
                                it.copy(
                                    isDeleted = true,
                                    lastModified = nextLocalTimestamp(now, it.lastModified),
                                )
                            database.cardDao().insert(deleted)
                            enqueue("card", "delete", json.encodeToString(deleted))
                        }
                    database
                        .noteDao()
                        .getAll()
                        .first()
                        .filter { local -> notes.none { it.id == local.id } }
                        .forEach {
                            val deleted =
                                it.copy(
                                    isDeleted = true,
                                    lastModified = nextLocalTimestamp(now, it.lastModified),
                                )
                            database.noteDao().insert(deleted)
                            enqueue("note", "delete", json.encodeToString(deleted))
                        }
                }
                for (entry in passwords) {
                    val local = database.passwordDao().getStoredById(entry.id)
                    if (
                        mode == ImportMode.REPLACE ||
                            local == null ||
                            entry.lastModified > local.lastModified
                    ) {
                        val saved =
                            entry.copy(
                                lastModified =
                                    nextLocalTimestamp(
                                        maxOf(now, entry.lastModified),
                                        local?.lastModified,
                                    ),
                            )
                        database.passwordDao().insert(saved)
                        enqueue(
                            "password",
                            if (local == null) "insert" else "update",
                            json.encodeToString(saved),
                        )
                    }
                }
                for (entry in cards) {
                    val local = database.cardDao().getStoredById(entry.id)
                    if (
                        mode == ImportMode.REPLACE ||
                            local == null ||
                            entry.lastModified > local.lastModified
                    ) {
                        val saved =
                            entry.copy(
                                lastModified =
                                    nextLocalTimestamp(
                                        maxOf(now, entry.lastModified),
                                        local?.lastModified,
                                    ),
                            )
                        database.cardDao().insert(saved)
                        enqueue(
                            "card",
                            if (local == null) "insert" else "update",
                            json.encodeToString(saved),
                        )
                    }
                }
                for (entry in notes) {
                    val local = database.noteDao().getStoredById(entry.id)
                    if (
                        mode == ImportMode.REPLACE ||
                            local == null ||
                            entry.lastModified > local.lastModified
                    ) {
                        val saved =
                            entry.copy(
                                lastModified =
                                    nextLocalTimestamp(
                                        maxOf(now, entry.lastModified),
                                        local?.lastModified,
                                    ),
                            )
                        database.noteDao().insert(saved)
                        enqueue(
                            "note",
                            if (local == null) "insert" else "update",
                            json.encodeToString(saved),
                        )
                    }
                }
            }
        }

    private suspend fun enqueue(type: String, action: String, payload: String) {
        database
            .queueSyncDao()
            .insert(QueueSyncEntity(UUID.randomUUID().toString(), type, action, payload))
    }

    private companion object {
        const val MAX_IMPORT_BYTES = 16 * 1024 * 1024
    }
}
