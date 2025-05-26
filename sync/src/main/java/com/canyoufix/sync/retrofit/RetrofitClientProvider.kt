package com.canyoufix.sync.retrofit

import android.util.Log
import com.canyoufix.settings.datastore.SyncSettings
import com.canyoufix.settings.datastore.SyncSettingsStore
import kotlinx.coroutines.flow.first

class RetrofitClientProvider(private val settingsStore: SyncSettingsStore) {
    private var retrofitClient: RetrofitClient? = null
    private var lastSettings: SyncSettings? = null

    suspend fun getClient(): RetrofitClient {
        val currentSettings = settingsStore.syncSettingsFlow.first()

        // Если клиента ещё нет или настройки изменились — создаём новый
        if (retrofitClient == null || currentSettings != lastSettings) {
            retrofitClient = RetrofitClient(currentSettings.ip, currentSettings.port)
            lastSettings = currentSettings
            Log.d("RetrofitClientProvider", "RetrofitClient создан с ${currentSettings.ip}:${currentSettings.port}")
        }

        return retrofitClient!!
    }
}
