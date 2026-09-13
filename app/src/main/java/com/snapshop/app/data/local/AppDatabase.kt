package com.snapshop.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.snapshop.app.data.local.dao.RecentlyViewedDao
import com.snapshop.app.data.local.dao.SearchHistoryDao
import com.snapshop.app.data.local.dao.WishlistDao
import com.snapshop.app.data.local.entity.RecentlyViewedEntity
import com.snapshop.app.data.local.entity.SearchHistoryEntity
import com.snapshop.app.data.local.entity.WishlistEntity

@Database(
    entities = [
        SearchHistoryEntity::class,
        WishlistEntity::class,
        RecentlyViewedEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun recentlyViewedDao(): RecentlyViewedDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snapshop_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
