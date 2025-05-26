package com.canyoufix.data.json

import android.content.Context
import android.net.Uri
import com.canyoufix.data.repository.NoteRepository
import com.canyoufix.data.repository.CardRepository
import com.canyoufix.data.repository.PasswordRepository
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Build
import com.canyoufix.crypto.CryptoManager
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.crypto.SecurityConfig
import com.canyoufix.data.database.DatabaseManager
import com.canyoufix.data.dto.ExportData
import com.canyoufix.data.dto.MetaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
import java.io.IOException

class DataExportImportManager(
    private val context: Context,
    private val noteRepository: NoteRepository,
    private val cardRepository: CardRepository,
    private val passwordRepository: PasswordRepository,
    private val securePrefsManager: SecurePrefsManager
) {
    private val databaseManager by lazy {
        getKoin().get<DatabaseManager>()
    }


    fun parseExportData(context: Context, uri: Uri): ExportData {
        val jsonString = context.contentResolver.openInputStream(uri)?.use {
            it.readBytes().decodeToString()
        } ?: throw IOException("Не удалось прочитать файл")

        return Json.decodeFromString(jsonString)
    }

    suspend fun exportAll(context: Context, uri: Uri, encrypted: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val notes = if (encrypted) {
                noteRepository.getAllEncryptedNotes.first()
            } else {
                noteRepository.getAllNotes.first()
            }

            val cards = if (encrypted) {
                cardRepository.getAllEncryptedCards.first()
            } else {
                cardRepository.getAllCards.first()
            }

            val passwords = if (encrypted) {
                passwordRepository.getAllEncryptedPasswords.first()
            } else {
                passwordRepository.getAllPasswords.first()
            }

            val salt = if (encrypted) {
                securePrefsManager.getSalt() ?: return@withContext Result.failure(
                    IllegalStateException("Encryption salt not found")
                )
            } else {
                ""
            }

            val testBlock = if (encrypted) {
                securePrefsManager.getEncryptedTestBlock() ?: return@withContext Result.failure(
                    IllegalStateException("Encrypted test block not found")
                )
            } else {
                ""
            }

            val exportData = ExportData(
                encrypted = testBlock,
                salt = salt,
                passwords = passwords,
                cards = cards,
                notes = notes,
                meta = createMetaInfo(),
            )

            val json = Json {
                prettyPrint = true
                prettyPrintIndent = "  "
            }

            val jsonString = json.encodeToString(exportData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: return@withContext Result.failure(IOException("Failed to open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createMetaInfo(): MetaInfo {
        return MetaInfo(
            version = "1.0",
            exportedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            device = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE
        )
    }

    fun checkImport(
        context: Context,
        uri: Uri,
        importType: String // "clean" или "merge"
    ): ImportStatus {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes().decodeToString()
            } ?: return ImportStatus.Error("Файл не читается")

            val exportData = Json.decodeFromString<ExportData>(jsonString)

            when {
                exportData.encrypted.isNullOrBlank() && importType == "clean" ->
                    ImportStatus.NotEncryptedClean
                exportData.encrypted.isNullOrBlank() && importType == "merge" ->
                    ImportStatus.NotEncryptedMerge
                exportData.encrypted?.isNotBlank() == true && importType == "clean" ->
                    ImportStatus.EncryptedCleanNeeded
                exportData.encrypted?.isNotBlank() == true && importType == "merge" ->
                    ImportStatus.EncryptedMergeNeeded
                else -> ImportStatus.Error("Error")
            }
        } catch (e: Exception) {
            ImportStatus.Error(e.message ?: "Error")
        }
    }


    suspend fun importMergeNotEncryptedData(exportData: ExportData) {
        exportData.passwords.forEach { passwordEntity ->
            passwordRepository.insert(passwordEntity)
        }

        exportData.cards.forEach { cardEntity ->
            cardRepository.insert(cardEntity)
        }

        exportData.notes.forEach { noteEntity ->
            noteRepository.insert(noteEntity)
        }
    }

    suspend fun importCleanNotEncryptedData(exportData: ExportData) {
        databaseManager.clearAllData()
        importMergeNotEncryptedData(exportData)
    }

    suspend fun importMergeEncryptedData(exportData: ExportData, importPassword: String) {
        val importSalt = exportData.salt
        if (importSalt.isEmpty()) throw IllegalStateException("Salt missing for encrypted import")

        val importKey = CryptoManager.deriveKeyFromPassword(importPassword, importSalt)

        // Проверка правильности мастер-пароля через расшифровку тестового блока
        val decryptedTestBlock = exportData.encrypted?.let { encryptedTestBlock ->
            CryptoManager.decrypt(encryptedTestBlock, importKey)
        } ?: throw IllegalStateException("Encrypted test block missing")

        if (decryptedTestBlock != SecurityConfig.TEST_BLOCK) {
            throw IllegalArgumentException("Неверный мастер-пароль для импортируемых данных")
        }

        // Дешифруем и сохраняем записи
        exportData.passwords.forEach { encryptedPassword ->
            val decryptedPassword = passwordRepository.decryptPassword(encryptedPassword, importKey)
            passwordRepository.insert(decryptedPassword)
        }

        exportData.cards.forEach { encryptedCard ->
            val decryptedCard = cardRepository.decryptCard(encryptedCard, importKey)
            cardRepository.insert(decryptedCard)
        }

        exportData.notes.forEach { encryptedNote ->
            val decryptedNote = noteRepository.decryptNote(encryptedNote, importKey)
            noteRepository.insert(decryptedNote)
        }
    }

    suspend fun importCleanEncryptedData(exportData: ExportData, importPassword: String) {
        val importSalt = exportData.salt
        if (importSalt.isEmpty()) throw IllegalStateException("Salt missing for encrypted import")

        val importKey = CryptoManager.deriveKeyFromPassword(importPassword, importSalt)

        // Проверка правильности мастер-пароля через расшифровку тестового блока
        val decryptedTestBlock = exportData.encrypted?.let { encryptedTestBlock ->
            CryptoManager.decrypt(encryptedTestBlock, importKey)
        } ?: throw IllegalStateException("Encrypted test block missing")

        if (decryptedTestBlock != SecurityConfig.TEST_BLOCK) {
            throw IllegalArgumentException("Неверный мастер-пароль для импортируемых данных")
        }

        // Очистка БД
        databaseManager.clearAllData()

        // Дешифруем и сохраняем записи
        exportData.passwords.forEach { encryptedPassword ->
            val decryptedPassword = passwordRepository.decryptPassword(encryptedPassword, importKey)
            passwordRepository.insert(decryptedPassword)
        }

        exportData.cards.forEach { encryptedCard ->
            val decryptedCard = cardRepository.decryptCard(encryptedCard, importKey)
            cardRepository.insert(decryptedCard)
        }

        exportData.notes.forEach { encryptedNote ->
            val decryptedNote = noteRepository.decryptNote(encryptedNote, importKey)
            noteRepository.insert(decryptedNote)
        }
    }

}


sealed class ImportStatus {
    object EncryptedCleanNeeded : ImportStatus()    // Нужен пароль + чистка
    object EncryptedMergeNeeded : ImportStatus()    // Нужен пароль (мердж)
    object NotEncryptedClean : ImportStatus()       // Незашифр. + чистка
    object NotEncryptedMerge : ImportStatus()       // Незашифр. мердж
    class Error(val message: String) : ImportStatus()
}