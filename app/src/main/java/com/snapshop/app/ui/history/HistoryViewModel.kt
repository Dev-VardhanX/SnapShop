package com.snapshop.app.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapshop.app.data.local.entity.SearchHistoryEntity
import com.snapshop.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application.applicationContext)

    val historyItems: StateFlow<List<SearchHistoryEntity>> = repository.getSearchHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteItem(query: String) {
        viewModelScope.launch {
            repository.deleteSearchHistoryItem(query)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }
}
