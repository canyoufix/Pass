package com.canyoufix.data.repository

import android.net.Uri
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionAESKeyHolder
import com.canyoufix.data.dao.PasswordDao
import com.canyoufix.data.entity.PasswordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.crypto.SecretKey

class PasswordRepository(private val passwordDao: PasswordDao) {
    private val cryptoManager = CryptoManager

    val getAllPasswords: Flow<List<PasswordEntity>> = passwordDao.getAll()
        .map { passwords -> decryptPasswords(passwords) }

    val getAllEncryptedPasswords: Flow<List<PasswordEntity>> = passwordDao.getAll()

    suspend fun insert(password: PasswordEntity) {
        val encryptedPassword = encryptPassword(password)
        passwordDao.insert(encryptedPassword)
    }

    suspend fun update(password: PasswordEntity) {
        val encryptedPassword = encryptPassword(password)
        passwordDao.update(encryptedPassword)
    }

    suspend fun delete(password: PasswordEntity) {
        val encryptedPassword = encryptPassword(password)
        passwordDao.delete(encryptedPassword)
    }

    fun getById(id: String): Flow<PasswordEntity?> {
        return passwordDao.getById(id)
            .map { it?.let { decryptPassword(it) } }
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
        return decryptPasswords(passwords, key)
    }

    private fun decryptPasswords(passwords: List<PasswordEntity>, key: SecretKey): List<PasswordEntity> {
        return passwords.map { decryptPassword(it, key) }
    }

    suspend fun getAccountsByDomain(domain: String): List<PasswordEntity> {
        // Получаем все пароли, расшифрованные с помощью метода getAllPasswords
        val allPasswords = getAllPasswords.first()

        // Фильтруем пароли по домену
        return allPasswords.filter { passwordEntity ->
            val siteDomain = extractDomain(passwordEntity.url)
            siteDomain.equals(domain, ignoreCase = true)
        }
    }

    // Функция для извлечения домена из URL
    private fun extractDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
