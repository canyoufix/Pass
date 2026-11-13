package com.canyoufix.domain.repository

/** Android document URI strings are opaque to domain and presentation. */
data class VaultFile(val name: String, val size: Long)

enum class ImportMode {
    MERGE,
    REPLACE,
}

interface EncryptedFilesRepository {
    suspend fun listFiles(): List<VaultFile>

    suspend fun encryptFile(source: String)

    suspend fun decryptFile(name: String, destination: String)

    suspend fun deleteFile(name: String)
}

interface VaultTransferRepository {
    suspend fun export(destination: String, encrypted: Boolean)

    suspend fun importData(source: String, mode: ImportMode, sourcePassword: String)
}
