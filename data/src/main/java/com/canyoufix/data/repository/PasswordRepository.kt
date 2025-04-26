package com.canyoufix.data.repository

import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionKeyHolder
import com.canyoufix.data.dao.PasswordDao
import com.canyoufix.data.entity.PasswordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PasswordRepository(private val passwordDao: PasswordDao) {
    private val cryptoManager = CryptoManager

    val allPasswords: Flow<List<PasswordEntity>> = passwordDao.getAll()
        .map { passwords -> decryptPasswords(passwords) }

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
        val key = SessionKeyHolder.key ?: throw SecurityException("No active session key")

        return password.copy(
            title = cryptoManager.encrypt(password.title, key),
            site = cryptoManager.encrypt(password.site, key),
            username = cryptoManager.encrypt(password.username, key),
            password = cryptoManager.encrypt(password.password, key)
        )
    }

    private fun decryptPassword(password: PasswordEntity): PasswordEntity {
        val key = SessionKeyHolder.key ?: throw SecurityException("No active session key")

        return password.copy(
            title = cryptoManager.decrypt(password.title, key) ?: "DECRYPTION_ERROR",
            site = cryptoManager.decrypt(password.site, key) ?: "",
            username = cryptoManager.decrypt(password.username, key) ?: "",
            password = cryptoManager.decrypt(password.password, key) ?: ""
        )
    }

    private fun decryptPasswords(passwords: List<PasswordEntity>): List<PasswordEntity> {
        return passwords.map { decryptPassword(it) }
    }
}