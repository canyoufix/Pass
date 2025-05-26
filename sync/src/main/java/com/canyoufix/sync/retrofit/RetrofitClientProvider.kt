package com.canyoufix.sync.retrofit

import android.util.Log
import com.canyoufix.settings.datastore.SyncSettingsStore
import kotlinx.coroutines.flow.first

class RetrofitClientProvider(private val settingsStore: SyncSettingsStore) {
    private var retrofitClient: RetrofitClient? = null

    // suspend функция для получения RetrofitClient с актуальными настройками
    suspend fun getClient(): RetrofitClient {
        if (retrofitClient == null) {
            val settings = settingsStore.syncSettingsFlow.first()
            retrofitClient = RetrofitClient(settings.ip, settings.port)
            Log.d("RetrofitClientProvider", "RetrofitClient создан с ${settings.ip}:${settings.port}")
        }
        return retrofitClient!!
    }
}