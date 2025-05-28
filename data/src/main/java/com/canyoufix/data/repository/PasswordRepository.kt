package com.canyoufix.data.repository

import android.net.Uri
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionAESKeyHolder
import com.canyoufix.data.dao.PasswordDao
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.data.entity.QueueSyncEntity
import com.canyoufix.data.mapping.DtoToEntity.toPasswordEntity
import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.dto.PasswordDto
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.crypto.SecretKey

class PasswordRepository(
    private val passwordDao: PasswordDao,
    private val retrofitClientProvider: RetrofitClientProvider,
    private val syncSettingsStore: SyncSettingsStore,
    private val queueSyncRepository: QueueSyncRepository
) {
    private val cryptoManager = CryptoManager

    val getAllPasswords: Flow<List<PasswordEntity>> = passwordDao.getAll()
        .map { decryptPasswords(it) }

    val getAllEncryptedPasswords: Flow<List<PasswordEntity>> = passwordDao.getAll()

    suspend fun insert(password: PasswordEntity) {
        val encrypted = encryptPassword(password)
        passwordDao.insert(encrypted)

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "password",
                action = "insert",
                payload = Json.encodeToString(encrypted)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = passwordToDto(encrypted)
                retrofit.passwordApi.uploadPassword(dto)
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }

    suspend fun insertFromServer(passwordDto: PasswordDto) {
        val entity = passwordDto.toPasswordEntity()
        passwordDao.insert(entity)
    }


    suspend fun update(password: PasswordEntity) {
        val timestamp = System.currentTimeMillis()
        val updatedPassword = password.copy(lastModified = timestamp)

        val encrypted = encryptPassword(updatedPassword)

        passwordDao.update(
            id = encrypted.id,
            title = encrypted.title,
            url = encrypted.url,
            username = encrypted.username,
            password = encrypted.password,
            timestamp = encrypted.lastModified
        )

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "password",
                action = "update",
                payload = Json.encodeToString(encrypted)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = passwordToDto(encrypted)
                retrofit.passwordApi.updatePassword(encrypted.id, dto)
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }

    suspend fun delete(password: PasswordEntity) {
        val timestamp = System.currentTimeMillis()
        val updatedPassword = password.copy(
            lastModified = timestamp,
            isDeleted = true
        )

        val encrypted = encryptPassword(updatedPassword)

        passwordDao.delete(
            id = encrypted.id,
            timestamp = encrypted.lastModified
        )

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "password",
                action = "update", // мягкое удаление!
                payload = Json.encodeToString(encrypted)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = passwordToDto(encrypted)
                retrofit.passwordApi.deletePassword(encrypted.id, dto)
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }



    fun getById(id: String): Flow<PasswordEntity?> {
        return passwordDao.getById(id)
            .map { it?.let { decryptPassword(it) } }
    }

    suspend fun getAccountsByDomain(domain: String): List<PasswordEntity> {
        val allPasswords = getAllPasswords.first()
        return allPasswords.filter {
            val siteDomain = extractDomain(it.url)
            siteDomain.equals(domain, ignoreCase = true)
        }
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun passwordToDto(password: PasswordEntity): PasswordDto {
        return PasswordDto(
            id = password.id,
            title = password.title,
            url = password.url,
            username = password.username,
            password = password.password,
            lastModified = password.lastModified,
            isDeleted = password.isDeleted
        )
    }

    private fun encryptPassword(password: PasswordEntity): PasswordEntity {
        val key = SessionAESKeyHolder.key
        return encryptPassword(password, key)
    }

    private fun encryptPassword(password: PasswordEntity, key: SecretKey): PasswordEntity {
        return password.copy(
            title = cryptoManager.encrypt(password.title, key),
            url = cryptoManager.encrypt(password.url, key),
            username = cryptoManager.encrypt(password.username, key),
            password = cryptoManager.encrypt(password.password, key)
        )
    }

    private fun decryptPassword(password: PasswordEntity): PasswordEntity {
        val key = SessionAESKeyHolder.key
        return decryptPassword(password, key)
    }

    fun decryptPassword(password: PasswordEntity, key: SecretKey): PasswordEntity {
        return password.copy(
            title = cryptoManager.decrypt(password.title, key) ?: "DECRYPTION_ERROR",
            url = cryptoManager.decrypt(password.url, key) ?: "",
            username = cryptoManager.decrypt(password.username, key) ?: "",
            password = cryptoManager.decrypt(password.password, key) ?: ""
        )
    }

    private fun decryptPasswords(passwords: List<PasswordEntity>): List<PasswordEntity> {
        val key = SessionAESKeyHolder.key
        return passwords.map { decryptPassword(it, key) }
    }
}

