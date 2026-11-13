package com.canyoufix.domain

import com.canyoufix.domain.repository.*
import com.canyoufix.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class TransferVaultTest {
    private class Session(private val accepts: Boolean) : SessionRepository {
        override val state = MutableStateFlow<SessionState>(SessionState.Unlocked)

        override suspend fun initialize() = Unit

        override suspend fun unlock(password: String) = Unit

        override suspend fun setup(credentials: SetupCredentials) = Unit

        override suspend fun verify(password: String) = accepts

        override fun lock() = Unit

        override suspend fun reset() = Unit
    }

    private class Settings(enabled: Boolean) : SettingsRepository {
        override val syncSettings = MutableStateFlow(SyncSettings(isEnabled = enabled))
        override val theme = flowOf(AppTheme.SYSTEM)

        override suspend fun saveSyncSettings(settings: SyncSettings) {
            syncSettings.value = settings
        }

        override suspend fun setTheme(theme: AppTheme) = Unit
    }

    private class Transfer : VaultTransferRepository {
        var exports = 0
        var imports = 0

        override suspend fun export(destination: String, encrypted: Boolean) {
            exports++
        }

        override suspend fun importData(source: String, mode: ImportMode, sourcePassword: String) {
            imports++
        }
    }

    @Test
    fun wrongMasterPasswordNeverOpensExport() = runTest {
        val transfer = Transfer()
        try {
            ExportVault(Session(false), transfer)("document", true, "wrong")
            fail("Expected rejection")
        } catch (_: IllegalArgumentException) {}
        assertEquals(0, transfer.exports)
    }

    @Test
    fun replacingWhileSyncEnabledNeverTouchesStorage() = runTest {
        val transfer = Transfer()
        try {
            ImportVault(Session(true), Settings(true), transfer)(
                "document",
                ImportMode.REPLACE,
                "master",
                "source",
            )
            fail("Expected rejection")
        } catch (_: IllegalArgumentException) {}
        assertEquals(0, transfer.imports)
    }

    @Test
    fun mergingIsAllowedWithSyncAndReplacementIsAllowedOffline() = runTest {
        val transfer = Transfer()
        ImportVault(Session(true), Settings(true), transfer)(
            "document",
            ImportMode.MERGE,
            "master",
            "source",
        )
        ImportVault(Session(true), Settings(false), transfer)(
            "document",
            ImportMode.REPLACE,
            "master",
            "source",
        )
        assertEquals(2, transfer.imports)
    }
}
