package com.canyoufix.data.sync

internal fun nextLocalTimestamp(now: Long, previous: Long?): Long {
    check(previous != Long.MAX_VALUE) { "Некорректная версия записи" }
    return maxOf(now, (previous ?: -1L) + 1L)
}
