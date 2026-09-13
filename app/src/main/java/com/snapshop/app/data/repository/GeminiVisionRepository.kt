package com.snapshop.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.snapshop.app.data.remote.gemini.GeminiContent
import com.snapshop.app.data.remote.gemini.GeminiGenerationConfig
import com.snapshop.app.data.remote.gemini.GeminiInlineData
import com.snapshop.app.data.remote.gemini.GeminiPart
import com.snapshop.app.data.remote.gemini.GeminiProductAnalysisDto
import com.snapshop.app.data.remote.gemini.GeminiRequest
import com.snapshop.app.data.remote.gemini.GeminiResponse
import com.snapshop.app.data.remote.gemini.GeminiRetrofitClient
import com.snapshop.app.domain.Product
import com.snapshop.app.domain.RecognizedProductInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

data class ImageSearchResult(
    val info: RecognizedProductInfo,
    val selectedQuery: String,
    val attemptedQueries: List<String>,
    val products: List<Product>
)

class GeminiVisionRepository(
    private val context: Context,
    private val productRepository: ProductRepository = ProductRepository(context)
) {
    private val gson = Gson()

    suspend fun analyzeAndSearch(imageUri: Uri): ImageSearchResult = withContext(Dispatchers.IO) {
        Log.d("SnapShopGemini", "==================================================")
        Log.d("SnapShopGemini", "VISUAL_SEARCH_START")
        Log.d("SnapShopGemini", "IMAGE_URI: $imageUri")

        val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
        Log.d("SnapShopGemini", "IMAGE_MIME_TYPE: $mimeType")

        val bitmap = loadAndScaleBitmap(context, imageUri)
            ?: throw IllegalArgumentException("Failed to decode image from Uri: $imageUri")

        analyzeAndSearchBitmap(bitmap, imageUri.toString(), mimeType)
    }

    suspend fun analyzeAndSearchBitmap(
        bitmap: Bitmap,
        imageUriString: String? = null,
        mimeType: String = "image/jpeg"
    ): ImageSearchResult = withContext(Dispatchers.IO) {
        val base64Image = bitmapToBase64(bitmap)
        val imageSizeBytes = (base64Image.length * 3) / 4

        Log.d("SnapShopGemini", "IMAGE_SIZE: $imageSizeBytes bytes")

        val promptText = """
            You are an expert e-commerce product recognition AI.
            Analyze the product in this image and return a JSON object with this exact schema:
            {
                "category": "High level category e.g. Electronics, Footwear, Fashion, Home",
                "productType": "Specific product type e.g. Running Shoes, Smartphone, Mechanical Keyboard",
                "brand": "Brand name if visible or recognizable, else empty string",
                "model": "Model name or number if visible or recognizable, else empty string",
                "color": "Primary color of the product, else empty string",
                "keywords": ["top 3 to 5 precise e-commerce search keywords"]
            }
            Rules:
            - Identify the visible product accurately.
            - Do NOT hallucinate brand, model, or specs if not clearly visible or identifiable.
            - If something is unknown or ambiguous, return an empty string.
            - Generate highly relevant e-commerce search terms suitable for Google Shopping in India.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = promptText),
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = "image/jpeg",
                                data = base64Image
                            )
                        )
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(responseMimeType = "application/json")
        )

        val apiKey = GeminiRetrofitClient.apiKey
        if (apiKey.isBlank()) {
            Log.e("SnapShopGemini", "VISUAL_SEARCH_ERROR: GEMINI_API_KEY is not configured in local.properties")
            throw IllegalStateException("GEMINI_API_KEY is not configured in local.properties")
        }

        val model = "gemini-3.6-flash"
        val endpointUrl = "v1beta/models/gemini-3.6-flash:generateContent"
        val requestUrlWithoutKey = "https://generativelanguage.googleapis.com/$endpointUrl"

        Log.d("SnapShopGemini", "GEMINI_REQUEST_URL: $requestUrlWithoutKey")
        Log.d("SnapShopGemini", "GEMINI_MODEL: $model")

        val response: GeminiResponse = try {
            val apiResponse = GeminiRetrofitClient.apiService.analyzeImage(
                url = endpointUrl,
                apiKey = apiKey,
                request = request
            )
            Log.d("SnapShopGemini", "GEMINI_HTTP_STATUS: 200 OK")
            apiResponse
        } catch (e: HttpException) {
            val statusCode = e.code()
            val errorBody = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            Log.e("SnapShopGemini", "GEMINI_HTTP_STATUS: $statusCode")
            if (errorBody != null) {
                Log.e("SnapShopGemini", "GEMINI_RESPONSE_BODY: $errorBody")
            }
            Log.e("SnapShopGemini", "VISUAL_SEARCH_ERROR: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("SnapShopGemini", "VISUAL_SEARCH_ERROR: ${e.message}", e)
            throw e
        }

        val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Gemini returned empty analysis response")

        Log.d("SnapShopGemini", "GEMINI_RESPONSE_BODY: $rawJson")

        val cleanJson = rawJson.replace(Regex("^```(json)?\\s*"), "")
            .replace(Regex("```$"), "")
            .trim()

        val dto = try {
            gson.fromJson(cleanJson, GeminiProductAnalysisDto::class.java)
        } catch (e: Exception) {
            Log.e("SnapShopGemini", "VISUAL_SEARCH_ERROR: Failed to parse Gemini JSON: ${e.message}")
            GeminiProductAnalysisDto()
        }

        val info = RecognizedProductInfo(
            category = dto.category.orEmpty(),
            productType = dto.productType.orEmpty(),
            brand = dto.brand.orEmpty(),
            model = dto.model.orEmpty(),
            color = dto.color.orEmpty(),
            keywords = dto.keywords.orEmpty()
        )

        Log.d("SnapShopGemini", "RECOGNIZED PRODUCT INFO: Brand='${info.brand}', Type='${info.productType}', Model='${info.model}'")

        val candidateQueries = info.generateSearchQueries()
        Log.d("SnapShopGemini", "GENERATED CANDIDATE QUERIES: $candidateQueries")

        var successfulQuery = candidateQueries.first()
        var productsResult = emptyList<Product>()

        for (query in candidateQueries) {
            try {
                Log.d("SnapShopGemini", "EXECUTING SERPAPI QUERY CANDIDATE: '$query'")
                val results = productRepository.searchProducts(query, saveToHistory = false)
                if (results.isNotEmpty()) {
                    successfulQuery = query
                    productsResult = results
                    Log.d("SnapShopGemini", "SUCCESSFUL QUERY CANDIDATE FOUND: '$query' (${results.size} products)")
                    break
                }
            } catch (e: Exception) {
                Log.w("SnapShopGemini", "Query candidate failed: '$query' - ${e.message}")
            }
        }

        try {
            productRepository.recordImageSearchHistory(
                query = successfulQuery,
                imageUri = imageUriString,
                category = info.category.ifBlank { info.productType },
                brand = info.brand
            )
        } catch (e: Exception) {
            Log.e("SnapShopGemini", "Failed to record image search history: ${e.message}")
        }

        ImageSearchResult(
            info = info,
            selectedQuery = successfulQuery,
            attemptedQueries = candidateQueries,
            products = productsResult
        )
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun loadAndScaleBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            var inSampleSize = 1
            val maxDimension = 1024
            while (options.outWidth / inSampleSize > maxDimension || options.outHeight / inSampleSize > maxDimension) {
                inSampleSize *= 2
            }

            val scaleOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            val scaledInputStream = context.contentResolver.openInputStream(uri) ?: return null
            val scaledBitmap = BitmapFactory.decodeStream(scaledInputStream, null, scaleOptions)
            scaledInputStream.close()
            scaledBitmap
        } catch (e: Exception) {
            Log.e("SnapShopGemini", "VISUAL_SEARCH_ERROR loading bitmap: ${e.message}")
            null
        }
    }
}
