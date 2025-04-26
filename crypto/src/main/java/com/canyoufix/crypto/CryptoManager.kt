package com.canyoufix.crypto

import android.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {

    fun generateSalt(): String {
        val salt = ByteArray(SecurityConfig.SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

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

    fun encrypt(data: String, key: SecretKey): String {
        val secretKeySpec = SecretKeySpec(key.encoded, "AES") // поддержка и SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(data: String, key: SecretKey): String? {
        return try {
            val secretKeySpec = SecretKeySpec(key.encoded, "AES")
            val decoded = Base64.decode(data, Base64.NO_WRAP)
            val iv = decoded.copyOfRange(0, 12)
            val ciphertextWithTag = decoded.copyOfRange(12, decoded.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec)

            val decrypted = cipher.doFinal(ciphertextWithTag)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
