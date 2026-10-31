package com.canyoufix.pass

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build

object AuthIntentProvider {
    fun createAuthIntent(context: Context): IntentSender {
        val intent = Intent(context, AutofillAuthActivity::class.java)
        val mutable = if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
        // Autofill must be able to add EXTRA_ASSIST_STRUCTURE to this explicit PendingIntent.
        return PendingIntent.getActivity(
                context,
                37,
                intent,
                mutable or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            .intentSender
    }
}
