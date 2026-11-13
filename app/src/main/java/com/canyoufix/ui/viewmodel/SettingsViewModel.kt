package com.canyoufix.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.canyoufix.crypto.CryptoModeMigrator
import com.canyoufix.domain.repository.*
import kotlinx.coroutines.flow.*

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val sync: SyncCoordinator,
    private val cryptoModeMigrator: CryptoModeMigrator,
) : ActionViewModel() {
    private val mutableDraft = MutableStateFlow(SyncSettings())
    val draft = mutableDraft.asStateFlow()
    val saved =
        settings.syncSettings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SyncSettings(),
        )
    val theme =
        settings.theme.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppTheme.SYSTEM,
        )
    val encryptionMode =
        settings.encryptionMode.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            EncryptionMode.AES,
        )

    init {
        perform { mutableDraft.value = settings.syncSettings.first() }
    }

    fun edit(change: (SyncSettings) -> SyncSettings) {
        if (!status.value.busy) mutableDraft.value = change(draft.value)
    }

    fun connect() =
        perform("Синхронизация включена") {
            val value =
                draft.value.copy(
                    isEnabled = true,
                    ip = draft.value.ip.trim(),
                    port = draft.value.port.trim(),
                )
            value.validateEndpoint()
            sync.testConnection(value)
            settings.saveSyncSettings(value)
            mutableDraft.value = value
        }

    fun disconnect() =
        perform("Синхронизация отключена") {
            settings.saveSyncSettings(draft.value.copy(isEnabled = false))
            mutableDraft.value = draft.value.copy(isEnabled = false)
        }

    fun syncNow() = perform("Синхронизация завершена") { sync.sync() }

    fun theme(value: AppTheme) = perform { settings.setTheme(value) }

    fun encryptionMode(value: EncryptionMode) =
        perform("Режим шифрования изменён") { cryptoModeMigrator.switchTo(value) }
}
