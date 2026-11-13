package com.canyoufix.autofill.service

import android.os.CancellationSignal
import android.service.autofill.*
import com.canyoufix.autofill.auth.AutofillAuthBridge
import com.canyoufix.domain.repository.PasswordRepository
import com.canyoufix.domain.repository.SessionRepository
import com.canyoufix.domain.repository.SessionState
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class MyAutofillService : AutofillService() {
    private val passwords: PasswordRepository by inject()
    private val session: SessionRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback,
    ) {
        val structure = request.fillContexts.lastOrNull()?.structure
        val fields = structure?.let(AutofillResponseFactory::fields)
        if (fields == null) {
            callback.onSuccess(null)
            return
        }
        val job = serviceScope.launch {
            try {
                val response =
                    if (session.state.value == SessionState.Unlocked) {
                        AutofillResponseFactory.unlocked(
                            this@MyAutofillService,
                            fields,
                            passwords.findByDomain(fields.domain),
                        )
                    } else {
                        AutofillAuthBridge.createAuthIntent()?.let {
                            AutofillResponseFactory.locked(this@MyAutofillService, fields, it)
                        }
                    }
                ensureActive()
                if (!cancellationSignal.isCanceled) callback.onSuccess(response)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (!cancellationSignal.isCanceled)
                    callback.onFailure("Не удалось прочитать хранилище")
            }
        }
        cancellationSignal.setOnCancelListener { job.cancel() }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        callback.onFailure("Добавьте запись в приложении TwicePass")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
