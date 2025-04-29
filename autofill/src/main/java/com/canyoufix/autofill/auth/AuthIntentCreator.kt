package com.canyoufix.autofill.auth

import android.content.IntentSender

interface AuthIntentCreator {
    fun createAuthIntent(): IntentSender?
}