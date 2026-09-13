package com.snapshop.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.snapshop.app.data.local.entity.RecentlyViewedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyViewedDao {

    @Query("SELECT * FROM recently_viewed ORDER BY viewedAt DESC LIMIT 20")
    fun getRecentlyViewed(): Flow<List<RecentlyViewedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecentlyViewedEntity)

    @Transaction
    suspend fun recordProductView(entity: RecentlyViewedEntity) {
        val updated = entity.copy(viewedAt = System.currentTimeMillis())
        insert(updated)
        trimToLimit(20)
    }

    @Query("DELETE FROM recently_viewed WHERE productId NOT IN (SELECT productId FROM recently_viewed ORDER BY viewedAt DESC LIMIT :limit)")
    suspend fun trimToLimit(limit: Int)

    @Query("DELETE FROM recently_viewed")
    suspend fun clearAll()
}
