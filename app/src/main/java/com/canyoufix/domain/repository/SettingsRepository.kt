package com.canyoufix.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class EncryptionMode {
    AES,
    RSA_AES,
    X25519_AES,
    ML_KEM_AES,
}

data class SyncSettings(
    val isEnabled: Boolean = false,
    val ip: String = "",
    val port: String = "",
    val lastSyncTime: Long = 0L,
) {
    fun validateEndpoint() {
        require(ip.isNotBlank() && ip.none { it.isWhitespace() || it in "/?#@" }) {
            "Введите адрес сервера без схемы и пути"
        }
        val number = port.toIntOrNull()
        require(number in 1..65535) { "Порт должен быть от 1 до 65535" }
        val address = runCatching { java.net.URI("http://" + ip + ":" + port + "/") }.getOrNull()
        require(
            address?.host != null &&
                address.port == number &&
                address.rawUserInfo == null &&
                address.rawPath == "/",
        ) {
            "Некорректный адрес сервера"
        }
    }
}

interface SettingsRepository {
    val syncSettings: Flow<SyncSettings>
    val theme: Flow<AppTheme>
    val encryptionMode: Flow<EncryptionMode>
        get() = flowOf(EncryptionMode.AES)

    suspend fun saveSyncSettings(settings: SyncSettings)

    suspend fun setTheme(theme: AppTheme)

}

interface SyncCoordinator {
    suspend fun testConnection(settings: SyncSettings)

    suspend fun sync()
}
