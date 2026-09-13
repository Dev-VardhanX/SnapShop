package com.snapshop.app.data.remote

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("product_id")
    val id: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("price")
    val price: String? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("thumbnail")
    val imageUrl: String? = null,

    @SerializedName("link")
    val buyUrl: String? = null,

    @SerializedName("rating")
    val rating: Double? = null,

    @SerializedName("reviews")
    val reviewsCount: Int? = null
)