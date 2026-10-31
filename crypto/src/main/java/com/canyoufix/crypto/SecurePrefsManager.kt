package com.canyoufix.crypto

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Legacy on-disk keys are kept stable; all credential fields commit together. */
@Suppress("DEPRECATION")
class SecurePrefsManager(context: Context) : CredentialStore {
    private val applicationContext = context.applicationContext
    private val preferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            applicationContext,
            "secure_prefs",
            MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun getSalt(): String? = preferences.getString("salt", null)

    override fun getEncryptedTestBlock(): String? = preferences.getString("test_block", null)

    override fun getFakeEncryptedTestBlock(): String? =
        preferences.getString("fake_test_block", null)

    override fun saveCredentials(salt: String, testBlock: String, fakeTestBlock: String) {
        check(
            preferences
                .edit()
                .putString("salt", salt)
                .putString("test_block", testBlock)
                .putString("fake_test_block", fakeTestBlock)
                .commit(),
        ) {
            "Не удалось сохранить мастер-пароль"
        }
    }

    override fun clearAllData() {
        check(preferences.edit().clear().commit()) { "Не удалось сбросить мастер-пароль" }
    }
}
