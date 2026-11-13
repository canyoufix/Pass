package com.canyoufix.data.session

import android.content.Context
import com.canyoufix.crypto.*
import com.canyoufix.data.database.DatabaseManager
import com.canyoufix.data.files.VaultFileLock
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.repository.*
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DefaultSessionRepository(
    private val preferences: CredentialStore,
    private val passwords: MasterPasswordManager,
    private val database: DatabaseManager,
    private val dispatchers: CoroutineDispatchers,
    private val settings: SettingsRepository,
    private val context: Context,
    private val keys: SessionKeyStore,
    private val fileLock: VaultFileLock,
) : SessionRepository {
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Loading)
    override val state = mutableState.asStateFlow()
    private val mutex = Mutex()
    private val generation = AtomicLong(0)

    override suspend fun initialize() = mutex.withLock {
        withContext(dispatchers.io) {
            mutableState.value =
                when {
                    keys.isAuthenticated -> SessionState.Unlocked
                    preferences.getSalt() == null -> SessionState.SetupRequired
                    else -> SessionState.Locked
                }
        }
    }

    override suspend fun unlock(password: String) = mutex.withLock {
        require(password.isNotEmpty()) { "Введите мастер-пароль" }
        val attempt = generation.get()
        val result = withContext(dispatchers.computation) { passwords.authenticate(password) }
        currentCoroutineContext().ensureActive()
        if (attempt != generation.get()) throw CancellationException("Сессия была заблокирована")
        when (result) {
            is MasterPasswordManager.AuthResult.Authenticated -> {
                keys.setKey(result.key, result.decoy)
                mutableState.value = SessionState.Unlocked
            }
            MasterPasswordManager.AuthResult.InvalidCredentials ->
                throw IllegalArgumentException("Неверный мастер-пароль")
        }
    }

    override suspend fun setup(credentials: SetupCredentials) = mutex.withLock {
        credentials.validate()
        val attempt = generation.get()
        val key = withContext(dispatchers.computation) { passwords.setup(credentials) }
        currentCoroutineContext().ensureActive()
        if (attempt != generation.get()) throw CancellationException("Сессия была заблокирована")
        keys.setKey(key)
        mutableState.value = SessionState.Unlocked
    }

    override suspend fun verify(password: String): Boolean =
        withContext(dispatchers.computation) {
            passwords.verifyMasterPassword(password)
        }

    override fun lock() {
        generation.incrementAndGet()
        keys.clear()
        if (mutableState.value != SessionState.SetupRequired)
            mutableState.value = SessionState.Locked
    }

    override suspend fun reset() = mutex.withLock {
        generation.incrementAndGet()
        keys.clear()
        mutableState.value = SessionState.Locked
        settings.saveSyncSettings(SyncSettings())
        withContext(dispatchers.io) {
            database.clearAllData()
            fileLock.mutex.withLock {
                File(context.filesDir, "EncryptedFiles")
                    .listFiles()
                    .orEmpty()
                    .filter { it.isFile }
                    .forEach {
                        check(it.delete()) { "Не удалось удалить зашифрованный файл" }
                    }
                context.cacheDir
                    .listFiles()
                    .orEmpty()
                    .filter { it.name.startsWith("decrypted-") }
                    .forEach { it.delete() }
            }
            preferences.clearAllData()
        }
        keys.clear()
        mutableState.value = SessionState.SetupRequired
    }
}
