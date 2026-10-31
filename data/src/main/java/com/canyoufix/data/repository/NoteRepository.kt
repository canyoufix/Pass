package com.canyoufix.data.repository

import androidx.room.withTransaction
import com.canyoufix.data.database.AppDatabase
import com.canyoufix.data.entity.QueueSyncEntity
import com.canyoufix.data.mapping.EntryCodec
import com.canyoufix.data.mapping.toDomain
import com.canyoufix.data.mapping.toEntity
import com.canyoufix.data.serialization.storageJson
import com.canyoufix.data.sync.nextLocalTimestamp
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.model.NoteEntry
import com.canyoufix.domain.repository.NoteRepository as Contract
import com.canyoufix.domain.repository.SessionRepository
import com.canyoufix.domain.repository.SessionState
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class NoteRepository(
    private val database: AppDatabase,
    private val codec: EntryCodec,
    private val session: SessionRepository,
    private val dispatchers: CoroutineDispatchers,
    private val clock: Clock,
) : Contract {
    private val dao = database.noteDao()

    override fun observeAll(): Flow<List<NoteEntry>> =
        session.state
            .flatMapLatest { state ->
                if (state == SessionState.Unlocked && codec.canRead) {
                    dao.getAll().map { entries ->
                        val cipher = codec.capture()
                        entries.map { it.toDomain(cipher::decode) }.also { cipher.requireActive() }
                    }
                } else flowOf(emptyList())
            }
            .distinctUntilChanged()
            .flowOn(dispatchers.computation)

    override fun observeById(id: String): Flow<NoteEntry?> =
        session.state
            .flatMapLatest { state ->
                if (state == SessionState.Unlocked && codec.canRead) {
                    dao.getById(id).map {
                        it?.let { stored ->
                            val cipher = codec.capture()
                            stored.toDomain(cipher::decode).also { cipher.requireActive() }
                        }
                    }
                } else flowOf(null)
            }
            .flowOn(dispatchers.computation)

    override suspend fun save(entry: NoteEntry) =
        withContext(dispatchers.io) {
            val cipher = codec.capture()
            val encrypted = entry.copy(lastModified = clock.millis()).toEntity(cipher::encode)
            database.withTransaction {
                cipher.requireActive()
                val existing = dao.getStoredById(entry.id)
                if (existing != null)
                    requireUnchangedRevision(
                        entry.lastModified,
                        existing.lastModified,
                        existing.isDeleted,
                    )
                val action = if (existing == null) "insert" else "update"
                val saved =
                    encrypted.copy(
                        lastModified = nextLocalTimestamp(clock.millis(), existing?.lastModified),
                    )
                dao.insert(saved)
                database
                    .queueSyncDao()
                    .insert(
                        QueueSyncEntity(
                            UUID.randomUUID().toString(),
                            "note",
                            action,
                            storageJson.encodeToString(saved),
                        ),
                    )
            }
        }

    override suspend fun delete(entry: NoteEntry) =
        withContext(dispatchers.io) {
            val cipher = codec.capture()
            database.withTransaction {
                cipher.requireActive()
                val stored = dao.getStoredById(entry.id) ?: return@withTransaction
                if (stored.isDeleted) return@withTransaction
                requireUnchangedRevision(entry.lastModified, stored.lastModified, stored.isDeleted)
                val deleted =
                    stored.copy(
                        isDeleted = true,
                        lastModified = nextLocalTimestamp(clock.millis(), stored.lastModified),
                    )
                dao.insert(deleted)
                database
                    .queueSyncDao()
                    .insert(
                        QueueSyncEntity(
                            UUID.randomUUID().toString(),
                            "note",
                            "delete",
                            storageJson.encodeToString(deleted),
                        ),
                    )
            }
        }
}
