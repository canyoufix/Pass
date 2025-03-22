package com.canyoufix.data.repository

import com.canyoufix.data.dao.CardDao
import com.canyoufix.data.entity.CardEntity
import kotlinx.coroutines.flow.Flow

class CardRepository(private val cardDao: CardDao) {
    val allCards: Flow<List<CardEntity>> = cardDao.getAll()

    suspend fun insert(card: CardEntity) {
        cardDao.insert(card)
    }

    suspend fun update(card: CardEntity) {
        cardDao.update(card)
    }

    suspend fun delete(card: CardEntity) {
        cardDao.delete(card)
    }
}