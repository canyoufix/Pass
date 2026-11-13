package com.canyoufix.data

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.canyoufix.crypto.*
import com.canyoufix.data.database.AppDatabase
import com.canyoufix.data.mapping.EntryCodec
import com.canyoufix.data.repository.NoteRepository
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.model.NoteEntry
import com.canyoufix.domain.repository.SessionRepository
import com.canyoufix.domain.repository.SessionState
import com.canyoufix.domain.repository.SetupCredentials
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: NoteRepository
    private lateinit var context: Context

    private class Keys : SessionKeyStore {
        override var generation = 1L
        private var active: SecretKey? = SecretKeySpec(ByteArray(32) { 7 }, "AES")
        override var isDecoy = false
        override val isAuthenticated
            get() = active != null

        override val key
            get() = active ?: error("locked")

        override fun setKey(secretKey: SecretKey, decoy: Boolean) {
            active = secretKey
            isDecoy = decoy
            generation++
        }

        override fun clear() {
            active = null
            isDecoy = false
            generation++
        }
    }

    private val session =
        object : SessionRepository {
            override val state = MutableStateFlow<SessionState>(SessionState.Unlocked)

            override suspend fun initialize() = Unit

            override suspend fun unlock(password: String) = Unit

            override suspend fun setup(credentials: SetupCredentials) = Unit

            override suspend fun verify(password: String) = true

            override fun lock() = Unit

            override suspend fun reset() = Unit
        }

    @Before
    fun prepare() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        check(
            context.packageName.startsWith("com.canyoufix.data") &&
                context.packageName.contains("test"),
        )
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository =
            NoteRepository(
                database,
                EntryCodec(CryptoManager, Keys()),
                session,
                CoroutineDispatchers(Dispatchers.IO, Dispatchers.Default),
                Clock.fixed(Instant.ofEpochMilli(1000), ZoneOffset.UTC),
            )
    }

    @After
    fun close() {
        database.close()
    }

    @Test
    fun recordAndOutboxRoundTripThroughRealSqlite() = runBlocking {
        val entry = NoteEntry("fixture", "Тест", "Секрет теста", 0L)
        repository.save(entry)
        val restored = repository.observeById(entry.id).first()!!
        assertEquals(entry.content, restored.content)
        assertEquals(1000L, restored.lastModified)
        assertNotEquals(entry.content, database.noteDao().getStoredById(entry.id)!!.content)
        assertEquals(1, database.queueSyncDao().getAll().first().size)
    }

    @Test
    fun failedOutboxInsertRollsBackRecordInsert() = runBlocking {
        withContext(Dispatchers.IO) {
            database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER reject_test_outbox BEFORE INSERT ON queue_sync BEGIN SELECT RAISE(ABORT, 'test failure'); END",
            )
        }
        try {
            repository.save(NoteEntry("rollback", "Тест", "fixture", 0L))
            fail("Expected database failure")
        } catch (_: android.database.sqlite.SQLiteException) {}
        assertNull(database.noteDao().getStoredById("rollback"))
        assertTrue(database.queueSyncDao().getAll().first().isEmpty())
    }

    @Test
    fun staleEditorCannotOverwriteACommittedChange() = runBlocking {
        repository.save(NoteEntry("revision", "Тест", "original", 0L))
        val original = repository.observeById("revision").first()!!
        repository.save(original.copy(content = "newer"))
        try {
            repository.save(original.copy(content = "stale"))
            fail("Expected conflict")
        } catch (_: IllegalStateException) {}
        assertEquals("newer", repository.observeById("revision").first()!!.content)
        assertEquals(1001L, repository.observeById("revision").first()!!.lastModified)
        assertEquals(2, database.queueSyncDao().getAll().first().size)
    }

    @Test
    fun credentialCommitPersistsBothIndependentPasswordsInTheIsolatedTestPackage() = runBlocking {
        withContext(Dispatchers.IO) {
            val prefs = SecurePrefsManager(context)
            prefs.clearAllData()
            try {
                val manager = MasterPasswordManager(prefs, CryptoManager)
                manager.setup(
                    SetupCredentials("test-real", "test-real", "test-decoy", "test-decoy"),
                )
                val reopened = MasterPasswordManager(SecurePrefsManager(context), CryptoManager)
                assertFalse(
                    (reopened.authenticate("test-real")
                            as MasterPasswordManager.AuthResult.Authenticated)
                        .decoy,
                )
                assertTrue(
                    (reopened.authenticate("test-decoy")
                            as MasterPasswordManager.AuthResult.Authenticated)
                        .decoy,
                )
                assertFalse(reopened.verifyMasterPassword("test-decoy"))
            } finally {
                prefs.clearAllData()
            }
        }
    }
}
