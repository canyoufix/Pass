package com.canyoufix.ui.platform

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle

internal fun copySecret(context: Context, value: String) {
    val clip = ClipData.newPlainText("TwicePass", value)
    if (Build.VERSION.SDK_INT >= 33)
        clip.description.extras =
            PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
    context.getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
}
