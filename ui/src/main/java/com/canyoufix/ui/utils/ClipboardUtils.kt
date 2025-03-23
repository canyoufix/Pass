package com.canyoufix.ui.utils

import android.content.ClipboardManager
import android.content.Context
import android.content.ClipData

object ClipboardUtils {

    // Функция для копирования текста в буфер обмена
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clip)
    }
}