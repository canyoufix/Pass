package com.canyoufix.settings.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.canyoufix.domain.repository.AppTheme
import com.canyoufix.domain.repository.SettingsRepository
import com.canyoufix.domain.repository.SyncSettings as DomainSyncSettings
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DefaultSettingsRepository(
    private val context: Context,
    private val store: SyncSettingsStore,
    private val cryptoModeStore: CryptoModeStore,
) : SettingsRepository {
    private val themeKey = stringPreferencesKey("app_theme")
    override val syncSettings =
        store.syncSettingsFlow.map {
            DomainSyncSettings(it.isEnabled, it.ip, it.port, it.lastSyncTime)
        }
    override val theme =
        context.syncSettingsDataStore.data
            .catch {
                if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
                else throw it
            }
            .map { prefs ->
                AppTheme.entries.firstOrNull { it.name == prefs[themeKey] } ?: AppTheme.DARK
            }
    override val encryptionMode = cryptoModeStore.mode

    override suspend fun setTheme(theme: AppTheme) {
        context.syncSettingsDataStore.edit { it[themeKey] = theme.name }
    }

    override suspend fun saveSyncSettings(settings: DomainSyncSettings) {
        if (settings.isEnabled) settings.validateEndpoint()
        store.saveSettings(
            SyncSettings(
                settings.isEnabled,
                settings.ip.trim(),
                settings.port,
                settings.lastSyncTime,
            ),
        )
    }
}
