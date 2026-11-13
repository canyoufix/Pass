package com.canyoufix.domain.repository

import kotlinx.coroutines.flow.StateFlow

sealed interface SessionState {
    data object Loading : SessionState

    data object SetupRequired : SessionState

    data object Locked : SessionState

    data object Unlocked : SessionState
}

data class SetupCredentials(
    val password: String,
    val confirmation: String,
    val decoyPassword: String,
    val decoyConfirmation: String,
) {
    override fun toString(): String = "SetupCredentials(redacted)"

    fun validate() {
        require(password.isNotEmpty()) { "Введите мастер-пароль" }
        require(password == confirmation) { "Пароли не совпадают" }
        require(decoyPassword.isNotEmpty()) { "Введите ложный мастер-пароль" }
        require(decoyPassword == decoyConfirmation) { "Ложные пароли не совпадают" }
        require(password != decoyPassword) { "Настоящий и ложный пароли должны различаться" }
    }
}

interface SessionRepository {
    val state: StateFlow<SessionState>

    suspend fun initialize()

    suspend fun unlock(password: String)

    suspend fun setup(credentials: SetupCredentials)

    suspend fun verify(password: String): Boolean

    fun lock()

    suspend fun reset()
}
