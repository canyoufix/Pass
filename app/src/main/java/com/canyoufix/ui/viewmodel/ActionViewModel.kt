package com.canyoufix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActionStatus(val busy: Boolean = false, val message: String? = null)

abstract class ActionViewModel : ViewModel() {
    private val mutableStatus = MutableStateFlow(ActionStatus())
    val status = mutableStatus.asStateFlow()

    fun dismissMessage() {
        mutableStatus.value = mutableStatus.value.copy(message = null)
    }

    protected fun perform(success: String? = null, action: suspend () -> Unit) {
        if (mutableStatus.value.busy) return
        mutableStatus.value = ActionStatus(busy = true)
        viewModelScope.launch {
            try {
                action()
                mutableStatus.value = ActionStatus(message = success)
            } catch (cancelled: CancellationException) {
                mutableStatus.value = ActionStatus()
                throw cancelled
            } catch (failure: Exception) {
                mutableStatus.value = ActionStatus(message = failure.userMessage())
            }
        }
    }
}

internal fun Throwable.userMessage(): String =
    when (this) {
        is IllegalArgumentException,
        is IllegalStateException -> message ?: "Операция не выполнена"
        is IOException ->
            "Не удалось выполнить операцию. Проверьте доступ к файлу или соединение с сервером"
        else -> "Операция не выполнена. Повторите попытку"
    }
