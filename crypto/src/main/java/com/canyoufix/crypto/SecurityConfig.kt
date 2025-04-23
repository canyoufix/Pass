package com.canyoufix.crypto

object SecurityConfig {
    const val SALT_LENGTH = 16
    const val PBKDF2_ITERATIONS = 100_000
    const val KEY_SIZE = 256
    const val TEST_BLOCK = "VALID"
    const val FAKE_TEST_BLOCK = "VALID"
}