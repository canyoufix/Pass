package com.canyoufix.sync.api

import com.canyoufix.sync.dto.PasswordDto
import retrofit2.Response
import retrofit2.http.*

interface PasswordApi {
    @GET("passwords") suspend fun getAllPasswords(): List<PasswordDto>

    @GET("passwords/{id}") suspend fun getById(@Path("id") id: String): Response<PasswordDto>

    @POST("passwords") suspend fun uploadPassword(@Body dto: PasswordDto): Response<Unit>

    @PUT("passwords/{id}")
    suspend fun updatePassword(@Path("id") id: String, @Body dto: PasswordDto): Response<Unit>
}
