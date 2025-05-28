package com.canyoufix.sync.api

import com.canyoufix.sync.dto.PasswordDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PasswordApi {
    @GET("passwords")
    suspend fun getAllPasswords(): List<PasswordDto>

    @POST("passwords")
    suspend fun uploadPassword(@Body dto: PasswordDto): Response<Unit>

    @PUT("passwords/{id}")
    suspend fun updatePassword(@Path("id") id: String, @Body dto: PasswordDto): Response<Unit>


    @HTTP(method = "DELETE", path = "passwords/{id}", hasBody = true)
    suspend fun deletePassword(@Path("id") id: String, @Body dto: PasswordDto): Response<Unit>
}
