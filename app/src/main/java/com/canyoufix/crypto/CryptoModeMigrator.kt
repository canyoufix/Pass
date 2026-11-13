package com.canyoufix.crypto

import androidx.room.withTransaction
import com.canyoufix.data.database.AppDatabase
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.data.entity.QueueSyncEntity
import com.canyoufix.data.serialization.storageJson
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.repository.EncryptionMode
import com.canyoufix.settings.datastore.CryptoModeStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CryptoModeMigrator(
    private val database: AppDatabase,
    private val credentials: CredentialStore,
    private val crypto: CryptoEngine,
    private val keys: SessionKeyStore,
    private val modeStore: CryptoModeStore,
    private val dispatchers: CoroutineDispatchers,
) {
    suspend fun switchTo(target: EncryptionMode) =
        withContext(dispatchers.io) {
            require(keys.isAuthenticated && !keys.isDecoy) {
                "Режим шифрования можно изменить только в основном хранилище"
            }
            val source = modeStore.current
            if (source == target) return@withContext
            val key = keys.key
            database.withTransaction {
                database.passwordDao().getAllStored().forEach {
                    database.passwordDao().insert(reencrypt(it, key, target))
                }
                database.cardDao().getAllStored().forEach {
                    database.cardDao().insert(reencrypt(it, key, target))
                }
                database.noteDao().getAllStored().forEach {
                    database.noteDao().insert(reencrypt(it, key, target))
                }
                database.queueSyncDao().getAll().first().forEach { item ->
                    database.queueSyncDao().insert(item.copy(payload = reencryptQueue(item, key, target)))
                }
            }
            val salt = credentials.getSalt()
            val testBlock = credentials.getEncryptedTestBlock()
            val fakeTestBlock = credentials.getFakeEncryptedTestBlock()
            if (salt != null && testBlock != null && fakeTestBlock != null) {
                val plaintext = crypto.decrypt(testBlock, key)
                    ?: error("Не удалось обновить проверочный блок")
                credentials.saveCredentials(
                    salt,
                    crypto.encryptWithMode(plaintext, key, target),
                    fakeTestBlock,
                )
            }
            modeStore.set(target)
        }

    private fun reencrypt(entity: PasswordEntity, key: javax.crypto.SecretKey, target: EncryptionMode) =
        entity.copy(
            title = reencrypt(entity.title, key, target),
            url = reencrypt(entity.url, key, target),
            username = reencrypt(entity.username, key, target),
            password = reencrypt(entity.password, key, target),
        )

    private fun reencrypt(entity: CardEntity, key: javax.crypto.SecretKey, target: EncryptionMode) =
        entity.copy(
            title = reencrypt(entity.title, key, target),
            number = reencrypt(entity.number, key, target),
            expiryDate = reencrypt(entity.expiryDate, key, target),
            cvc = reencrypt(entity.cvc, key, target),
            holderName = reencrypt(entity.holderName, key, target),
        )

    private fun reencrypt(entity: NoteEntity, key: javax.crypto.SecretKey, target: EncryptionMode) =
        entity.copy(
            title = reencrypt(entity.title, key, target),
            content = reencrypt(entity.content, key, target),
        )

    private fun reencryptQueue(
        item: QueueSyncEntity,
        key: javax.crypto.SecretKey,
        target: EncryptionMode,
    ): String =
        when (item.type) {
            "password" ->
                storageJson.encodeToString(
                    reencrypt(storageJson.decodeFromString<PasswordEntity>(item.payload), key, target),
                )
            "card" ->
                storageJson.encodeToString(
                    reencrypt(storageJson.decodeFromString<CardEntity>(item.payload), key, target),
                )
            "note" ->
                storageJson.encodeToString(
                    reencrypt(storageJson.decodeFromString<NoteEntity>(item.payload), key, target),
                )
            else -> error("Неизвестный тип записи в очереди")
        }

    private fun reencrypt(value: String, key: javax.crypto.SecretKey, target: EncryptionMode): String {
        val plaintext = crypto.decrypt(value, key) ?: error("Не удалось расшифровать запись при смене режима")
        return crypto.encryptWithMode(plaintext, key, target)
    }
}
