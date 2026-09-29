package com.snapshop.app.data.remote

import android.util.Log
import com.snapshop.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val SERP_API_BASE_URL = "https://serpapi.com/"

object RetrofitClient {
    val apiKey: String
        get() = BuildConfig.SERPAPI_KEY

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        val safeMessage = message.replace(Regex("api_key=[^&\\s]+"), "api_key=REDACTED")
        if (BuildConfig.DEBUG) {
            Log.d("SnapShopHttp", safeMessage)
        }
    }.apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        chain.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val serpApiService: SerpApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SERP_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SerpApiService::class.java)
    }
}