package com.canyoufix.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.canyoufix.domain.repository.*
import kotlinx.coroutines.flow.*

data class AuthForm(
    val password: String = "",
    val confirmation: String = "",
    val decoy: String = "",
    val decoyConfirmation: String = "",
) {
    override fun toString(): String = "AuthForm(redacted)"
}

class AuthViewModel(private val session: SessionRepository, settings: SettingsRepository) :
    ActionViewModel() {
    val sessionState = session.state
    val theme =
        settings.theme.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppTheme.DARK,
        )
    private val mutableForm = MutableStateFlow(AuthForm())
    val form = mutableForm.asStateFlow()
    private val mutableInitializationFailed = MutableStateFlow(false)
    val initializationFailed = mutableInitializationFailed.asStateFlow()

    init {
        initialize()
    }

    fun initialize() = perform {
        mutableInitializationFailed.value = false
        try {
            session.initialize()
        } catch (failure: Exception) {
            mutableInitializationFailed.value = true
            throw failure
        }
    }

    fun edit(change: (AuthForm) -> AuthForm) {
        if (!status.value.busy) mutableForm.value = change(mutableForm.value)
    }

    fun submit() = perform {
        val draft = mutableForm.value
        if (session.state.value == SessionState.SetupRequired) {
            session.setup(
                SetupCredentials(
                    draft.password,
                    draft.confirmation,
                    draft.decoy,
                    draft.decoyConfirmation,
                ),
            )
        } else session.unlock(draft.password)
        mutableForm.value = AuthForm()
    }

    fun reset() = perform {
        session.reset()
        mutableForm.value = AuthForm()
    }

    fun lock() {
        mutableForm.value = AuthForm()
        session.lock()
    }
}
