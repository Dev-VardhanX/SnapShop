package com.snapshop.app.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapshop.app.data.repository.ProductRepository
import com.snapshop.app.domain.Product
import com.snapshop.app.ui.components.PriceRangeFilter
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
    val loadingMessage: String = "Finding products...",
    val error: String? = null,
    val isFromVisualSearch: Boolean = false,
    val selectedSort: SortOption = SortOption.RECOMMENDED,
    val minRatingFilter: Double = 0.0,
    val selectedMerchant: String? = null,
    val priceRangeFilter: PriceRangeFilter = PriceRangeFilter.ALL,
    val availableMerchants: List<String> = emptyList()
)

data class AppliedFilters(
    val minRating: Double = 0.0,
    val merchant: String? = null,
    val priceRange: PriceRangeFilter = PriceRangeFilter.ALL
)

class ProductSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ProductSearchUiState())
    val uiState: StateFlow<ProductSearchUiState> = _uiState.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            error = null,
            isFromVisualSearch = false
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

        searchJob?.cancel()

        _uiState.value = _uiState.value.copy(
            query = q,
            isLoading = true,
            loadingMessage = "Finding products...",
            error = null,
            isFromVisualSearch = false
        )

        searchJob = viewModelScope.launch {
            try {
                val products = repository.searchProducts(q)
                publishSearchResults(q, products, isFromVisualSearch = false)
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e("SnapShopSearch", "Search error: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Couldn't find products right now. Please try again."
                    )
                }
            }
        }
    }

    fun setVisualSearchResults(query: String, products: List<Product>) {
        publishSearchResults(query, products, isFromVisualSearch = true)
    }

    private fun publishSearchResults(
        query: String,
        products: List<Product>,
        isFromVisualSearch: Boolean
    ) {
        val merchants = products.mapNotNull { it.source }
            .filter { it.isNotBlank() }
            .distinct()

        val currentState = _uiState.value
        val filtered = applySortAndFilters(
            products = products,
            sort = currentState.selectedSort,
            minRating = currentState.minRatingFilter,
            merchant = currentState.selectedMerchant,
            priceRange = currentState.priceRangeFilter
        )

        _uiState.value = currentState.copy(
            query = query,
            rawProducts = products,
            displayedProducts = filtered,
            availableMerchants = merchants,
            isLoading = false,
            isFromVisualSearch = isFromVisualSearch,
            error = null
        )
    }

    fun setSort(sort: SortOption) {
        val currentState = _uiState.value
        val updated = applySortAndFilters(
            products = currentState.rawProducts,
            sort = sort,
            minRating = currentState.minRatingFilter,
            merchant = currentState.selectedMerchant,
            priceRange = currentState.priceRangeFilter
        )
        _uiState.value = currentState.copy(
            selectedSort = sort,
            displayedProducts = updated
        )
    }

    fun applyFilters(filters: AppliedFilters) {
        val currentState = _uiState.value
        val updated = applySortAndFilters(
            products = currentState.rawProducts,
            sort = currentState.selectedSort,
            minRating = filters.minRating,
            merchant = filters.merchant,
            priceRange = filters.priceRange
        )
        _uiState.value = currentState.copy(
            minRatingFilter = filters.minRating,
            selectedMerchant = filters.merchant,
            priceRangeFilter = filters.priceRange,
            displayedProducts = updated
        )
    }

    fun resetFilters() {
        val currentState = _uiState.value
        val updated = applySortAndFilters(
            products = currentState.rawProducts,
            sort = SortOption.RECOMMENDED,
            minRating = 0.0,
            merchant = null,
            priceRange = PriceRangeFilter.ALL
        )
        _uiState.value = currentState.copy(
            selectedSort = SortOption.RECOMMENDED,
            minRatingFilter = 0.0,
            selectedMerchant = null,
            priceRangeFilter = PriceRangeFilter.ALL,
            displayedProducts = updated
        )
    }

    fun currentAppliedFilters(): AppliedFilters = AppliedFilters(
        minRating = _uiState.value.minRatingFilter,
        merchant = _uiState.value.selectedMerchant,
        priceRange = _uiState.value.priceRangeFilter
    )

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
        merchant: String?,
        priceRange: PriceRangeFilter
    ): List<Product> {
        var result = products

        if (minRating > 0.0) {
            result = result.filter { (it.rating ?: 0.0) >= minRating }
        }

        if (merchant != null && merchant.isNotBlank()) {
            result = result.filter { it.source?.contains(merchant, ignoreCase = true) == true }
        }

        if (priceRange != PriceRangeFilter.ALL) {
            result = result.filter { product ->
                val price = product.numericPrice
                price > 0.0 && priceRange.matches(price)
            }
        }

        return when (sort) {
            SortOption.RECOMMENDED -> result
            SortOption.PRICE_LOW_HIGH -> result.sortedWith(
                compareBy { if (it.numericPrice <= 0.0) Double.MAX_VALUE else it.numericPrice }
            )
            SortOption.PRICE_HIGH_LOW -> result.sortedByDescending { it.numericPrice }
            SortOption.RATING -> result.sortedByDescending { it.rating ?: 0.0 }
        }
    }
}
