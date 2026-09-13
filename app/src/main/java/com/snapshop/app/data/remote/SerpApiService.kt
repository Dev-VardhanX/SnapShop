package com.snapshop.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface SerpApiService {

    @GET("search")
    suspend fun searchProducts(
        @Query("engine") engine: String = "google_shopping",
        @Query("q") query: String,
        @Query("gl") country: String = "in",
        @Query("hl") language: String = "en",
        @Query("num") numberOfResults: Int = 20
    ): SerpApiResponseDto
}