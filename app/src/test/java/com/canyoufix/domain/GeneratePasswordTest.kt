package com.canyoufix.domain

import com.canyoufix.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class GeneratePasswordTest {
    private val generate = GeneratePassword()

    @Test
    fun everySelectedCategoryIsPresentEvenAtMinimumLength() {
        repeat(100) {
            val result = generate(PasswordOptions(length = 4))
            assertEquals(4, result.length)
            assertTrue(result.any { it in 'a'..'z' })
            assertTrue(result.any { it in 'A'..'Z' })
            assertTrue(result.any { it in '0'..'9' })
            assertTrue(result.any { !it.isLetterOrDigit() })
        }
    }

    @Test
    fun disabledCategoriesNeverAppear() {
        val result = generate(PasswordOptions(128, false, false, true, false))
        assertEquals(128, result.length)
        assertTrue(result.all { it in '0'..'9' })
    }

    @Test
    fun impossibleOptionsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { generate(PasswordOptions(length = 3)) }
        assertThrows(IllegalArgumentException::class.java) {
            generate(PasswordOptions(length = 129))
        }
        assertThrows(IllegalArgumentException::class.java) {
            generate(PasswordOptions(20, false, false, false, false))
        }
    }
}
