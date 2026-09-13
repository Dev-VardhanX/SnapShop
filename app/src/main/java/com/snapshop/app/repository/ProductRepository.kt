package com.snapshop.app.data.repository

import android.content.Context
import android.util.Log
import com.snapshop.app.data.local.AppDatabase
import com.snapshop.app.data.local.entity.RecentlyViewedEntity
import com.snapshop.app.data.local.entity.SearchHistoryEntity
import com.snapshop.app.data.local.entity.WishlistEntity
import com.snapshop.app.data.remote.RetrofitClient
import com.snapshop.app.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProductRepository(context: Context) {

    private val api = RetrofitClient.serpApiService
    private val db = AppDatabase.getInstance(context)
    private val wishlistDao = db.wishlistDao()
    private val historyDao = db.searchHistoryDao()
    private val recentlyViewedDao = db.recentlyViewedDao()

    suspend fun searchProducts(query: String, saveToHistory: Boolean = true): List<Product> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return emptyList()

        Log.d("SnapShopSearch", "==================================================")
        Log.d("SnapShopSearch", "SEARCH: START")
        Log.d("SnapShopSearch", "SEARCH: QUERY: $trimmedQuery")

        if (saveToHistory) {
            try {
                historyDao.recordSearch(trimmedQuery)
            } catch (e: Exception) {
                Log.e("SnapShopSearch", "Failed to record search history: ${e.message}")
            }
        }

        try {
            val response = api.searchProducts(query = trimmedQuery)
            val results = response.shoppingResults.orEmpty()
            Log.d("SnapShopSearch", "SEARCH: RAW RESULT COUNT: ${results.size}")

            val wishlistIds = try {
                wishlistDao.getWishlistedProductIdsSync().toSet()
            } catch (e: Exception) {
                Log.w("SnapShopSearch", "Failed to fetch wishlist IDs sync: ${e.message}")
                emptySet()
            }

            val mappedProducts = results.map { dto ->
                val pid = dto.productId ?: dto.title?.hashCode()?.toString() ?: ""
                Product(
                    id = dto.productId,
                    title = dto.title,
                    price = dto.price,
                    source = dto.source,
                    imageUrl = dto.thumbnail,
                    buyUrl = dto.link,
                    rating = dto.rating,
                    reviewsCount = dto.reviews,
                    isWishlisted = wishlistIds.contains(pid)
                )
            }

            Log.d("SnapShopSearch", "SEARCH: MAPPED PRODUCT COUNT: ${mappedProducts.size}")
            return mappedProducts
        } catch (e: Exception) {
            Log.e("SnapShopSearch", "SEARCH ERROR: ${e.message}", e)
            throw e
        }
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
        if (currentlyWishlisted) {
            wishlistDao.removeFromWishlist(pid)
            return false
        } else {
            wishlistDao.addToWishlist(WishlistEntity.fromProduct(product))
            return true
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

    suspend fun recordProductView(product: Product) {
        recentlyViewedDao.recordProductView(RecentlyViewedEntity.fromProduct(product))
    }

    fun getRecentlyViewedProducts(): Flow<List<Product>> {
        return recentlyViewedDao.getRecentlyViewed().map { list ->
            val wishlistIds = try {
                wishlistDao.getWishlistedProductIdsSync().toSet()
            } catch (e: Exception) {
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