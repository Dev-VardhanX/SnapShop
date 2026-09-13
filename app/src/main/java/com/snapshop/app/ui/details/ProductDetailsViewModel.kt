package com.snapshop.app.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapshop.app.data.repository.ProductRepository
import com.snapshop.app.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailsUiState(
    val product: Product? = null,
    val isWishlisted: Boolean = false,
    val relatedProducts: List<Product> = emptyList(),
    val isLoadingRelated: Boolean = false
)

class ProductDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    fun setProduct(product: Product) {
        _uiState.value = _uiState.value.copy(
            product = product,
            isWishlisted = product.isWishlisted
        )

        viewModelScope.launch {
            repository.recordProductView(product)

            repository.isWishlisted(product.safeId()).collect { isSaved ->
                _uiState.value = _uiState.value.copy(isWishlisted = isSaved)
            }
        }

        loadRelatedProducts(product)
    }

    fun toggleWishlist() {
        val currentProduct = _uiState.value.product ?: return
        viewModelScope.launch {
            val nowWishlisted = repository.toggleWishlist(currentProduct)
            _uiState.value = _uiState.value.copy(isWishlisted = nowWishlisted)
        }
    }

    private fun loadRelatedProducts(product: Product) {
        val query = product.title?.split(" ")?.take(3)?.joinToString(" ") ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRelated = true)
            try {
                val results = repository.searchProducts(query, saveToHistory = false)
                val filtered = results.filter { it.safeId() != product.safeId() }
                _uiState.value = _uiState.value.copy(
                    relatedProducts = filtered,
                    isLoadingRelated = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingRelated = false)
            }
        }
    }
}
