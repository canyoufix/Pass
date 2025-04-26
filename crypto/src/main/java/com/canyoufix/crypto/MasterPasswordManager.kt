package com.canyoufix.crypto



sealed class AuthResult {
    object Success : AuthResult()
    object FakeSuccess : AuthResult()
    data class Failure(val error: String) : AuthResult()
}

fun authenticateMasterPassword(
    password: String,
    prefsManager: SecurePrefsManager
): AuthResult {
    val salt = prefsManager.getSalt()
    val encryptedTestBlock = prefsManager.getEncryptedTestBlock()
    val fakeEncryptedTestBlock = prefsManager.getFakeEncryptedTestBlock()

    if (salt != null && encryptedTestBlock != null) {
        val key = CryptoManager.deriveKeyFromPassword(password, salt)

        val decryptedRealBlock = try {
            CryptoManager.decrypt(encryptedTestBlock, key)
        } catch (e: Exception) {
            null
        }

        val decryptedFakeBlock = try {
            fakeEncryptedTestBlock?.let { CryptoManager.decrypt(it, key) }
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


fun setupMasterPassword(
    password: String,
    confirmPassword: String,
    fakePassword: String,
    confirmFakePassword: String,
    prefsManager: SecurePrefsManager,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    val error = validatePasswords(password, confirmPassword, fakePassword, confirmFakePassword)
    if (error != null) {
        onError(error)
        return
    }

    val salt = CryptoManager.generateSalt()
    val masterKey = CryptoManager.deriveKeyFromPassword(password, salt)
    val testBlock = CryptoManager.encrypt(SecurityConfig.TEST_BLOCK, masterKey)

    prefsManager.saveSalt(salt)
    prefsManager.saveEncryptedTestBlock(testBlock)
    SessionKeyHolder.key = masterKey

    if (fakePassword.isNotEmpty()) {
        val fakeKey = CryptoManager.deriveKeyFromPassword(fakePassword, salt)
        val fakeTestBlock = CryptoManager.encrypt(SecurityConfig.FAKE_TEST_BLOCK, fakeKey)
        prefsManager.saveFakeEncryptedTestBlock(fakeTestBlock)
    }

    onSuccess()
}


fun validatePasswords(
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