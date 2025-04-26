package com.canyoufix.crypto


class MasterPasswordManager(
    private val prefsManager: SecurePrefsManager,
    private val cryptoManager: CryptoManager
) {
    sealed class AuthResult {
        object Success : AuthResult()
        object FakeSuccess : AuthResult()
        data class Failure(val error: String) : AuthResult()
    }

    fun authenticate(password: String): AuthResult {
        val salt = prefsManager.getSalt()
        val encryptedTestBlock = prefsManager.getEncryptedTestBlock()
        val fakeEncryptedTestBlock = prefsManager.getFakeEncryptedTestBlock()

        if (salt != null && encryptedTestBlock != null) {
            val key = cryptoManager.deriveKeyFromPassword(password, salt)

            val decryptedRealBlock = try {
                cryptoManager.decrypt(encryptedTestBlock, key)
            } catch (e: Exception) {
                null
            }

            val decryptedFakeBlock = try {
                fakeEncryptedTestBlock?.let { cryptoManager.decrypt(it, key) }
            } catch (e: Exception) {
                null
            }

            return when {
                decryptedRealBlock == SecurityConfig.TEST_BLOCK -> {
                    SessionKeyHolder.key = key
                    AuthResult.Success
                }
                decryptedFakeBlock == SecurityConfig.FAKE_TEST_BLOCK -> {
                    SessionKeyHolder.key = key
                    AuthResult.FakeSuccess
                }
                else -> AuthResult.Failure("Неверный пароль!")
            }
        } else {
            return AuthResult.Failure("Данные аутентификации отсутствуют.")
        }
    }

    fun setup(
        password: String,
        confirmPassword: String,
        fakePassword: String,
        confirmFakePassword: String,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        val error = validatePasswords(password, confirmPassword, fakePassword, confirmFakePassword)
        if (error != null) {
            onError(error)
            return
        }

        val salt = cryptoManager.generateSalt()
        val masterKey = cryptoManager.deriveKeyFromPassword(password, salt)
        val testBlock = cryptoManager.encrypt(SecurityConfig.TEST_BLOCK, masterKey)

        prefsManager.saveSalt(salt)
        prefsManager.saveEncryptedTestBlock(testBlock)
        SessionKeyHolder.key = masterKey

        if (fakePassword.isNotEmpty()) {
            val fakeKey = cryptoManager.deriveKeyFromPassword(fakePassword, salt)
            val fakeTestBlock = cryptoManager.encrypt(SecurityConfig.FAKE_TEST_BLOCK, fakeKey)
            prefsManager.saveFakeEncryptedTestBlock(fakeTestBlock)
        }

        onSuccess()
    }

    fun verifyMasterPassword(password: String): Boolean {
        val salt = prefsManager.getSalt() ?: return false
        val encryptedTestBlock = prefsManager.getEncryptedTestBlock() ?: return false

        val key = cryptoManager.deriveKeyFromPassword(password, salt)

        return try {
            val decryptedBlock = cryptoManager.decrypt(encryptedTestBlock, key)
            decryptedBlock == SecurityConfig.TEST_BLOCK
        } catch (e: Exception) {
            false
        }
    }

    private fun validatePasswords(
        password: String,
        confirmPassword: String,
        fakePassword: String,
        confirmFakePassword: String
    ): String? {
        return when {
            password.isEmpty() || confirmPassword.isEmpty() -> "Пароли не могут быть пустыми!"
            password != confirmPassword -> "Пароли не совпадают!"
            fakePassword.isEmpty() || confirmFakePassword.isEmpty() -> "Фейковые пароли не могут быть пустыми!"
            fakePassword != confirmFakePassword -> "Фейковые пароли не совпадают!"
            password == fakePassword -> "Настоящий и фейковый пароли не должны совпадать!"
            else -> null
        }
    }
}