package com.canyoufix.settings.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.syncSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_settings")

object SyncSettingsKeys {
    val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
    val SYNC_IP = stringPreferencesKey("sync_ip")
    val SYNC_PORT = stringPreferencesKey("sync_port")
}

data class SyncSettings(
    val isEnabled: Boolean,
    val ip: String,
    val port: String
)

class SyncSettingsStore(private val context: Context) {

    val syncSettingsFlow: Flow<SyncSettings> = context.syncSettingsDataStore.data
        .map { prefs: Preferences ->
            SyncSettings(
                isEnabled = prefs[SyncSettingsKeys.SYNC_ENABLED] ?: false,
                ip = prefs[SyncSettingsKeys.SYNC_IP] ?: "",
                port = prefs[SyncSettingsKeys.SYNC_PORT] ?: ""
            )
        }

    suspend fun saveSettings(syncSettings: SyncSettings) {
        context.syncSettingsDataStore.edit { prefs: MutablePreferences ->
            prefs[SyncSettingsKeys.SYNC_ENABLED] = syncSettings.isEnabled
            prefs[SyncSettingsKeys.SYNC_IP] = syncSettings.ip
            prefs[SyncSettingsKeys.SYNC_PORT] = syncSettings.port
        }
    }

    // Добавляем эту функцию для удобства проверки включена ли синхронизация
    suspend fun isEnabled(): Boolean {
        return syncSettingsFlow.first().isEnabled
    }
}
