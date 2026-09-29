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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
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
        val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
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

        val promptText = """
            You are an expert e-commerce visual shopping AI assistant for SnapShop, an app searching Google Shopping in India.
            Analyze the product in this image and return a JSON object with precise commercial metadata and optimized search queries.

            Search Query Generation Rules:
            1. Real shoppers and Google Shopping search engines use concise, high-intent 3 to 5 word queries.
            2. For Branded Products:
               - Include Brand + exact Model/Line + core Product Type (e.g. "Apple AirPods Pro 2", "Sony WH-1000XM5 Headphones", "Nike Air Force 1 07", "Samsung Galaxy S24 Ultra").
               - Do NOT append redundant generic words if the model already implies the type (e.g. NEVER "Apple iPhone 15 Pro Smartphone").
            3. For Fashion / Footwear / Apparel:
               - MUST include Target Audience / Gender (e.g. "Men", "Women", "Unisex") + Color / Pattern + Fit / Style + Category.
               - E.g. "Men Black Oversized Graphic Hoodie", "Women High Waist Blue Wide Leg Jeans", "Men White Casual Canvas Sneakers".
            4. For Generic / Unbranded / Home Goods:
               - Include Key Material / Visual Trait + Color + Specific Product Type.
               - E.g. "Stainless Steel Insulated Water Bottle 1L", "Minimalist Ceramic Coffee Mug Wooden Handle", "Ergonomic Mesh Office Chair Black".
            5. STRICT DEDUPLICATION:
               - NEVER repeat words in any query (e.g. NEVER "Nike Nike", NEVER "Mouse Wireless Mouse", NEVER "Shoes Running Shoes").
               - NEVER include filler words: "buy", "online", "cheap", "best", "photo", "image", "product", "e-commerce".
            6. SEARCH QUERIES TO RETURN:
               - Query 1 (primary): The absolute best 3-5 word high-conversion Google Shopping search query.
               - Query 2 (secondary): A slightly broader alternative query (omitting ultra-specific color or edition) so if exact item is out of stock, matching items are found.
               - Query 3 (broad): Clean category / style fallback query.

            Return ONLY a valid JSON object matching this schema:
            {
              "productTitle": "Concise human-friendly product title (e.g. 'Apple AirPods Pro 2' or 'Men Black Oversized Hoodie')",
              "category": "Electronics | Fashion | Footwear | Home & Kitchen | Beauty | Sports | Accessories",
              "productType": "Specific product type (e.g. Wireless Earbuds, Running Shoes, Oversized T-Shirt)",
              "brand": "Recognized brand or empty string if unbranded",
              "model": "Recognized model name/number or empty string",
              "color": "Dominant color(s) or empty string",
              "targetAudience": "Men | Women | Unisex | Kids | All or empty string",
              "searchQueries": [
                "Primary 3-5 word Google Shopping query",
                "Secondary alternative query",
                "Broad style fallback query"
              ],
              "keywords": ["3 to 5 key visual attributes/tags, e.g. 'Active Noise Cancelling', 'Oversized Fit'"]
            }
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

        val info: RecognizedProductInfo = try {
            val apiKey = GeminiRetrofitClient.apiKey
            if (apiKey.isBlank()) {
                throw IllegalStateException("GEMINI_API_KEY is not configured in local.properties")
            }

            val response: GeminiResponse = executeGeminiWithFallback(apiKey, request)

            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Photo analysis returned empty response")

            val cleanJson = rawJson.replace(Regex("^```(json)?\\s*"), "")
                .replace(Regex("```$"), "")
                .trim()

            val dto = gson.fromJson(cleanJson, GeminiProductAnalysisDto::class.java)

            RecognizedProductInfo(
                productTitle = dto.productTitle.orEmpty(),
                category = dto.category.orEmpty(),
                productType = dto.productType.orEmpty(),
                brand = dto.brand.orEmpty(),
                model = dto.model.orEmpty(),
                color = dto.color.orEmpty(),
                targetAudience = dto.targetAudience.orEmpty(),
                searchQueries = dto.searchQueries.orEmpty(),
                keywords = dto.keywords.orEmpty()
            )
        } catch (e: Exception) {
            Log.w("SnapShopGemini", "Gemini vision unavailable (${e.message}), activating on-device ML Kit fallback...")
            val labels = labelImageWithMlKit(bitmap)
            Log.d("SnapShopGemini", "ML Kit detected labels: $labels")

            val meaningfulLabels = labels.filter { label ->
                !label.equals("Product", ignoreCase = true) &&
                !label.equals("Material", ignoreCase = true) &&
                !label.equals("Font", ignoreCase = true) &&
                !label.equals("Pattern", ignoreCase = true) &&
                !label.equals("Rectangle", ignoreCase = true) &&
                !label.equals("Black-and-white", ignoreCase = true) &&
                !label.equals("Snapshot", ignoreCase = true)
            }

            val topLabel = meaningfulLabels.firstOrNull() ?: "trending products"
            val secondaryLabel = meaningfulLabels.drop(1).firstOrNull()

            val mlQueries = mutableListOf<String>()
            if (meaningfulLabels.size >= 2) {
                mlQueries.add("${meaningfulLabels[0]} ${meaningfulLabels[1]}")
            }
            mlQueries.add(topLabel)
            if (secondaryLabel != null) mlQueries.add(secondaryLabel)

            RecognizedProductInfo(
                productTitle = topLabel,
                productType = topLabel,
                category = "General",
                searchQueries = mlQueries,
                keywords = meaningfulLabels
            )
        }

        val candidateQueries = info.generateSearchQueries()
        Log.d("SnapShopGemini", "Candidate shopping queries: $candidateQueries")

        var successfulQuery = candidateQueries.firstOrNull() ?: info.productTitle.ifBlank { "trending products" }
        var productsResult = emptyList<Product>()

        for (query in candidateQueries) {
            if (query.isBlank()) continue
            try {
                val results = productRepository.searchProducts(query, saveToHistory = false)
                if (results.size >= 3) {
                    // Solid shopping match with plentiful listings
                    successfulQuery = query
                    productsResult = results
                    break
                } else if (results.isNotEmpty() && productsResult.isEmpty()) {
                    successfulQuery = query
                    productsResult = results
                }
            } catch (e: Exception) {
                Log.w("SnapShopGemini", "Search failed for '$query': ${e.message}")
            }
        }

        // Emergency fallback if still zero results
        if (productsResult.isEmpty()) {
            val fallbackTerm = info.productTitle.ifBlank { info.productType }
            if (fallbackTerm.isNotBlank() && fallbackTerm != successfulQuery) {
                try {
                    val fallbackResults = productRepository.searchProducts(fallbackTerm, saveToHistory = false)
                    if (fallbackResults.isNotEmpty()) {
                        successfulQuery = fallbackTerm
                        productsResult = fallbackResults
                    }
                } catch (_: Exception) {}
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

    private suspend fun executeGeminiWithFallback(
        apiKey: String,
        request: GeminiRequest
    ): GeminiResponse {
        val modelEndpoints = listOf(
            "v1beta/models/gemini-3.8-flash:generateContent",
            "v1beta/models/gemini-3.6-flash:generateContent",
            "v1beta/models/gemini-flash-latest:generateContent"
        )

        var lastException: Exception? = null

        for (endpoint in modelEndpoints) {
            try {
                return GeminiRetrofitClient.apiService.analyzeImage(
                    url = endpoint,
                    apiKey = apiKey,
                    request = request
                )
            } catch (e: HttpException) {
                lastException = e
                val code = e.code()
                val errorBody = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                Log.w("SnapShopGemini", "Gemini HTTP $code on $endpoint: $errorBody")

                if (code == 429) {
                    // Quota exceeded for this specific model - never retry this model, try next candidate immediately
                    continue
                } else if (code == 503) {
                    // High demand spike - quick single retry after 400ms
                    try {
                        kotlinx.coroutines.delay(400L)
                        return GeminiRetrofitClient.apiService.analyzeImage(
                            url = endpoint,
                            apiKey = apiKey,
                            request = request
                        )
                    } catch (e2: Exception) {
                        lastException = e2
                        continue
                    }
                }
            } catch (e: Exception) {
                lastException = e
                Log.w("SnapShopGemini", "Gemini request error on $endpoint: ${e.message}")
            }
        }

        throw lastException ?: IllegalStateException("Failed to communicate with Gemini vision service")
    }

    private suspend fun labelImageWithMlKit(bitmap: Bitmap): List<String> = suspendCancellableCoroutine { cont ->
        try {
            val scaled = scaleBitmapForVision(bitmap, 1024)
            val image = InputImage.fromBitmap(scaled, 0)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val result = labels
                        .filter { it.confidence > 0.45f }
                        .sortedByDescending { it.confidence }
                        .map { it.text }
                    cont.resume(result)
                }
                .addOnFailureListener { e ->
                    Log.w("SnapShopGemini", "ML Kit labeling failed: ${e.message}")
                    cont.resume(emptyList())
                }
        } catch (e: Exception) {
            Log.e("SnapShopGemini", "Error in ML Kit image labeling: ${e.message}")
            cont.resume(emptyList())
        }
    }

    private fun scaleBitmapForVision(bitmap: Bitmap, maxDimension: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (width > height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val scaled = scaleBitmapForVision(bitmap, 1024)
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun loadAndScaleBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            var inSampleSize = 1
            val maxDimension = 1024
            while (options.outWidth / inSampleSize > maxDimension || options.outHeight / inSampleSize > maxDimension) {
                inSampleSize *= 2
            }

            val scaleOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, scaleOptions)
            }
        } catch (e: Exception) {
            Log.e("SnapShopGemini", "Error loading bitmap from Uri: ${e.message}", e)
            null
        }
    }
}
