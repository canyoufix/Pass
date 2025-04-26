package com.canyoufix.data.repository

import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionKeyHolder
import com.canyoufix.data.dao.CardDao
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardRepository(private val cardDao: CardDao) {
    private val cryptoManager = CryptoManager

    val allCards: Flow<List<CardEntity>> = cardDao.getAll()
        .map { cards -> decryptCards(cards) }

    suspend fun insert(card: CardEntity) {
        val encryptedCard = encryptCard(card)
        cardDao.insert(encryptedCard)
    }

    suspend fun update(card: CardEntity) {
        val encryptedCard = encryptCard(card)
        cardDao.update(encryptedCard)
    }

    suspend fun delete(card: CardEntity) {
        val encryptedCard = encryptCard(card)
        cardDao.delete(encryptedCard)
    }

    fun getById(id: String): Flow<CardEntity?> {
        return cardDao.getById(id)
            .map { it?.let { decryptCard(it) } }
    }

    private fun encryptCard(card: CardEntity): CardEntity {
        val key = SessionKeyHolder.key ?: throw SecurityException("No active session key")

        return card.copy(
            title = cryptoManager.encrypt(card.title, key),
            cardNumber = cryptoManager.encrypt(card.cardNumber, key),
            expiryDate = cryptoManager.encrypt(card.expiryDate, key),
            cvc = cryptoManager.encrypt(card.cvc, key),
            cardHolder = cryptoManager.encrypt(card.cardHolder, key)
        )
    }

    private fun decryptCard(card: CardEntity): CardEntity {
        val key = SessionKeyHolder.key ?: throw SecurityException("No active session key")

        return card.copy(
            title = cryptoManager.decrypt(card.title, key) ?: "DECRYPTION_ERROR",
            cardNumber = cryptoManager.decrypt(card.cardNumber, key) ?: "",
            expiryDate = cryptoManager.decrypt(card.expiryDate, key) ?: "",
            cvc = cryptoManager.decrypt(card.cvc, key) ?: "",
            cardHolder = cryptoManager.decrypt(card.cardHolder, key) ?: ""
        )
    }

    private fun decryptCards(cards: List<CardEntity>): List<CardEntity> {
        return cards.map { decryptCard(it) }
    }
}