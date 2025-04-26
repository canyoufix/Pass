package com.canyoufix.crypto

import javax.crypto.SecretKey

object SessionKeyHolder {
    var key: SecretKey? = null

    fun clear() {
        key = null
    }
}