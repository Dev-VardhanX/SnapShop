package com.snapshop.app.data.repository

import android.content.Context
import android.util.Log
import com.snapshop.app.data.local.AppDatabase
import com.snapshop.app.data.local.entity.RecentlyViewedEntity
import com.snapshop.app.data.local.entity.SearchHistoryEntity
import com.snapshop.app.data.local.entity.WishlistEntity
import com.snapshop.app.data.remote.RetrofitClient
import com.snapshop.app.domain.Product
import com.snapshop.app.util.MerchantUrlResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(context: Context) {

    private val api = RetrofitClient.serpApiService
    private val db = AppDatabase.getInstance(context)
    private val wishlistDao = db.wishlistDao()
    private val historyDao = db.searchHistoryDao()
    private val recentlyViewedDao = db.recentlyViewedDao()

    suspend fun searchProducts(query: String, saveToHistory: Boolean = true): List<Product> {
        val sanitizedQuery = sanitizeSearchQuery(query)
        if (sanitizedQuery.isEmpty()) return emptyList()

        if (saveToHistory) {
            try {
                historyDao.recordSearch(sanitizedQuery)
            } catch (e: Exception) {
                Log.e("SnapShopSearch", "Failed to record search history: ${e.message}")
            }
        }

        try {
            val response = api.searchProducts(query = sanitizedQuery)
            val results = response.allShoppingResults

            val wishlistIds = try {
                wishlistDao.getWishlistedProductIdsSync().toSet()
            } catch (e: Exception) {
                Log.w("SnapShopSearch", "Failed to fetch wishlist IDs sync: ${e.message}")
                emptySet()
            }

            return results.map { dto ->
                val pid = dto.productId ?: dto.title?.hashCode()?.toString() ?: ""
                val rawUrl = when {
                    !dto.directLink.isNullOrBlank() -> dto.directLink
                    !dto.link.isNullOrBlank() -> dto.link
                    !dto.productLink.isNullOrBlank() -> dto.productLink
                    !dto.productId.isNullOrBlank() -> "https://www.google.com/shopping/product/${dto.productId}?gl=in&hl=en"
                    !dto.title.isNullOrBlank() -> "https://www.google.com/search?q=${java.net.URLEncoder.encode(dto.title, "UTF-8")}&tbm=shop"
                    else -> null
                }

                val resolvedSource = when {
                    !dto.source.isNullOrBlank() -> dto.source
                    dto.multipleSources == true -> "Multiple Stores"
                    else -> "Google Shopping"
                }

                val resolvedImage = dto.thumbnail?.takeIf { it.isNotBlank() }
                    ?: dto.serpapiThumbnail?.takeIf { it.isNotBlank() }

                val resolvedBuyUrl = MerchantUrlResolver.resolveDirectMerchantUrl(
                    rawUrl = rawUrl,
                    source = resolvedSource,
                    title = dto.title
                )

                Product(
                    id = dto.productId,
                    title = dto.title,
                    price = dto.price,
                    source = resolvedSource,
                    imageUrl = resolvedImage,
                    buyUrl = resolvedBuyUrl,
                    rating = dto.rating,
                    reviewsCount = dto.reviews,
                    isWishlisted = wishlistIds.contains(pid)
                )
            }
        } catch (e: Exception) {
            Log.e("SnapShopSearch", "Search error for '$sanitizedQuery': ${e.message}", e)
            throw e
        }
    }

    private fun sanitizeSearchQuery(raw: String): String {
        return raw.replace(Regex("[\"\'(){}\\[\\]+:*#&/\\-_]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun getWishlistProducts(): Flow<List<Product>> {
        return wishlistDao.getAllWishlist().map { list ->
            list.map { it.toProduct() }
        }
    }

    fun isWishlisted(productId: String): Flow<Boolean> {
        return wishlistDao.isWishlisted(productId)
    }

    suspend fun toggleWishlist(product: Product): Boolean {
        val pid = product.safeId()
        val currentlyWishlisted = wishlistDao.isWishlistedSync(pid)
        return if (currentlyWishlisted) {
            wishlistDao.removeFromWishlist(pid)
            false
        } else {
            wishlistDao.addToWishlist(WishlistEntity.fromProduct(product))
            true
        }
    }

    suspend fun removeFromWishlist(productId: String) {
        wishlistDao.removeFromWishlist(productId)
    }

    fun getSearchHistory(): Flow<List<SearchHistoryEntity>> {
        return historyDao.getAllHistory()
    }

    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> {
        return historyDao.getRecentSearches()
    }

    suspend fun deleteSearchHistoryItem(query: String) {
        historyDao.deleteByQuery(query)
    }

    suspend fun clearSearchHistory() {
        historyDao.clearAll()
    }

    suspend fun clearRecentlyViewed() {
        recentlyViewedDao.clearAll()
    }

    suspend fun recordProductView(product: Product) {
        recentlyViewedDao.recordProductView(RecentlyViewedEntity.fromProduct(product))
    }

    fun getRecentlyViewedProducts(): Flow<List<Product>> {
        return recentlyViewedDao.getRecentlyViewed().map { list ->
            val wishlistIds = try {
                wishlistDao.getWishlistedProductIdsSync().toSet()
            } catch (_: Exception) {
                emptySet()
            }
            list.map { entity ->
                entity.toProduct(isWishlisted = wishlistIds.contains(entity.productId))
            }
        }
    }

    suspend fun recordImageSearchHistory(query: String, imageUri: String?, category: String?, brand: String?) {
        historyDao.recordSearch(
            query = query,
            searchType = "IMAGE",
            imageUri = imageUri,
            category = category,
            brand = brand
        )
    }
}
