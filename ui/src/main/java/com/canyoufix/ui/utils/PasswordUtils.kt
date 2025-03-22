package com.canyoufix.ui.utils

import java.security.SecureRandom

fun generatePassword(
    length: Int,
    useLowerCase: Boolean,
    useUpperCase: Boolean,
    useDigits: Boolean,
    useSymbols: Boolean
): String {
    val lowerCaseChars = "abcdefghijklmnopqrstuvwxyz"
    val upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val digits = "0123456789"
    val symbols = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/\\"

    var characters = ""
    if (useLowerCase) characters += lowerCaseChars
    if (useUpperCase) characters += upperCaseChars
    if (useDigits) characters += digits
    if (useSymbols) characters += symbols

    if (characters.isEmpty()) throw IllegalArgumentException("At least one character type must be selected")

    val random = SecureRandom()
    return (1..length)
        .map { characters[random.nextInt(characters.length)] }
        .joinToString("")
}