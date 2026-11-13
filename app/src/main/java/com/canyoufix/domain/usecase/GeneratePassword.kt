package com.canyoufix.domain.usecase

import java.security.SecureRandom

data class PasswordOptions(
    val length: Int = 20,
    val lowercase: Boolean = true,
    val uppercase: Boolean = true,
    val digits: Boolean = true,
    val symbols: Boolean = true,
)

class GeneratePassword(private val random: SecureRandom = SecureRandom()) {
    operator fun invoke(options: PasswordOptions): String {
        require(options.length in 4..128) { "Длина должна быть от 4 до 128" }
        val groups = buildList {
            if (options.lowercase) add("abcdefghijklmnopqrstuvwxyz")
            if (options.uppercase) add("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            if (options.digits) add("0123456789")
            if (options.symbols) add("!@#$%^&*()-_=+[]{}:,.?/")
        }
        require(groups.isNotEmpty()) { "Выберите хотя бы один набор символов" }
        val alphabet = groups.joinToString("")
        val result = groups.map { it[random.nextInt(it.length)] }.toMutableList()
        repeat(options.length - result.size) { result += alphabet[random.nextInt(alphabet.length)] }
        for (i in result.lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val temporary = result[i]
            result[i] = result[j]
            result[j] = temporary
        }
        return result.joinToString("")
    }
}
