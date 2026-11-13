package com.canyoufix.crypto

import java.util.concurrent.atomic.AtomicLong
import javax.crypto.SecretKey

/** Process-memory only. Domain/presentation depend on SessionRepository instead. */
object SessionAESKeyHolder : SessionKeyStore {
    private val revision = AtomicLong(0)
    override val generation: Long
        get() = revision.get()

    @Volatile private var activeKey: SecretKey? = null
    @Volatile
    override var isDecoy: Boolean = false
        private set

    override val isAuthenticated: Boolean
        get() = activeKey != null

    override val key: SecretKey
        get() = activeKey ?: throw SecurityException("Хранилище заблокировано")

    @Synchronized
    override fun setKey(secretKey: SecretKey, decoy: Boolean) {
        CryptoManager.clearCachedKeys()
        isDecoy = decoy
        activeKey = secretKey
        revision.incrementAndGet()
    }

    @Synchronized
    override fun clear() {
        CryptoManager.clearCachedKeys()
        revision.incrementAndGet()
        activeKey = null
        isDecoy = false
    }
}
