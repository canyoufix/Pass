package com.canyoufix.domain.usecase

import com.canyoufix.domain.repository.*
import kotlinx.coroutines.flow.first

class ExportVault(
    private val session: SessionRepository,
    private val transfer: VaultTransferRepository,
) {
    suspend operator fun invoke(destination: String, encrypted: Boolean, masterPassword: String) {
        require(session.verify(masterPassword)) { "Неверный мастер-пароль текущего хранилища" }
        transfer.export(destination, encrypted)
    }
}

class ImportVault(
    private val session: SessionRepository,
    private val settings: SettingsRepository,
    private val transfer: VaultTransferRepository,
) {
    suspend operator fun invoke(
        source: String,
        mode: ImportMode,
        masterPassword: String,
        sourcePassword: String,
    ) {
        require(session.verify(masterPassword)) { "Неверный мастер-пароль текущего хранилища" }
        require(mode != ImportMode.REPLACE || !settings.syncSettings.first().isEnabled) {
            "Для замены локальных записей сначала отключите синхронизацию"
        }
        transfer.importData(source, mode, sourcePassword)
    }
}
