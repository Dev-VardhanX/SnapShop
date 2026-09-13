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
        Log.d("SnapShopHttp", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()

        val redactedUrl = newUrl.toString().replace(Regex("api_key=[^&]+"), "api_key=REDACTED")
        Log.d("SnapShopSearch", "SEARCH: API REQUEST: $redactedUrl")

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        val response = chain.proceed(newRequest)
        Log.d("SnapShopSearch", "SEARCH: API RESPONSE RECEIVED")
        Log.d("SnapShopSearch", "SEARCH: HTTP STATUS: ${response.code}")
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
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