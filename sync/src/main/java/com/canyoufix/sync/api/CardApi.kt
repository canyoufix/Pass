package com.canyoufix.sync.api

import com.canyoufix.sync.dto.CardDto
import com.canyoufix.sync.dto.PasswordDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CardApi {

    @GET("cards")
    suspend fun getAllCards(): List<CardDto>

    @POST("cards")
    suspend fun uploadCard(@Body dto: CardDto): Response<Unit>

    @PUT("cards/{id}")
    suspend fun updateCard(@Path("id") id: String, @Body dto: CardDto): Response<Unit>

    @HTTP(method = "DELETE", path = "cards/{id}", hasBody = true)
    suspend fun deleteCard(@Path("id") id: String, @Body dto: CardDto): Response<Unit>
}