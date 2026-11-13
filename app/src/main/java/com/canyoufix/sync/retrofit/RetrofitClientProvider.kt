package com.canyoufix.sync.retrofit

import com.canyoufix.settings.datastore.SyncSettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RetrofitClientProvider(private val settingsStore: SyncSettingsStore) {
    private val mutex = Mutex()
    private var cached: Pair<Pair<String, String>, RetrofitClient>? = null

    suspend fun getClient(): RetrofitClient {
        val settings = settingsStore.syncSettingsFlow.first()
        val endpoint = settings.ip to settings.port
        return mutex.withLock {
            cached?.takeIf { it.first == endpoint }?.second
                ?: RetrofitClient(settings.ip, settings.port).also { cached = endpoint to it }
        }
    }
}
