package com.canyoufix.data

import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SessionKeyStore
import com.canyoufix.data.mapping.EntryCodec
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.*
import org.junit.Test

class EntryCodecSessionTest {
    private class Keys : SessionKeyStore {
        override var generation = 0L
        private var active: SecretKey? = null
        override var isDecoy = false
        override val isAuthenticated
            get() = active != null

        override val key
            get() = active ?: error("locked")

        override fun setKey(secretKey: SecretKey, decoy: Boolean) {
            active = secretKey
            isDecoy = decoy
            generation++
        }

        override fun clear() {
            active = null
            isDecoy = false
            generation++
        }
    }

    @Test
    fun aLockAndUnlockInvalidateAnOperationEvenIfTheKeyBytesAreEqual() {
        val keys = Keys()
        keys.setKey(SecretKeySpec(ByteArray(32) { 1 }, "AES"))
        val operation = EntryCodec(CryptoManager, keys).capture()
        val encrypted = operation.encode("private")
        operation.requireActive()
        assertEquals("private", operation.decode(encrypted))
        keys.clear()
        keys.setKey(SecretKeySpec(ByteArray(32) { 1 }, "AES"))
        assertThrows(IllegalStateException::class.java) { operation.requireActive() }
    }

    @Test
    fun decoySessionCannotAcquireAReadableOrWritableCipher() {
        val keys = Keys()
        keys.setKey(SecretKeySpec(ByteArray(32) { 1 }, "AES"), decoy = true)
        val codec = EntryCodec(CryptoManager, keys)
        assertFalse(codec.canRead)
        assertThrows(IllegalStateException::class.java) { codec.capture() }
    }
}
