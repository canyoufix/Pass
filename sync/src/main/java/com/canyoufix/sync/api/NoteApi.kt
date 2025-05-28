package com.canyoufix.sync.api

import com.canyoufix.sync.dto.NoteDto
import com.canyoufix.sync.dto.PasswordDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NoteApi {

    @GET("notes")
    suspend fun getAllNotes(): List<NoteDto>

    @POST("notes")
    suspend fun uploadNote(@Body dto: NoteDto): Response<Unit>

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body dto: NoteDto): Response<Unit>

    @HTTP(method = "DELETE", path = "notes/{id}", hasBody = true)
    suspend fun deleteNote(@Path("id") id: String, @Body dto: NoteDto): Response<Unit>

    @GET("notes/sync")
    suspend fun getNotesSince(@Query("since") lastSync: Long): List<NoteDto>
}

