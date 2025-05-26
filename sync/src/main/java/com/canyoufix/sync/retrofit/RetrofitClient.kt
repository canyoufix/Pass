package com.canyoufix.sync.retrofit

import com.canyoufix.sync.api.CardApi
import com.canyoufix.sync.api.NoteApi
import com.canyoufix.sync.api.PasswordApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(private val ip: String, private val port: String) {
    private val retrofit: Retrofit by lazy {
        val baseUrl = "http://$ip:$port/"

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val passwordApi: PasswordApi by lazy { retrofit.create(PasswordApi::class.java) }
    val noteApi: NoteApi by lazy { retrofit.create(NoteApi::class.java) }
    val cardApi: CardApi by lazy { retrofit.create(CardApi::class.java) }
}