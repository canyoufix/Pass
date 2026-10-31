package com.canyoufix.sync.api

import com.canyoufix.sync.dto.CardDto
import retrofit2.Response
import retrofit2.http.*

interface CardApi {
    @GET("cards") suspend fun getAllCards(): List<CardDto>

    @GET("cards/{id}") suspend fun getById(@Path("id") id: String): Response<CardDto>

    @POST("cards") suspend fun uploadCard(@Body dto: CardDto): Response<Unit>

    @PUT("cards/{id}")
    suspend fun updateCard(@Path("id") id: String, @Body dto: CardDto): Response<Unit>
}
