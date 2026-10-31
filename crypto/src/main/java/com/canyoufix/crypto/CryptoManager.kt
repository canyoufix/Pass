package com.canyoufix.crypto

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager : CryptoEngine {

    override fun generateSalt(): String {
        val salt = ByteArray(SecurityConfig.SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    override fun deriveKeyFromPassword(password: String, salt: String): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec =
            PBEKeySpec(
                password.toCharArray(),
                Base64.getDecoder().decode(salt),
                SecurityConfig.PBKDF2_ITERATIONS,
                SecurityConfig.KEY_SIZE,
            )
        return try {
            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    override fun encrypt(data: String, key: SecretKey): String {
        val secretKeySpec = SecretKeySpec(key.encoded, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    override fun decrypt(data: String, key: SecretKey): String? {
        return try {
            val secretKeySpec = SecretKeySpec(key.encoded, "AES")
            val decoded = Base64.getDecoder().decode(data)
            val iv = decoded.copyOfRange(0, 12)
            val ciphertextWithTag = decoded.copyOfRange(12, decoded.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec)

            val decrypted = cipher.doFinal(ciphertextWithTag)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    override fun encryptFile(
        inputStream: InputStream,
        outputFile: File,
        secretKey: SecretKey,
    ): Boolean {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            FileOutputStream(outputFile).use { fileOut ->
                fileOut.write(iv) // Сначала записываем IV
                CipherOutputStream(fileOut, cipher).use { cipherOut ->
                    val buffer = ByteArray(8192) // 8KB буфер, можно увеличить/уменьшить по памяти
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        cipherOut.write(buffer, 0, bytesRead)
                    }
                    cipherOut.flush()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun decryptFile(
        inputFile: File,
        outputStream: OutputStream,
        secretKey: SecretKey,
    ): Boolean {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            FileInputStream(inputFile).use { fileIn ->
                val iv = ByteArray(12)
                if (fileIn.read(iv) != iv.size) {
                    throw IOException("Не удалось прочитать IV из файла")
                }
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                CipherInputStream(fileIn, cipher).use { cipherIn ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (cipherIn.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
