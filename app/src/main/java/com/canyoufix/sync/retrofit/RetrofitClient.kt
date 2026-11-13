package com.canyoufix.sync.retrofit

import com.canyoufix.sync.api.*
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(ip: String, port: String) {
    private val retrofit =
        Retrofit.Builder()
            .baseUrl("http://" + ip + ":" + port + "/")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(45, TimeUnit.SECONDS)
                    .build(),
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    val passwordApi: PasswordApi = retrofit.create(PasswordApi::class.java)
    val noteApi: NoteApi = retrofit.create(NoteApi::class.java)
    val cardApi: CardApi = retrofit.create(CardApi::class.java)
    val pingApi: PingApi = retrofit.create(PingApi::class.java)
}
