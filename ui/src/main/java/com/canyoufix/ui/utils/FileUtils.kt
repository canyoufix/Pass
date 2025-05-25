package com.canyoufix.ui.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    fun addTimestampToFileName(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        return if (dotIndex != -1) {
            val namePart = fileName.substring(0, dotIndex)
            val extensionPart = fileName.substring(dotIndex)
            "${namePart}_$timeStamp$extensionPart"
        } else {
            "${fileName}_$timeStamp"
        }
    }

    fun deleteEncryptedFile(context: Context, fileName: String): Boolean {
        val encryptedDir = File(context.filesDir, "EncryptedFiles")
        val fileToDelete = File(encryptedDir, fileName)
        return if (fileToDelete.exists()) {
            fileToDelete.delete()
        } else {
            false
        }
    }
}
