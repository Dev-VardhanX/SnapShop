package com.snapshop.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapshop.app.data.repository.ProductRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application.applicationContext)

    fun clearSearchHistory(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearSearchHistory()
            onDone()
        }
    }

    fun clearRecentlyViewed(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearRecentlyViewed()
            onDone()
        }
    }
}
