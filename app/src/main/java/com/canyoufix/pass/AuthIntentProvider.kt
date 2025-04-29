package com.canyoufix.pass

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender

object AuthIntentProvider {
    fun createAuthIntent(context: Context): IntentSender {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "OPEN_AUTH_SCREEN"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ).intentSender
    }
}