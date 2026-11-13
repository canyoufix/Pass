package com.canyoufix.data.files

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.canyoufix.crypto.CryptoEngine
import com.canyoufix.data.mapping.EntryCodec
import com.canyoufix.domain.CoroutineDispatchers
import com.canyoufix.domain.repository.*
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class EncryptedFilesRepositoryImpl(
    private val context: Context,
    private val codec: EntryCodec,
    private val crypto: CryptoEngine,
    private val dispatchers: CoroutineDispatchers,
    private val fileLock: VaultFileLock,
) : EncryptedFilesRepository {
    private val resolver
        get() = context.contentResolver

    private val directory
        get() = File(context.filesDir, "EncryptedFiles").apply { check(exists() || mkdirs()) }

    private val mutex
        get() = fileLock.mutex

    private var staleCacheRemoved = false

    override suspend fun listFiles(): List<VaultFile> =
        withContext(dispatchers.io) {
            mutex.withLock {
                if (!codec.canRead) emptyList()
                else {
                    removeStaleCache()
                    directory
                        .listFiles()
                        .orEmpty()
                        .filter { it.isFile && it.name.endsWith(".enc") }
                        .sortedBy { it.name }
                        .map { VaultFile(it.name, it.length()) }
                }
            }
        }

    override suspend fun encryptFile(source: String): Unit =
        withContext(dispatchers.io) {
            mutex.withLock {
                val cipher = codec.capture()
                val key = cipher.key
                val uri = Uri.parse(source)
                val originalName =
                    resolver
                        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                        ?.use {
                            val column = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (column >= 0 && it.moveToFirst()) it.getString(column) else null
                        } ?: "file"
                val safeName =
                    File(originalName).name.take(100).replace(Regex("""[^\p{L}\p{N} ._-]"""), "_")
                val destination = resolveFile(safeName + "_" + UUID.randomUUID() + ".enc")
                val temporary = File.createTempFile("encrypt-", ".tmp", directory)
                try {
                    resolver.openInputStream(uri)?.use { input ->
                        check(crypto.encryptFile(input, temporary, key)) {
                            "Не удалось зашифровать файл"
                        }
                    } ?: throw IOException("Не удалось открыть файл")
                    currentCoroutineContext().ensureActive()
                    cipher.requireActive()
                    check(!destination.exists() && temporary.renameTo(destination)) {
                        "Не удалось сохранить зашифрованный файл"
                    }
                } finally {
                    temporary.delete()
                }
            }
        }

    override suspend fun decryptFile(name: String, destination: String): Unit =
        withContext(dispatchers.io) {
            mutex.withLock {
                val cipher = codec.capture()
                removeStaleCache()
                val key = cipher.key
                val temporary = File.createTempFile("decrypted-", ".tmp", context.cacheDir)
                try {
                    temporary.outputStream().use {
                        check(crypto.decryptFile(resolveFile(name), it, key)) {
                            "Не удалось расшифровать файл"
                        }
                    }
                    currentCoroutineContext().ensureActive()
                    cipher.requireActive()
                    resolver.openOutputStream(Uri.parse(destination), "wt")?.use { output ->
                        temporary.inputStream().use { it.copyTo(output) }
                    } ?: throw IOException("Не удалось открыть документ для записи")
                } finally {
                    temporary.delete()
                }
            }
        }

    override suspend fun deleteFile(name: String): Unit =
        withContext(dispatchers.io) {
            mutex.withLock {
                codec.capture().requireActive()
                check(resolveFile(name).delete()) { "Не удалось удалить файл" }
            }
        }

    private fun removeStaleCache() {
        if (staleCacheRemoved) return
        context.cacheDir
            .listFiles()
            .orEmpty()
            .filter { it.isFile && it.name.startsWith("decrypted-") }
            .forEach {
                check(it.delete()) { "Не удалось очистить временные файлы" }
            }
        staleCacheRemoved = true
    }

    private fun resolveFile(name: String): File {
        require(name.isNotBlank() && File(name).name == name) { "Некорректное имя файла" }
        val file = File(directory, name).canonicalFile
        require(file.parentFile == directory.canonicalFile) { "Некорректный путь" }
        return file
    }
}
