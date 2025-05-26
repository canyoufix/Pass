package com.canyoufix.data.repository

import android.net.Uri
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionAESKeyHolder
import com.canyoufix.data.dao.PasswordDao
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.dto.PasswordDto
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.crypto.SecretKey

class PasswordRepository(
    private val passwordDao: PasswordDao,
    private val retrofitClientProvider: RetrofitClientProvider,
    private val syncSettingsStore: SyncSettingsStore
) {
    private val cryptoManager = CryptoManager

    val getAllPasswords: Flow<List<PasswordEntity>> = passwordDao.getAll()
        .map { passwords -> decryptPasswords(passwords) }

    val getAllEncryptedPasswords: Flow<List<PasswordEntity>> = passwordDao.getAll()

    suspend fun insert(password: PasswordEntity) {
        val encryptedPassword = encryptPassword(password)
        passwordDao.insert(encryptedPassword)

        if (syncSettingsStore.isEnabled()) {
            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = passwordToDto(encryptedPassword)
                retrofit.passwordApi.uploadPassword(dto)
            } catch (e: Exception) {
                // Обработка ошибки (например, логирование)
            }
        }
    }

    suspend fun update(password: PasswordEntity) {
        val encryptedPassword = encryptPassword(password)
        passwordDao.update(encryptedPassword)

        if (syncSettingsStore.isEnabled()) {
            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = passwordToDto(encryptedPassword)
                retrofit.passwordApi.updatePassword(encryptedPassword.id, dto)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    suspend fun delete(password: PasswordEntity) {
        val encryptedPassword = encryptPassword(password)
        passwordDao.delete(encryptedPassword)

        if (syncSettingsStore.isEnabled()) {
            try {
                val retrofit = retrofitClientProvider.getClient()
                retrofit.passwordApi.deletePassword(encryptedPassword.id)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    fun getById(id: String): Flow<PasswordEntity?> {
        return passwordDao.getById(id)
            .map { it?.let { decryptPassword(it) } }
    }

    // Маппинг Entity -> DTO
    private fun passwordToDto(password: PasswordEntity): PasswordDto {
        return PasswordDto(
            id = password.id,
            title = password.title,
            url = password.url,
            username = password.username,
            password = password.password
        )
    }

    // Шифрование
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

    // Дешифрование
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
        return decryptPasswords(passwords, key)
    }

    private fun decryptPasswords(passwords: List<PasswordEntity>, key: SecretKey): List<PasswordEntity> {
        return passwords.map { decryptPassword(it, key) }
    }

    suspend fun getAccountsByDomain(domain: String): List<PasswordEntity> {
        val allPasswords = getAllPasswords.first()
        return allPasswords.filter { passwordEntity ->
            val siteDomain = extractDomain(passwordEntity.url)
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
}

