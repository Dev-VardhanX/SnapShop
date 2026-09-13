package com.snapshop.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.snapshop.app.domain.Product

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey
    val productId: String,
    val title: String?,
    val price: String?,
    val source: String?,
    val imageUrl: String?,
    val buyUrl: String?,
    val rating: Double?,
    val reviewsCount: Int?,
    val viewedAt: Long = System.currentTimeMillis()
) {
    fun toProduct(isWishlisted: Boolean = false): Product = Product(
        id = productId,
        title = title,
        price = price,
        source = source,
        imageUrl = imageUrl,
        buyUrl = buyUrl,
        rating = rating,
        reviewsCount = reviewsCount,
        isWishlisted = isWishlisted
    )

    companion object {
        fun fromProduct(product: Product): RecentlyViewedEntity = RecentlyViewedEntity(
            productId = product.safeId(),
            title = product.title,
            price = product.price,
            source = product.source,
            imageUrl = product.imageUrl,
            buyUrl = product.buyUrl,
            rating = product.rating,
            reviewsCount = product.reviewsCount
        )
    }
}
