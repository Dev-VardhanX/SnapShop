package com.snapshop.app.data.remote.gemini

import com.snapshop.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

object GeminiRetrofitClient {
    val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        val safeMessage = message.replace(Regex("key=[^&\\s]+"), "key=REDACTED")
        if (BuildConfig.DEBUG) {
            android.util.Log.d("SnapShopGeminiHttp", safeMessage)
        }
    }.apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
