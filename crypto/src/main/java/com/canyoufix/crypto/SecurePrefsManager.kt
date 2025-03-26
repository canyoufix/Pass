package com.canyoufix.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePrefsManager(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSalt(salt: String) {
        prefs.edit().putString("salt", salt).apply()
    }

    fun getSalt(): String? = prefs.getString("salt", null)

    fun saveEncryptedTestBlock(data: String) {
        prefs.edit().putString("test_block", data).apply()
    }

    fun getEncryptedTestBlock(): String? = prefs.getString("test_block", null)
}