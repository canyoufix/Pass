package com.canyoufix.data.repository

import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionAESKeyHolder
import com.canyoufix.data.dao.CardDao
import com.canyoufix.data.entity.CardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.crypto.SecretKey

class CardRepository(private val cardDao: CardDao) {
    private val cryptoManager = CryptoManager

    val getAllCards: Flow<List<CardEntity>> = cardDao.getAll()
        .map { cards -> decryptCards(cards) }

    val getAllEncryptedCards: Flow<List<CardEntity>> = cardDao.getAll()

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
        val key = SessionAESKeyHolder.key
        return encryptCard(card, key)
    }

    private fun encryptCard(card: CardEntity, key: SecretKey): CardEntity {
        return card.copy(
            title = cryptoManager.encrypt(card.title, key),
            cardNumber = cryptoManager.encrypt(card.cardNumber, key),
            expiryDate = cryptoManager.encrypt(card.expiryDate, key),
            cvc = cryptoManager.encrypt(card.cvc, key),
            cardHolder = cryptoManager.encrypt(card.cardHolder, key)
        )
    }

    private fun decryptCard(card: CardEntity): CardEntity {
        val key = SessionAESKeyHolder.key
        return decryptCard(card, key)
    }

    fun decryptCard(card: CardEntity, key: SecretKey): CardEntity {
        return card.copy(
            title = cryptoManager.decrypt(card.title, key) ?: "DECRYPTION_ERROR",
            cardNumber = cryptoManager.decrypt(card.cardNumber, key) ?: "",
            expiryDate = cryptoManager.decrypt(card.expiryDate, key) ?: "",
            cvc = cryptoManager.decrypt(card.cvc, key) ?: "",
            cardHolder = cryptoManager.decrypt(card.cardHolder, key) ?: ""
        )
    }

    private fun decryptCards(cards: List<CardEntity>): List<CardEntity> {
        val key = SessionAESKeyHolder.key
        return decryptCards(cards, key)
    }

    private fun decryptCards(cards: List<CardEntity>, key: SecretKey): List<CardEntity> {
        return cards.map { decryptCard(it, key) }
    }
}
