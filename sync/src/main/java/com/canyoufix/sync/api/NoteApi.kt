package com.canyoufix.sync.api

import com.canyoufix.sync.dto.NoteDto
import retrofit2.Response
import retrofit2.http.*

interface NoteApi {
    @GET("notes") suspend fun getAllNotes(): List<NoteDto>

    @GET("notes/{id}") suspend fun getById(@Path("id") id: String): Response<NoteDto>

    @POST("notes") suspend fun uploadNote(@Body dto: NoteDto): Response<Unit>

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body dto: NoteDto): Response<Unit>
}
