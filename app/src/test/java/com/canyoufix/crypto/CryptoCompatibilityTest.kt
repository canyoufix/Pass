package com.canyoufix.crypto

import com.canyoufix.domain.repository.SetupCredentials
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.util.Base64
import org.junit.Assert.*
import org.junit.Test

class CryptoCompatibilityTest {
    private val salt = "AAECAwQFBgcICQoLDA0ODw=="

    @Test
    fun decryptsLegacyPbkdf2AesGcmFixture() {
        val key = CryptoManager.deriveKeyFromPassword("legacy-password", salt)
        assertEquals(
            "ebc912e03a48689698c21563a3f9c36e5c67d4c830f463de3b147ea1bf038166",
            key.encoded.joinToString("") { "%02x".format(it.toInt() and 255) },
        )
        assertEquals(
            "Совместимость legacy / secret",
            CryptoManager.decrypt(
                "EBESExQVFhcYGRob88V7nNOX9zLTMzAdejlguda4DBEs08a9d5YZD21xHpZ+gSlJwv12PQ3GqS1Pp88nN3c9xUQkEY6sUw==",
                key,
            ),
        )
    }

    @Test
    fun tamperedOrMalformedRecordsAreRejected() {
        val key = CryptoManager.deriveKeyFromPassword("legacy-password", salt)
        val encoded = CryptoManager.encrypt("private value", key)
        val damaged = Base64.getDecoder().decode(encoded)
        damaged[damaged.lastIndex] = (damaged.last().toInt() xor 1).toByte()
        assertNull(CryptoManager.decrypt(Base64.getEncoder().encodeToString(damaged), key))
        assertNull(CryptoManager.decrypt("not-base64", key))
        assertNull(CryptoManager.decrypt("", key))
    }

    @Test
    fun legacyFileFormatRoundTrips() {
        val key = CryptoManager.deriveKeyFromPassword("legacy-password", salt)
        val file = Files.createTempFile("pass-compat-", ".enc").toFile()
        try {
            val bytes = ByteArray(40000) { (it % 251).toByte() }
            assertTrue(CryptoManager.encryptFile(ByteArrayInputStream(bytes), file, key))
            val restored = ByteArrayOutputStream()
            assertTrue(CryptoManager.decryptFile(file, restored, key))
            assertArrayEquals(bytes, restored.toByteArray())
        } finally {
            file.delete()
        }
    }

    @Test
    fun realAndDecoyCredentialsHaveIndependentKeysAndNoImplicitSessionMutation() {
        val store =
            object : CredentialStore {
                var storedSalt: String? = null
                var real: String? = null
                var decoy: String? = null

                override fun getSalt() = storedSalt

                override fun getEncryptedTestBlock() = real

                override fun getFakeEncryptedTestBlock() = decoy

                override fun saveCredentials(
                    salt: String,
                    testBlock: String,
                    fakeTestBlock: String,
                ) {
                    storedSalt = salt
                    real = testBlock
                    decoy = fakeTestBlock
                }

                override fun clearAllData() {
                    storedSalt = null
                    real = null
                    decoy = null
                }
            }
        SessionAESKeyHolder.clear()
        val manager = MasterPasswordManager(store, CryptoManager)
        manager.setup(SetupCredentials("real", "real", "decoy", "decoy"))
        val real = manager.authenticate("real") as MasterPasswordManager.AuthResult.Authenticated
        val decoy = manager.authenticate("decoy") as MasterPasswordManager.AuthResult.Authenticated
        assertFalse(real.decoy)
        assertTrue(decoy.decoy)
        assertFalse(real.key.encoded.contentEquals(decoy.key.encoded))
        assertTrue(manager.verifyMasterPassword("real"))
        assertFalse(manager.verifyMasterPassword("decoy"))
        assertEquals(
            MasterPasswordManager.AuthResult.InvalidCredentials,
            manager.authenticate("wrong"),
        )
        assertFalse(SessionAESKeyHolder.isAuthenticated)
    }
}
