package com.canyoufix.data.sync

import retrofit2.HttpException
import retrofit2.Response

internal fun Response<Unit>.requireSuccess() {
    if (!isSuccessful) throw HttpException(this)
}

internal fun acceptRemoteChange(
    localTimestamp: Long?,
    remoteTimestamp: Long,
    hasPendingLocalWrite: Boolean,
): Boolean = !hasPendingLocalWrite && (localTimestamp == null || remoteTimestamp >= localTimestamp)
