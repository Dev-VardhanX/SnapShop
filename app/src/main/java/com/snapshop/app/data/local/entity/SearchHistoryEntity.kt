package com.snapshop.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis(),
    val searchType: String = "TEXT", // "TEXT" or "IMAGE"
    val imageUri: String? = null,
    val recognizedCategory: String? = null,
    val recognizedBrand: String? = null
)
