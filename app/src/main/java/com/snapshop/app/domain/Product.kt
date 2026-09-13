package com.snapshop.app.domain

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
            if (price.isNull_or_blank()) return 0.0
            val cleaned = price?.replace(Regex("[^0-9.]"), "")
            return cleaned?.toDoubleOrNull() ?: 0.0
        }

    fun safeId(): String {
        return id ?: title?.hashCode()?.toString() ?: System.currentTimeMillis().toString()
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

data class RecognizedProductInfo(
    val category: String = "",
    val productType: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val keywords: List<String> = emptyList()
) {
    fun generateSearchQueries(): List<String> {
        val queries = mutableListOf<String>()

        // 1. brand + productType + model
        val brandModelType = listOf(brand, model, productType).filter { it.isNotBlank() }.joinToString(" ")
        if (brandModelType.isNotBlank()) queries.add(brandModelType)

        // 2. brand + model
        val brandModel = listOf(brand, model).filter { it.isNotBlank() }.joinToString(" ")
        if (brandModel.isNotBlank() && !queries.contains(brandModel)) queries.add(brandModel)

        // 3. brand + productType
        val brandType = listOf(brand, productType).filter { it.isNotBlank() }.joinToString(" ")
        if (brandType.isNotBlank() && !queries.contains(brandType)) queries.add(brandType)

        // 4. productType + color
        val typeColor = listOf(productType, color).filter { it.isNotBlank() }.joinToString(" ")
        if (typeColor.isNotBlank() && !queries.contains(typeColor)) queries.add(typeColor)

        // 5. top keywords
        if (keywords.isNotEmpty()) {
            val kwQuery = keywords.take(3).joinToString(" ")
            if (kwQuery.isNotBlank() && !queries.contains(kwQuery)) queries.add(kwQuery)
        }

        // 6. fallback category / productType
        if (productType.isNotBlank() && !queries.contains(productType)) queries.add(productType)
        if (category.isNotBlank() && !queries.contains(category)) queries.add(category)

        return queries.ifEmpty { listOf("trending products") }
    }
}