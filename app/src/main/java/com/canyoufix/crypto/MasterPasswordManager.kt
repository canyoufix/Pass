package com.canyoufix.crypto

import com.canyoufix.domain.repository.SetupCredentials
import javax.crypto.SecretKey

class MasterPasswordManager(
    private val preferences: CredentialStore,
    private val crypto: CryptoEngine,
) {
    sealed interface AuthResult {
        data class Authenticated(val key: SecretKey, val decoy: Boolean) : AuthResult

        data object InvalidCredentials : AuthResult
    }

    fun authenticate(password: String): AuthResult {
        val salt = preferences.getSalt() ?: return AuthResult.InvalidCredentials
        val realBlock = preferences.getEncryptedTestBlock() ?: return AuthResult.InvalidCredentials
        val key = crypto.deriveKeyFromPassword(password, salt)
        if (crypto.decrypt(realBlock, key) == SecurityConfig.TEST_BLOCK)
            return AuthResult.Authenticated(key, false)
        val decoyBlock = preferences.getFakeEncryptedTestBlock()
        return if (
            decoyBlock != null && crypto.decrypt(decoyBlock, key) == SecurityConfig.FAKE_TEST_BLOCK
        )
            AuthResult.Authenticated(key, true)
        else AuthResult.InvalidCredentials
    }

    fun setup(credentials: SetupCredentials): SecretKey {
        credentials.validate()
        check(preferences.getSalt() == null) { "Мастер-пароль уже установлен" }
        val salt = crypto.generateSalt()
        val realKey = crypto.deriveKeyFromPassword(credentials.password, salt)
        val decoyKey = crypto.deriveKeyFromPassword(credentials.decoyPassword, salt)
        preferences.saveCredentials(
            salt,
            crypto.encrypt(SecurityConfig.TEST_BLOCK, realKey),
            crypto.encrypt(SecurityConfig.FAKE_TEST_BLOCK, decoyKey),
        )
        return realKey
    }

    fun verifyMasterPassword(password: String): Boolean {
        val salt = preferences.getSalt() ?: return false
        val block = preferences.getEncryptedTestBlock() ?: return false
        return crypto.decrypt(block, crypto.deriveKeyFromPassword(password, salt)) ==
            SecurityConfig.TEST_BLOCK
    }
}
