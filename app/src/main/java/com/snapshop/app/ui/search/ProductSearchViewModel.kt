package com.snapshop.app.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapshop.app.data.repository.ProductRepository
import com.snapshop.app.domain.Product
import com.snapshop.app.ui.components.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductSearchUiState(
    val query: String = "",
    val rawProducts: List<Product> = emptyList(),
    val displayedProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedSort: SortOption = SortOption.RECOMMENDED,
    val minRatingFilter: Double = 0.0,
    val selectedMerchant: String? = null,
    val availableMerchants: List<String> = emptyList()
)

class ProductSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ProductSearchUiState())
    val uiState: StateFlow<ProductSearchUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            error = null
        )
    }

    fun search(searchQuery: String? = null) {
        val q = (searchQuery ?: _uiState.value.query).trim()

        if (q.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a product to search"
            )
            return
        }

        Log.d("SnapShopSearch", "SEARCH: START")
        Log.d("SnapShopSearch", "SEARCH: QUERY: $q")

        _uiState.value = _uiState.value.copy(
            query = q,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val products = repository.searchProducts(q)

                val merchants = products.mapNotNull { it.source }
                    .filter { it.isNotBlank() }
                    .distinct()

                val currentState = _uiState.value
                val filtered = applySortAndFilters(
                    products = products,
                    sort = currentState.selectedSort,
                    minRating = currentState.minRatingFilter,
                    merchant = currentState.selectedMerchant
                )

                _uiState.value = _uiState.value.copy(
                    rawProducts = products,
                    displayedProducts = filtered,
                    availableMerchants = merchants,
                    isLoading = false
                )

                Log.d("SnapShopSearch", "SEARCH: UI STATE UPDATED (raw = ${products.size}, displayed = ${filtered.size})")
            } catch (e: Exception) {
                Log.e("SnapShopSearch", "SEARCH ERROR IN VIEWMODEL: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load shopping results. Please try again."
                )
            }
        }
    }

    fun setSort(sort: SortOption) {
        val currentState = _uiState.value
        val updated = applySortAndFilters(
            products = currentState.rawProducts,
            sort = sort,
            minRating = currentState.minRatingFilter,
            merchant = currentState.selectedMerchant
        )
        _uiState.value = currentState.copy(
            selectedSort = sort,
            displayedProducts = updated
        )
    }

    fun setMinRating(minRating: Double) {
        val currentState = _uiState.value
        val updated = applySortAndFilters(
            products = currentState.rawProducts,
            sort = currentState.selectedSort,
            minRating = minRating,
            merchant = currentState.selectedMerchant
        )
        _uiState.value = currentState.copy(
            minRatingFilter = minRating,
            displayedProducts = updated
        )
    }

    fun setMerchantFilter(merchant: String?) {
        val currentState = _uiState.value
        val updated = applySortAndFilters(
            products = currentState.rawProducts,
            sort = currentState.selectedSort,
            minRating = currentState.minRatingFilter,
            merchant = merchant
        )
        _uiState.value = currentState.copy(
            selectedMerchant = merchant,
            displayedProducts = updated
        )
    }

    fun resetFilters() {
        val currentState = _uiState.value
        val updated = applySortAndFilters(
            products = currentState.rawProducts,
            sort = SortOption.RECOMMENDED,
            minRating = 0.0,
            merchant = null
        )
        _uiState.value = currentState.copy(
            selectedSort = SortOption.RECOMMENDED,
            minRatingFilter = 0.0,
            selectedMerchant = null,
            displayedProducts = updated
        )
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val nowWishlisted = repository.toggleWishlist(product)

            val updateList = { list: List<Product> ->
                list.map { p ->
                    if (p.safeId() == product.safeId()) p.copy(isWishlisted = nowWishlisted) else p
                }
            }

            _uiState.value = _uiState.value.copy(
                rawProducts = updateList(_uiState.value.rawProducts),
                displayedProducts = updateList(_uiState.value.displayedProducts)
            )
        }
    }

    private fun applySortAndFilters(
        products: List<Product>,
        sort: SortOption,
        minRating: Double,
        merchant: String?
    ): List<Product> {
        var result = products

        if (minRating > 0.0) {
            result = result.filter { (it.rating ?: 0.0) >= minRating }
        }

        if (merchant != null && merchant.isNotBlank()) {
            result = result.filter { it.source.equals(merchant, ignoreCase = true) }
        }

        return when (sort) {
            SortOption.RECOMMENDED -> result
            SortOption.PRICE_LOW_HIGH -> result.sortedBy { it.numericPrice }
            SortOption.PRICE_HIGH_LOW -> result.sortedByDescending { it.numericPrice }
            SortOption.RATING -> result.sortedByDescending { it.rating ?: 0.0 }
        }
    }
}