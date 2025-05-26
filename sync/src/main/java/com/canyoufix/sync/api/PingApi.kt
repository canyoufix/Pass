package com.canyoufix.sync.api

import retrofit2.Response
import retrofit2.http.GET

interface PingApi {
    @GET("ping")
    suspend fun ping(): Response<Unit>
}