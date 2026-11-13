package com.canyoufix.crypto

import com.canyoufix.domain.repository.EncryptionMode
import com.canyoufix.settings.datastore.CryptoModeStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.SecretWithEncapsulation
import org.bouncycastle.crypto.generators.MLKEMKeyPairGenerator
import org.bouncycastle.crypto.kems.MLKEMExtractor
import org.bouncycastle.crypto.kems.MLKEMGenerator
import org.bouncycastle.crypto.params.MLKEMKeyGenerationParameters
import org.bouncycastle.crypto.params.MLKEMParameters
import org.bouncycastle.crypto.params.MLKEMPrivateKeyParameters
import org.bouncycastle.crypto.params.MLKEMPublicKeyParameters
import org.bouncycastle.math.ec.rfc7748.X25519

object CryptoManager : CryptoEngine {
    private val secureRandom = SecureRandom()
    private val rsaKeys = ConcurrentHashMap<String, KeyPair>()
    private val mlKemKeys = ConcurrentHashMap<String, AsymmetricCipherKeyPair>()
    @Volatile private var modeStore: CryptoModeStore? = null

    override val currentMode: EncryptionMode
        get() = modeStore?.current ?: EncryptionMode.AES

    fun configure(store: CryptoModeStore) {
        modeStore = store
    }

    fun clearCachedKeys() {
        rsaKeys.clear()
        mlKemKeys.clear()
    }

    override fun generateSalt(): String {
        val salt = ByteArray(SecurityConfig.SALT_LENGTH)
        secureRandom.nextBytes(salt)
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

    override fun encrypt(data: String, key: SecretKey): String =
        encryptWithMode(data, key, currentMode)

    override fun decrypt(data: String, key: SecretKey): String? =
        try {
            val envelope = decodeEnvelope(data)
            if (envelope == null) String(decryptAesBytes(data, key), Charsets.UTF_8)
            else decryptEnvelope(envelope.first, envelope.second, key)
        } catch (_: Exception) {
            null
        }

    override fun encryptWithMode(
        data: String,
        key: SecretKey,
        mode: EncryptionMode,
    ): String {
        val plaintext = data.toByteArray(Charsets.UTF_8)
        return when (mode) {
            EncryptionMode.AES -> encryptAes(plaintext, key)
            EncryptionMode.RSA_AES -> encodeEnvelope(mode, encryptRsa(plaintext, key))
            EncryptionMode.X25519_AES -> encodeEnvelope(mode, encryptX25519(plaintext, key))
            EncryptionMode.ML_KEM_AES -> encodeEnvelope(mode, encryptMlKem(plaintext, key))
        }
    }

    override fun decryptWithMode(
        data: String,
        key: SecretKey,
        mode: EncryptionMode,
    ): String? =
        try {
            val envelope = decodeEnvelope(data)
            val bytes =
                if (envelope == null) {
                    require(mode == EncryptionMode.AES)
                    decryptAesBytes(data, key)
                } else {
                    require(envelope.first == mode)
                    decryptEnvelopeBytes(envelope.first, envelope.second, key)
                }
            String(bytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }

    override fun encryptFile(
        inputStream: InputStream,
        outputFile: File,
        secretKey: SecretKey,
    ): Boolean {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12).also(secureRandom::nextBytes)
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(secretKey), GCMParameterSpec(128, iv))

            FileOutputStream(outputFile).use { fileOut ->
                fileOut.write(iv)
                CipherOutputStream(fileOut, cipher).use { cipherOut ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        cipherOut.write(buffer, 0, bytesRead)
                    }
                    cipherOut.flush()
                }
            }
            true
        } catch (_: Exception) {
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
                cipher.init(Cipher.DECRYPT_MODE, aesKey(secretKey), GCMParameterSpec(128, iv))

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
        } catch (_: Exception) {
            false
        }
    }

    private fun encryptAes(plaintext: ByteArray, key: SecretKey): String =
        Base64.getEncoder().encodeToString(encryptAesBytes(plaintext, key))

    private fun encryptAesBytes(plaintext: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey(key))
        return cipher.iv + cipher.doFinal(plaintext)
    }

    private fun decryptAesBytes(data: String, key: SecretKey): ByteArray {
        val decoded = Base64.getDecoder().decode(data)
        require(decoded.size > 12)
        val iv = decoded.copyOfRange(0, 12)
        val ciphertext = decoded.copyOfRange(12, decoded.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, aesKey(key), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun encryptRsa(plaintext: ByteArray, sessionKey: SecretKey): ByteArray {
        val dataKey = ByteArray(32).also(secureRandom::nextBytes)
        val encrypted = encryptAesBytes(plaintext, SecretKeySpec(dataKey, "AES"))
        val wrapped = rsaCipher(Cipher.ENCRYPT_MODE, rsaKeyPair(sessionKey).public).doFinal(dataKey)
        return intBytes(wrapped.size) + wrapped + encrypted
    }

    private fun decryptRsa(payload: ByteArray, sessionKey: SecretKey): ByteArray {
        val wrappedSize = readInt(payload, 0)
        require(wrappedSize > 0 && payload.size > 4 + wrappedSize + 12)
        val wrapped = payload.copyOfRange(4, 4 + wrappedSize)
        val encrypted = payload.copyOfRange(4 + wrappedSize, payload.size)
        val dataKey = rsaCipher(Cipher.DECRYPT_MODE, rsaKeyPair(sessionKey).private).doFinal(wrapped)
        return decryptAesBytes(
            Base64.getEncoder().encodeToString(encrypted),
            SecretKeySpec(dataKey, "AES"),
        )
    }

    private fun encryptX25519(plaintext: ByteArray, sessionKey: SecretKey): ByteArray {
        val recipientPrivate = x25519RecipientPrivateKey(sessionKey)
        val recipientPublic = ByteArray(X25519.POINT_SIZE)
        X25519.generatePublicKey(recipientPrivate, 0, recipientPublic, 0)
        val ephemeralPrivate = ByteArray(X25519.SCALAR_SIZE)
        X25519.generatePrivateKey(secureRandom, ephemeralPrivate)
        val shared = ByteArray(X25519.POINT_SIZE)
        check(X25519.calculateAgreement(ephemeralPrivate, 0, recipientPublic, 0, shared, 0))
        return ephemeralPublicKey(ephemeralPrivate) +
            encryptAesBytes(plaintext, SecretKeySpec(sha256(shared, "x25519-aes"), "AES"))
    }

    private fun decryptX25519(payload: ByteArray, sessionKey: SecretKey): ByteArray {
        require(payload.size > X25519.POINT_SIZE + 12)
        val ephemeralPublic = payload.copyOfRange(0, X25519.POINT_SIZE)
        val encrypted = payload.copyOfRange(X25519.POINT_SIZE, payload.size)
        val shared = ByteArray(X25519.POINT_SIZE)
        check(
            X25519.calculateAgreement(
                x25519RecipientPrivateKey(sessionKey),
                0,
                ephemeralPublic,
                0,
                shared,
                0,
            ),
        )
        return decryptAesBytes(
            Base64.getEncoder().encodeToString(encrypted),
            SecretKeySpec(sha256(shared, "x25519-aes"), "AES"),
        )
    }

    private fun encryptMlKem(plaintext: ByteArray, sessionKey: SecretKey): ByteArray {
        val keyPair = mlKemKeyPair(sessionKey)
        val encapsulated: SecretWithEncapsulation =
            MLKEMGenerator(secureRandom)
                .generateEncapsulated(keyPair.public as MLKEMPublicKeyParameters)
        return try {
            val secret = sha256(encapsulated.secret, "ml-kem-aes")
            intBytes(encapsulated.encapsulation.size) +
                encapsulated.encapsulation +
                encryptAesBytes(plaintext, SecretKeySpec(secret, "AES"))
        } finally {
            encapsulated.destroy()
        }
    }

    private fun decryptMlKem(payload: ByteArray, sessionKey: SecretKey): ByteArray {
        val encapsulationSize = readInt(payload, 0)
        require(encapsulationSize > 0 && payload.size > 4 + encapsulationSize + 12)
        val encapsulation = payload.copyOfRange(4, 4 + encapsulationSize)
        val encrypted = payload.copyOfRange(4 + encapsulationSize, payload.size)
        val secret =
            MLKEMExtractor(mlKemKeyPair(sessionKey).private as MLKEMPrivateKeyParameters)
                .extractSecret(encapsulation)
        return decryptAesBytes(
            Base64.getEncoder().encodeToString(encrypted),
            SecretKeySpec(sha256(secret, "ml-kem-aes"), "AES"),
        )
    }

    private fun decryptEnvelope(
        mode: EncryptionMode,
        payload: ByteArray,
        sessionKey: SecretKey,
    ): String = String(decryptEnvelopeBytes(mode, payload, sessionKey), Charsets.UTF_8)

    private fun decryptEnvelopeBytes(
        mode: EncryptionMode,
        payload: ByteArray,
        sessionKey: SecretKey,
    ): ByteArray =
        when (mode) {
            EncryptionMode.AES -> decryptAesBytes(Base64.getEncoder().encodeToString(payload), sessionKey)
            EncryptionMode.RSA_AES -> decryptRsa(payload, sessionKey)
            EncryptionMode.X25519_AES -> decryptX25519(payload, sessionKey)
            EncryptionMode.ML_KEM_AES -> decryptMlKem(payload, sessionKey)
        }

    private fun rsaKeyPair(sessionKey: SecretKey): KeyPair {
        val cacheKey = cacheKey(sessionKey, "rsa")
        return rsaKeys.computeIfAbsent(cacheKey) {
            KeyPairGenerator.getInstance("RSA").apply {
                initialize(2048, HmacRandom(deriveSeed(sessionKey, "rsa")))
            }.generateKeyPair()
        }
    }

    private fun x25519RecipientPrivateKey(sessionKey: SecretKey): ByteArray =
        ByteArray(X25519.SCALAR_SIZE).also {
            X25519.generatePrivateKey(HmacRandom(deriveSeed(sessionKey, "x25519-recipient")), it)
        }

    private fun ephemeralPublicKey(privateKey: ByteArray): ByteArray =
        ByteArray(X25519.POINT_SIZE).also { X25519.generatePublicKey(privateKey, 0, it, 0) }

    private fun mlKemKeyPair(sessionKey: SecretKey): AsymmetricCipherKeyPair {
        val cacheKey = cacheKey(sessionKey, "ml-kem-768")
        return mlKemKeys.computeIfAbsent(cacheKey) {
            MLKEMKeyPairGenerator().apply {
                init(
                    MLKEMKeyGenerationParameters(
                        HmacRandom(deriveSeed(sessionKey, "ml-kem-768")),
                        MLKEMParameters.ml_kem_768,
                    ),
                )
            }.generateKeyPair()
        }
    }

    private fun rsaCipher(mode: Int, key: Key): Cipher =
        Cipher.getInstance("RSA/ECB/OAEPPadding").apply {
            init(
                mode,
                key,
                OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT,
                ),
            )
        }

    private fun aesKey(key: SecretKey): SecretKeySpec = SecretKeySpec(key.encoded, "AES")

    private fun encodeEnvelope(mode: EncryptionMode, payload: ByteArray): String {
        val result = ByteArray(MAGIC.size + 1 + payload.size)
        MAGIC.copyInto(result)
        result[MAGIC.size] = mode.id
        payload.copyInto(result, MAGIC.size + 1)
        return Base64.getEncoder().encodeToString(result)
    }

    private fun decodeEnvelope(data: String): Pair<EncryptionMode, ByteArray>? {
        val decoded = Base64.getDecoder().decode(data)
        if (decoded.size <= MAGIC.size || !decoded.copyOfRange(0, MAGIC.size).contentEquals(MAGIC))
            return null
        val mode =
            EncryptionMode.entries.firstOrNull { it.id == decoded[MAGIC.size] }
                ?: throw IllegalArgumentException("Неизвестный режим шифрования")
        return mode to decoded.copyOfRange(MAGIC.size + 1, decoded.size)
    }

    private fun deriveSeed(key: SecretKey, label: String): ByteArray =
        hmac(key.encoded, label.toByteArray(Charsets.UTF_8))

    private fun sha256(value: ByteArray, label: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(value + label.toByteArray(Charsets.UTF_8))

    private fun hmac(key: ByteArray, value: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(value)
    }

    private fun cacheKey(key: SecretKey, label: String): String =
        Base64.getEncoder().encodeToString(hmac(key.encoded, label.toByteArray(Charsets.UTF_8)))

    private fun intBytes(value: Int): ByteArray =
        ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(value).array()

    private fun readInt(value: ByteArray, offset: Int): Int {
        require(value.size >= offset + 4)
        return ByteBuffer.wrap(value, offset, 4).order(ByteOrder.BIG_ENDIAN).int
    }

    private class HmacRandom(seed: ByteArray) : SecureRandom() {
        private val key = seed.copyOf()
        private var counter = 0L

        override fun setSeed(seed: ByteArray?) = Unit

        override fun nextBytes(bytes: ByteArray) {
            var offset = 0
            while (offset < bytes.size) {
                val mac = Mac.getInstance("HmacSHA256")
                mac.init(SecretKeySpec(key, "HmacSHA256"))
                mac.update(ByteBuffer.allocate(8).putLong(counter++).array())
                val block = mac.doFinal()
                val count = minOf(block.size, bytes.size - offset)
                block.copyInto(bytes, offset, 0, count)
                offset += count
            }
        }
    }

    private val EncryptionMode.id: Byte
        get() =
            when (this) {
                EncryptionMode.AES -> 0
                EncryptionMode.RSA_AES -> 1
                EncryptionMode.X25519_AES -> 2
                EncryptionMode.ML_KEM_AES -> 3
            }

    private val MAGIC = byteArrayOf('T'.code.toByte(), 'P'.code.toByte(), 'C'.code.toByte(), 1)
}
