package com.snapshop.app.data.remote

import com.google.gson.annotations.SerializedName

data class SerpApiResponseDto(
    @SerializedName("shopping_results")
    val shoppingResults: List<SerpApiProductDto>? = null,

    @SerializedName("inline_shopping_results")
    val inlineShoppingResults: List<SerpApiProductDto>? = null
) {
    val allShoppingResults: List<SerpApiProductDto>
        get() = shoppingResults ?: inlineShoppingResults.orEmpty()
}

data class SerpApiProductDto(
    @SerializedName("product_id")
    val productId: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("price")
    val price: String? = null,

    @SerializedName("extracted_price")
    val extractedPrice: Double? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: String? = null,

    @SerializedName("serpapi_thumbnail")
    val serpapiThumbnail: String? = null,

    @SerializedName("link")
    val link: String? = null,

    @SerializedName("product_link")
    val productLink: String? = null,

    @SerializedName("rating")
    val rating: Double? = null,

    @SerializedName("reviews")
    val reviews: Int? = null,

    @SerializedName("multiple_sources")
    val multipleSources: Boolean? = null
)