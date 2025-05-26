package com.canyoufix.data.repository

import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionAESKeyHolder
import com.canyoufix.data.dao.CardDao
import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.QueueSyncEntity
import com.canyoufix.settings.datastore.SyncSettingsStore
import com.canyoufix.sync.dto.CardDto
import com.canyoufix.sync.retrofit.RetrofitClientProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.crypto.SecretKey

class CardRepository(
    private val cardDao: CardDao,
    private val retrofitClientProvider: RetrofitClientProvider,
    private val syncSettingsStore: SyncSettingsStore,
    private val queueSyncRepository: QueueSyncRepository
) {
    private val cryptoManager = CryptoManager

    val getAllCards: Flow<List<CardEntity>> = cardDao.getAll()
        .map { cards -> decryptCards(cards) }

    val getAllEncryptedCards: Flow<List<CardEntity>> = cardDao.getAll()

    suspend fun insert(card: CardEntity) {
        val encryptedCard = encryptCard(card)
        cardDao.insert(encryptedCard)

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "card",
                action = "insert",
                payload = Json.encodeToString(encryptedCard)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = cardToDto(encryptedCard)
                retrofit.cardApi.uploadCard(dto)
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }

    suspend fun update(card: CardEntity) {
        val encryptedCard = encryptCard(card)
        cardDao.update(encryptedCard)

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "card",
                action = "update",
                payload = Json.encodeToString(encryptedCard)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                val dto = cardToDto(encryptedCard)
                retrofit.cardApi.updateCard(encryptedCard.id, dto)
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }

    suspend fun delete(card: CardEntity) {
        val encryptedCard = encryptCard(card)
        cardDao.delete(encryptedCard)

        if (syncSettingsStore.isEnabled()) {
            val queueEntity = QueueSyncEntity(
                id = UUID.randomUUID().toString(),
                type = "card",
                action = "delete",
                payload = Json.encodeToString(encryptedCard)
            )
            queueSyncRepository.insert(queueEntity)

            try {
                val retrofit = retrofitClientProvider.getClient()
                retrofit.cardApi.deleteCard(encryptedCard.id)
                queueSyncRepository.delete(queueEntity)
            } catch (_: Exception) {
                // Оставляем в очереди
            }
        }
    }


    fun getById(id: String): Flow<CardEntity?> {
        return cardDao.getById(id)
            .map { it?.let { decryptCard(it) } }
    }

    private fun cardToDto(card: CardEntity): CardDto {
        return CardDto(
            id = card.id,
            title = card.title,
            number = card.number,
            expiryDate = card.expiryDate,
            cvc = card.cvc,
            holderName = card.holderName
        )
    }

    // Шифрование
    private fun encryptCard(card: CardEntity): CardEntity {
        val key = SessionAESKeyHolder.key
        return encryptCard(card, key)
    }

    private fun encryptCard(card: CardEntity, key: SecretKey): CardEntity {
        return card.copy(
            title = cryptoManager.encrypt(card.title, key),
            number = cryptoManager.encrypt(card.number, key),
            expiryDate = cryptoManager.encrypt(card.expiryDate, key),
            cvc = cryptoManager.encrypt(card.cvc, key),
            holderName = cryptoManager.encrypt(card.holderName, key)
        )
    }

    // Дешифрование
    private fun decryptCard(card: CardEntity): CardEntity {
        val key = SessionAESKeyHolder.key
        return decryptCard(card, key)
    }

    fun decryptCard(card: CardEntity, key: SecretKey): CardEntity {
        return card.copy(
            title = cryptoManager.decrypt(card.title, key) ?: "DECRYPTION_ERROR",
            number = cryptoManager.decrypt(card.number, key) ?: "",
            expiryDate = cryptoManager.decrypt(card.expiryDate, key) ?: "",
            cvc = cryptoManager.decrypt(card.cvc, key) ?: "",
            holderName = cryptoManager.decrypt(card.holderName, key) ?: ""
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


