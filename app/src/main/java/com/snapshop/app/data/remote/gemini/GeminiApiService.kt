package com.snapshop.app.data.remote.gemini

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface GeminiApiService {

    @POST
    suspend fun analyzeImage(
        @Url url: String = "v1beta/models/gemini-3.6-flash:generateContent",
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
