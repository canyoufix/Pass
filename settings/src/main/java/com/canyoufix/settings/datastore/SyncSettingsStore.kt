package com.canyoufix.settings.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.*

val Context.syncSettingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "sync_settings")

private object Keys {
    val enabled = booleanPreferencesKey("sync_enabled")
    val ip = stringPreferencesKey("sync_ip")
    val port = stringPreferencesKey("sync_port")
    val lastSync = longPreferencesKey("last_sync_time")
}

data class SyncSettings(
    val isEnabled: Boolean,
    val ip: String,
    val port: String,
    val lastSyncTime: Long = 0L,
)

class SyncSettingsStore(context: Context) {
    private val store = context.applicationContext.syncSettingsDataStore
    val syncSettingsFlow: Flow<SyncSettings> =
        store.data
            .catch {
                if (it is IOException) emit(emptyPreferences()) else throw it
            }
            .map {
                SyncSettings(
                    it[Keys.enabled] ?: false,
                    it[Keys.ip] ?: "",
                    it[Keys.port] ?: "",
                    it[Keys.lastSync] ?: 0L,
                )
            }

    suspend fun saveSettings(settings: SyncSettings) {
        store.edit { prefs ->
            val endpointChanged = prefs[Keys.ip] != settings.ip || prefs[Keys.port] != settings.port
            prefs[Keys.enabled] = settings.isEnabled
            prefs[Keys.ip] = settings.ip
            prefs[Keys.port] = settings.port
            if (endpointChanged) prefs[Keys.lastSync] = 0L
        }
    }

    suspend fun saveLastSyncTime(time: Long, ip: String, port: String) {
        store.edit { prefs ->
            if (prefs[Keys.enabled] == true && prefs[Keys.ip] == ip && prefs[Keys.port] == port)
                prefs[Keys.lastSync] = maxOf(time, prefs[Keys.lastSync] ?: 0L)
        }
    }
}
