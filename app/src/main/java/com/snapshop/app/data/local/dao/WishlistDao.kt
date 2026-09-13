package com.snapshop.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snapshop.app.data.local.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Query("SELECT * FROM wishlist ORDER BY addedAt DESC")
    fun getAllWishlist(): Flow<List<WishlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE productId = :productId)")
    fun isWishlisted(productId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE productId = :productId)")
    suspend fun isWishlistedSync(productId: String): Boolean

    @Query("SELECT productId FROM wishlist")
    fun getWishlistedProductIds(): Flow<List<String>>

    @Query("SELECT productId FROM wishlist")
    suspend fun getWishlistedProductIdsSync(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(entity: WishlistEntity)

    @Query("DELETE FROM wishlist WHERE productId = :productId")
    suspend fun removeFromWishlist(productId: String)

    @Query("DELETE FROM wishlist")
    suspend fun clearAll()
}
