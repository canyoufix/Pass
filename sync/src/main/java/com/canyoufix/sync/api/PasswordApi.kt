package com.canyoufix.sync.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PasswordApi {
    @GET("passwords")
    //suspend fun getAllPasswords(): List<PasswordDto>

    @POST("passwords")
    //suspend fun uploadPassword(@Body dto: PasswordDto): Response<Unit>

    @DELETE("passwords/{id}")
    suspend fun deletePassword(@Path("id") id: String): Response<Unit>
}
