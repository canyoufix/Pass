package com.canyoufix.crypto

import android.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import javax.crypto.spec.GCMParameterSpec

object KeyGenerator {

    // Генерирует соль для PBKDF2
    fun generateSalt(): String {
        val salt = ByteArray(SecurityConfig.SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    // Создаёт ключ из пароля и соли
    fun deriveKeyFromPassword(password: String, salt: String): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(
            password.toCharArray(),
            Base64.decode(salt, Base64.NO_WRAP),
            SecurityConfig.PBKDF2_ITERATIONS,
            SecurityConfig.KEY_SIZE
        )
        val secretKey = factory.generateSecret(spec).encoded
        return SecretKeySpec(secretKey, "AES")
    }

    // Шифрует данные и возвращает IV + Ciphertext + Tag в Base64
    fun encrypt(data: String, key: SecretKeySpec): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedData = cipher.doFinal(data.toByteArray())

        // Получаем IV (12 байт) и добавляем его к зашифрованным данным
        val iv = cipher.iv
        val combined = iv + encryptedData

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    // Дешифрует данные, извлекая IV из начала массива
    fun decrypt(data: String, key: SecretKeySpec): String? {
        return try {
            val combined = Base64.decode(data, Base64.NO_WRAP)

            // Извлекаем IV (первые 12 байт)
            val iv = combined.copyOfRange(0, 12)

            // Остальное – ciphertext + tag
            val ciphertextWithTag = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val params = GCMParameterSpec(128, iv) // 128 бит = 16 байт (размер тега)
            cipher.init(Cipher.DECRYPT_MODE, key, params)

            val decryptedData = cipher.doFinal(ciphertextWithTag)
            String(decryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}