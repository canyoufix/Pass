package com.canyoufix.crypto

import com.canyoufix.domain.repository.EncryptionMode
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.SecretKey

interface CryptoEngine {
    val currentMode: EncryptionMode

    fun generateSalt(): String

    fun deriveKeyFromPassword(password: String, salt: String): SecretKey

    fun encrypt(data: String, key: SecretKey): String

    fun decrypt(data: String, key: SecretKey): String?

    fun encryptWithMode(data: String, key: SecretKey, mode: EncryptionMode): String

    fun decryptWithMode(data: String, key: SecretKey, mode: EncryptionMode): String?

    fun encryptFile(inputStream: InputStream, outputFile: File, secretKey: SecretKey): Boolean

    fun decryptFile(inputFile: File, outputStream: OutputStream, secretKey: SecretKey): Boolean
}

interface SessionKeyStore {
    val generation: Long
    val isAuthenticated: Boolean
    val isDecoy: Boolean
    val key: SecretKey

    fun setKey(secretKey: SecretKey, decoy: Boolean = false)

    fun clear()
}

interface CredentialStore {
    fun getSalt(): String?

    fun getEncryptedTestBlock(): String?

    fun getFakeEncryptedTestBlock(): String?

    fun saveCredentials(salt: String, testBlock: String, fakeTestBlock: String)

    fun clearAllData()
}
