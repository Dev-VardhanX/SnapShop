package com.snapshop.app.data.remote.gemini

import com.google.gson.annotations.SerializedName

// --- Gemini REST API Request DTOs ---

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<GeminiContent>,
    @SerializedName("generationConfig")
    val generationConfig: GeminiGenerationConfig? = GeminiGenerationConfig()
)

data class GeminiContent(
    @SerializedName("parts")
    val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("inline_data")
    val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    @SerializedName("mime_type")
    val mimeType: String,
    @SerializedName("data")
    val data: String // Base64 encoded string
)

data class GeminiGenerationConfig(
    @SerializedName("response_mime_type")
    val responseMimeType: String = "application/json"
)

// --- Gemini REST API Response DTOs ---

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    @SerializedName("content")
    val content: GeminiCandidateContent? = null
)

data class GeminiCandidateContent(
    @SerializedName("parts")
    val parts: List<GeminiResponsePart>? = null
)

data class GeminiResponsePart(
    @SerializedName("text")
    val text: String? = null
)

// --- Structured Product Result from Gemini ---

data class GeminiProductAnalysisDto(
    @SerializedName("productTitle")
    val productTitle: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("productType")
    val productType: String? = null,
    @SerializedName("brand")
    val brand: String? = null,
    @SerializedName("model")
    val model: String? = null,
    @SerializedName("color")
    val color: String? = null,
    @SerializedName("targetAudience")
    val targetAudience: String? = null,
    @SerializedName("searchQueries")
    val searchQueries: List<String>? = null,
    @SerializedName("keywords")
    val keywords: List<String>? = null
)

