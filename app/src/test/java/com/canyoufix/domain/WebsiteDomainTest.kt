package com.canyoufix.domain

import com.canyoufix.domain.model.websiteDomain
import org.junit.Assert.*
import org.junit.Test

class WebsiteDomainTest {
    @Test
    fun normalizesHostsWithoutMatchingLookalikeSuffixes() {
        assertEquals("example.com", websiteDomain("https://WWW.Example.com./login"))
        assertEquals("example.com", websiteDomain("example.com/path"))
        assertNotEquals(websiteDomain("example.com"), websiteDomain("example.com.attacker.test"))
        assertNotEquals(websiteDomain("example.com"), websiteDomain("accounts.example.com"))
    }

    @Test
    fun emptyOriginsAndUserInfoAreRejected() {
        for (value in
            listOf(
                "",
                " ",
                "https://",
                "javascript:alert(1)",
                "https://user@example.com",
                "bad domain",
            )) {
            assertNull(value, websiteDomain(value))
        }
    }

    @Test
    fun idnDomainsHaveTheSameCanonicalHostAsPunycode() {
        assertEquals("xn--e1afmkfd.xn--p1ai", websiteDomain("https://пример.рф/login"))
    }
}
