package com.canyoufix.data.mapping

import com.canyoufix.crypto.CryptoEngine
import com.canyoufix.crypto.SessionKeyStore
import javax.crypto.SecretKey

class EntryCodec(private val crypto: CryptoEngine, private val keys: SessionKeyStore) {
    val canRead: Boolean
        get() = keys.isAuthenticated && !keys.isDecoy

    fun requireWritable() {
        check(canRead) { "Хранилище недоступно для записи" }
    }

    fun decode(value: String, key: SecretKey): String =
        crypto.decrypt(value, key)
            ?: throw IllegalStateException(
                "Не удалось расшифровать запись. Проверьте ключ и целостность данных",
            )

    /** One operation uses one key; a lock/reset invalidates its permission to commit. */
    fun capture(): CipherSession {
        requireWritable()
        val generation = keys.generation
        val key = keys.key
        return CipherSession(key, generation).also { it.requireActive() }
    }

    inner class CipherSession
    internal constructor(val key: SecretKey, private val generation: Long) {
        fun encode(value: String): String = crypto.encrypt(value, key)

        fun decode(value: String): String = this@EntryCodec.decode(value, key)

        fun requireActive() {
            check(canRead && keys.generation == generation) {
                "Сессия изменилась. Повторите операцию после входа"
            }
        }
    }
}
