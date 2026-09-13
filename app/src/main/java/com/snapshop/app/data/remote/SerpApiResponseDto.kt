package com.snapshop.app.data.remote

import com.google.gson.annotations.SerializedName

data class SerpApiResponseDto(
    @SerializedName("shopping_results")
    val shoppingResults: List<SerpApiProductDto>? = null
)

data class SerpApiProductDto(
    @SerializedName("product_id")
    val productId: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("price")
    val price: String? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: String? = null,

    @SerializedName("link")
    val link: String? = null,

    @SerializedName("rating")
    val rating: Double? = null,

    @SerializedName("reviews")
    val reviews: Int? = null
)