package com.canyoufix.sync.api

import com.canyoufix.sync.dto.NoteDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NoteApi {

    @GET("notes")
    suspend fun getAllNotes(): List<NoteDto>

    @POST("notes")
    suspend fun uploadNote(@Body dto: NoteDto): Response<Unit>

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body dto: NoteDto): Response<Unit>

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<Unit>
}

