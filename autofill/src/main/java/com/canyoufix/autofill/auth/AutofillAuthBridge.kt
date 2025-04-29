package com.canyoufix.autofill.auth

import android.content.IntentSender

object AutofillAuthBridge {
    private var creator: AuthIntentCreator? = null

    fun initialize(creator: AuthIntentCreator) {
        AutofillAuthBridge.creator = creator
    }

    internal fun createAuthIntent(): IntentSender? {
        return creator?.createAuthIntent()
    }
}