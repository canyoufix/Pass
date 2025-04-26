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
import com.canyoufix.crypto.SecurePrefsManager
import com.canyoufix.data.dto.ExportData
import com.canyoufix.data.dto.MetaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.IOException

class ExportManager(
    private val noteRepository: NoteRepository,
    private val cardRepository: CardRepository,
    private val passwordRepository: PasswordRepository,
    private val securePrefsManager: SecurePrefsManager
) {
    @OptIn(ExperimentalSerializationApi::class)
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

            val exportData = ExportData(
                encrypted = encrypted,
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

}