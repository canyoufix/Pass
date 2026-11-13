package com.canyoufix.data.sync

import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

/** The v1 server has POST/PUT but no idempotency keys or conditional revisions. */
internal suspend fun <T> upsertRemote(
    local: T,
    knownRemote: T?,
    timestamp: (T) -> Long,
    create: suspend (T) -> Response<Unit>,
    update: suspend (T) -> Response<Unit>,
    fetch: suspend () -> Response<T>,
): T {
    if (knownRemote != null && timestamp(knownRemote) >= timestamp(local)) return knownRemote
    val response = if (knownRemote == null) create(local) else update(local)
    if (response.isSuccessful) return local
    val mayBeRace =
        (knownRemote == null && response.code() in setOf(400, 409)) ||
            (knownRemote != null && response.code() == 404)
    if (!mayBeRace) throw HttpException(response)
    val fresh = fetch()
    if (fresh.isSuccessful) {
        val existing = fresh.body() ?: throw IOException("Пустой ответ сервера")
        if (timestamp(existing) >= timestamp(local)) return existing
        update(local).requireSuccess()
        return local
    }
    if (fresh.code() == 404) {
        create(local).requireSuccess()
        return local
    }
    throw HttpException(fresh)
}
