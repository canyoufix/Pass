package com.canyoufix.data.serialization

import kotlinx.serialization.json.Json

/** Defaults are explicit: dynamic entity defaults must not change a queued revision on decode. */
internal val storageJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}
