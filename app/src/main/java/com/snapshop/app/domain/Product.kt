package com.snapshop.app.domain

data class Product(
    val id: String?,
    val title: String?,
    val price: String?,
    val source: String?,
    val imageUrl: String?,
    val buyUrl: String?,
    val rating: Double?,
    val reviewsCount: Int?,
    val isWishlisted: Boolean = false
) {
    val numericPrice: Double
        get() {
            if (price.isNullOrBlank()) return 0.0
            return try {
                val sanitized = price.replace(",", "").replace(Regex("[^0-9.]"), "")
                val dotCount = sanitized.count { it == '.' }
                val cleanStr = if (dotCount > 1) {
                    val parts = sanitized.split(".")
                    parts[0] + "." + parts.drop(1).joinToString("")
                } else {
                    sanitized
                }
                cleanStr.toDoubleOrNull() ?: 0.0
            } catch (_: Exception) {
                0.0
            }
        }

    fun safeId(): String {
        return id ?: title?.hashCode()?.toString() ?: System.currentTimeMillis().toString()
    }
}

data class RecognizedProductInfo(
    val productTitle: String = "",
    val category: String = "",
    val productType: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val targetAudience: String = "",
    val searchQueries: List<String> = emptyList(),
    val keywords: List<String> = emptyList()
) {
    fun generateSearchQueries(): List<String> {
        val queries = mutableListOf<String>()

        // 1. Prioritize clean queries provided directly by Gemini AI
        for (rawQuery in searchQueries) {
            val sanitized = sanitizeQuery(rawQuery)
            if (sanitized.isNotBlank() && !containsSimilarQuery(queries, sanitized)) {
                queries.add(sanitized)
            }
        }

        // 2. High-precision Algorithmic Query: [Audience] + [Brand] + [Model] + [ProductType] + [Color]
        val algorithmicPrimary = buildAlgorithmicPrimaryQuery()
        if (algorithmicPrimary.isNotBlank() && !containsSimilarQuery(queries, algorithmicPrimary)) {
            queries.add(algorithmicPrimary)
        }

        // 3. Fallback / Broader Query
        val algorithmicFallback = buildAlgorithmicFallbackQuery()
        if (algorithmicFallback.isNotBlank() && !containsSimilarQuery(queries, algorithmicFallback)) {
            queries.add(algorithmicFallback)
        }

        // 4. Clean display title fallback
        val titleQuery = sanitizeQuery(productTitle)
        if (titleQuery.isNotBlank() && !containsSimilarQuery(queries, titleQuery)) {
            queries.add(titleQuery)
        }

        // 5. Default safe fallback
        val defaultFallback = sanitizeQuery(productType.ifBlank { "trending products" })
        if (defaultFallback.isNotBlank() && !containsSimilarQuery(queries, defaultFallback)) {
            queries.add(defaultFallback)
        }

        return queries.ifEmpty { listOf("trending products") }.take(4)
    }

    private fun buildAlgorithmicPrimaryQuery(): String {
        val parts = mutableListOf<String>()

        // Target audience if apparel / fashion (e.g. Men, Women)
        if (targetAudience.isNotBlank() && isFashionOrWearable()) {
            parts.add(targetAudience)
        }

        // Brand
        if (brand.isNotBlank()) {
            parts.add(brand)
        }

        // Model (ensure brand isn't duplicated inside model)
        if (model.isNotBlank()) {
            val cleanModel = if (brand.isNotBlank() && model.startsWith(brand, ignoreCase = true)) {
                model.substring(brand.length).trim()
            } else {
                model
            }
            if (cleanModel.isNotBlank()) {
                parts.add(cleanModel)
            }
        }

        // Product Type (only if not already part of model)
        if (productType.isNotBlank()) {
            val alreadyInModel = model.contains(productType, ignoreCase = true)
            if (!alreadyInModel) {
                parts.add(productType)
            }
        }

        // If no brand/model, include color and top visual feature
        if (brand.isBlank() && model.isBlank()) {
            if (color.isNotBlank()) parts.add(color)
            val firstKw = keywords.firstOrNull {
                !it.equals(productType, ignoreCase = true) && !it.equals(category, ignoreCase = true)
            }
            if (firstKw != null) parts.add(firstKw)
        }

        return sanitizeQuery(parts.joinToString(" "))
    }

    private fun buildAlgorithmicFallbackQuery(): String {
        val parts = mutableListOf<String>()

        if (targetAudience.isNotBlank() && isFashionOrWearable()) {
            parts.add(targetAudience)
        }

        if (brand.isNotBlank()) {
            parts.add(brand)
            if (productType.isNotBlank() && !brand.contains(productType, ignoreCase = true)) {
                parts.add(productType)
            }
        } else if (productType.isNotBlank()) {
            if (color.isNotBlank()) parts.add(color)
            parts.add(productType)
        } else if (category.isNotBlank() && !category.equals("Electronics", ignoreCase = true)) {
            parts.add(category)
        }

        return sanitizeQuery(parts.joinToString(" "))
    }

    private fun isFashionOrWearable(): Boolean {
        return category.contains("Fashion", ignoreCase = true) ||
            category.contains("Footwear", ignoreCase = true) ||
            category.contains("Clothing", ignoreCase = true) ||
            category.contains("Apparel", ignoreCase = true)
    }

    private fun sanitizeQuery(query: String): String {
        val cleaned = query.replace(Regex("[\"\'(){}\\[\\]+:*#&/\\-_]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val words = cleaned.split(" ")
        val uniqueWords = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        for (w in words) {
            val lower = w.lowercase()
            if (lower !in seen && lower.isNotBlank()) {
                seen.add(lower)
                uniqueWords.add(w)
            }
        }
        return uniqueWords.joinToString(" ").trim()
    }

    private fun containsSimilarQuery(list: List<String>, candidate: String): Boolean {
        return list.any { it.equals(candidate, ignoreCase = true) }
    }
}