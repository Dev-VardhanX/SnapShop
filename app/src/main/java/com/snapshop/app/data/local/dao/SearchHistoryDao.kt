package com.snapshop.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.snapshop.app.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<SearchHistoryEntity>>

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteByQuery(query: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun trimToLimit(limit: Int)

    @Transaction
    suspend fun recordSearch(query: String, searchType: String = "TEXT", imageUri: String? = null, category: String? = null, brand: String? = null) {
        deleteByQuery(query)
        insert(
            SearchHistoryEntity(
                query = query,
                timestamp = System.currentTimeMillis(),
                searchType = searchType,
                imageUri = imageUri,
                recognizedCategory = category,
                recognizedBrand = brand
            )
        )
        trimToLimit(50)
    }

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
