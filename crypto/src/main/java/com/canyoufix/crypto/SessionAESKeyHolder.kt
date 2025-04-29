package com.canyoufix.crypto

import javax.crypto.SecretKey

object SessionAESKeyHolder {
    private var _key: SecretKey? = null

    val isAuthenticated: Boolean
        get() = _key != null

    val key: SecretKey
        get() = _key ?: throw SecurityException("No active session key")

    fun setKey(secretKey: SecretKey) {
        _key = secretKey
    }

    fun clear() {
        _key = null
    }
}
