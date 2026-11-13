package com.canyoufix.domain.model

import java.net.IDN
import java.net.URI
import java.util.Locale

/** Exact normalized host matching; an empty/invalid origin never matches an account. */
fun websiteDomain(value: String): String? = runCatching {
    val input = value.trim()
    if (input.isEmpty()) return null
    val uri = URI(if (input.contains("://")) input else "https://" + input)
    if (uri.scheme.lowercase(Locale.ROOT) !in setOf("https", "http") || uri.rawUserInfo != null)
        return null
    val authority = uri.rawAuthority ?: return null
    if (uri.port != -1 && uri.port !in 1..65535) return null
    if (authority.endsWith(':')) return null
    val host =
        uri.host
            ?: run {
                if (authority.count { it == ':' } > 1) return null
                if (
                    authority.contains(':') &&
                        authority.substringAfter(':').toIntOrNull() !in 1..65535
                )
                    return null
                IDN.toASCII(authority.substringBefore(':'), IDN.USE_STD3_ASCII_RULES)
            }
    host.lowercase(Locale.ROOT).removeSuffix(".").removePrefix("www.").takeIf { it.isNotBlank() }
}
    .getOrNull()
