package com.cineflow.app.data.api

import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    const val BASE_URL = "https://ngintipya2.cineflow.my.id/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createOkHttpClient(): OkHttpClient {
        val userAgent = "CineFlow/0.2.7 (com.cineflow.app; Android ${Build.VERSION.RELEASE ?: "unknown"}; SDK ${Build.VERSION.SDK_INT})"

        return OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                var request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", userAgent)
                    .addHeader("X-Requested-With", "com.cineflow.app")

                // Add auth token if available
                val token = SessionManager.getToken()
                if (!token.isNullOrEmpty()) {
                    val prefs = SessionClient.appContext?.getSharedPreferences("cineflow_session", 0)
                    val tokenType = prefs?.getString("token_type", "Bearer") ?: "Bearer"
                    request = request.addHeader("Authorization", "$tokenType $token")
                }

                chain.proceed(request.build())
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
