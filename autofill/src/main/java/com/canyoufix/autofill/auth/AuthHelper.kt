package com.canyoufix.autofill.auth

import android.content.IntentSender
import com.canyoufix.autofill.service.MyAutofillService

object AuthHelper {
    fun createAuthIntentSender(myAutofillService: MyAutofillService): IntentSender? {
        return AutofillAuthBridge.createAuthIntent()
    }
}