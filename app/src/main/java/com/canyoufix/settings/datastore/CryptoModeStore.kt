package com.canyoufix.settings.datastore

import android.content.Context
import com.canyoufix.domain.repository.EncryptionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CryptoModeStore(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences("crypto_settings", Context.MODE_PRIVATE)
    private val mutableMode = MutableStateFlow(readMode())

    val mode: StateFlow<EncryptionMode> = mutableMode.asStateFlow()

    val current: EncryptionMode
        get() = mutableMode.value

    fun set(mode: EncryptionMode) {
        check(preferences.edit().putString(KEY_MODE, mode.name).commit()) {
            "Не удалось сохранить режим шифрования"
        }
        mutableMode.value = mode
    }

    private fun readMode(): EncryptionMode =
        preferences.getString(KEY_MODE, null)?.let { value ->
            EncryptionMode.entries.firstOrNull { it.name == value }
        } ?: EncryptionMode.AES

    private companion object {
        const val KEY_MODE = "encryption_mode"
    }
}
