package com.canyoufix.data.repository

import com.canyoufix.data.dao.PasswordDao
import com.canyoufix.data.entity.PasswordEntity
import kotlinx.coroutines.flow.Flow

class PasswordRepository(private val passwordDao: PasswordDao) {
    val allPasswords: Flow<List<PasswordEntity>> = passwordDao.getAll()

    suspend fun insert(password: PasswordEntity) {
        passwordDao.insert(password)
    }

    suspend fun update(password: PasswordEntity) {
        passwordDao.update(password)
    }

    suspend fun delete(password: PasswordEntity) {
        passwordDao.delete(password)
    }

    fun getById(id: String): Flow<PasswordEntity?> {
        return passwordDao.getById(id)
    }
}