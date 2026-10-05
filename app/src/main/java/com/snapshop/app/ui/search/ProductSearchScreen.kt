package com.snapshop.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapshop.app.domain.Product
import com.snapshop.app.ui.components.FilterSortSheet
import com.snapshop.app.ui.components.PriceRangeFilter
import com.snapshop.app.ui.components.ProductCard
import com.snapshop.app.ui.components.SortOption
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import com.snapshop.app.ui.components.SnapShopLogo
import com.snapshop.app.ui.theme.BackgroundLight
import com.snapshop.app.ui.theme.ClayBadge
import com.snapshop.app.ui.theme.ClayButton
import com.snapshop.app.ui.theme.ClayButtonVariant
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.ClayEmptyState
import com.snapshop.app.ui.theme.ClayIconButton
import com.snapshop.app.ui.theme.ClaySkeletonCard
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.SnapShopTheme
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.SurfaceVariantLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight
import com.snapshop.app.ui.theme.TextTertiaryLight
import kotlinx.coroutines.launch

@Composable
fun ProductSearchScreen(
    initialQuery: String? = null,
    onProductClick: (Product) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onOpenMerchantLink: (String?) -> Unit,
    viewModel: ProductSearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    ProductSearchContent(
        uiState = uiState,
        initialQuery = initialQuery,
        onProductClick = onProductClick,
        onCameraClick = onCameraClick,
        onGalleryClick = onGalleryClick,
        onOpenMerchantLink = onOpenMerchantLink,
        onQueryChange = viewModel::updateQuery,
        onSearch = { viewModel.search(it) },
        onToggleWishlist = viewModel::toggleWishlist,
        onSetSort = viewModel::setSort,
        onApplyFilters = viewModel::applyFilters,
        onResetFilters = viewModel::resetFilters
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearchContent(
    uiState: ProductSearchUiState,
    initialQuery: String? = null,
    onProductClick: (Product) -> Unit = {},
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onOpenMerchantLink: (String?) -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onSearch: (String?) -> Unit = {},
    onToggleWishlist: (Product) -> Unit = {},
    onSetSort: (SortOption) -> Unit = {},
    onApplyFilters: (AppliedFilters) -> Unit = {},
    onResetFilters: () -> Unit = {}
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val cleanInitialQuery = remember(initialQuery) {
        if (initialQuery == null || initialQuery == "{query}" || initialQuery.trim() == "{query}") "" else initialQuery
    }

    LaunchedEffect(cleanInitialQuery) {
        if (cleanInitialQuery.isNotBlank() &&
            (cleanInitialQuery != uiState.query || uiState.rawProducts.isEmpty()) &&
            !uiState.isLoading &&
            !uiState.isFromVisualSearch
        ) {
            onSearch(cleanInitialQuery)
        }
    }

    val isFilterActive = uiState.minRatingFilter > 0.0 ||
        uiState.selectedMerchant != null ||
        uiState.priceRangeFilter != PriceRangeFilter.ALL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Clay Search Bar Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClayCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceLight,
                elevation = 5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )

                    TextField(
                        value = uiState.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(
                            color = TextPrimaryLight,
                            fontSize = 14.sp
                        ),
                        placeholder = {
                            Text(
                                text = "Search products...",
                                fontSize = 14.sp,
                                color = TextTertiaryLight,
                                maxLines = 1
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch(null) }),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryLight,
                            unfocusedTextColor = TextPrimaryLight,
                            disabledTextColor = TextTertiaryLight,
                            cursorColor = PrimaryIndigo,
                            focusedPlaceholderColor = TextTertiaryLight,
                            unfocusedPlaceholderColor = TextTertiaryLight,
                            selectionColors = TextSelectionColors(
                                handleColor = PrimaryIndigo,
                                backgroundColor = PrimaryIndigo.copy(alpha = 0.25f)
                            ),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    if (uiState.query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextSecondaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Visual Search Camera & Gallery shortcuts
                    ClayIconButton(
                        onClick = onCameraClick,
                        icon = Icons.Default.CameraAlt,
                        contentDescription = "Camera Search",
                        size = 34.dp,
                        iconSize = 16.dp,
                        containerColor = PrimaryIndigoContainer,
                        contentColor = PrimaryIndigo,
                        elevation = 2.dp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    ClayIconButton(
                        onClick = onGalleryClick,
                        icon = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery Search",
                        size = 34.dp,
                        iconSize = 16.dp,
                        containerColor = PrimaryIndigoContainer,
                        contentColor = PrimaryIndigo,
                        elevation = 2.dp
                    )
                }
            }

            ClayButton(
                onClick = { onSearch(null) },
                text = "Search",
                variant = ClayButtonVariant.Primary,
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subheader: Results count & Sort/Filter Trigger
        if (uiState.rawProducts.isNotEmpty() && !uiState.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${uiState.displayedProducts.size} Products",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimaryLight
                    )

                    if (uiState.isFromVisualSearch) {
                        Spacer(modifier = Modifier.width(8.dp))
                        ClayBadge(
                            text = "PHOTO MATCH",
                            backgroundColor = PrimaryIndigoContainer,
                            textColor = PrimaryIndigo
                        )
                    }
                }

                // Sort & Filter Clay Pill Button with Active Dot
                ClayCard(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isFilterActive) PrimaryIndigoContainer else SurfaceLight,
                    elevation = 4.dp,
                    onClick = { showFilterSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (isFilterActive) PrimaryIndigo else TextPrimaryLight,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filter & Sort",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFilterActive) PrimaryIndigo else TextPrimaryLight
                        )
                        if (isFilterActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryIndigo)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Main Content Area
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Clay Skeleton Loading State
                uiState.isLoading -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryIndigo)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.loadingMessage,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(6) {
                                ClaySkeletonCard(height = 240.dp)
                            }
                        }
                    }
                }

                // Error State
                uiState.error != null -> {
                    ClayEmptyState(
                        useLogo = true,
                        title = "Couldn't reach store listings",
                        description = "We couldn't retrieve products right now. Please check your network and try again.",
                        actionText = "Try Again",
                        onActionClick = { onSearch(null) }
                    )
                }

                // 2-Column Product Grid
                uiState.displayedProducts.isNotEmpty() -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = uiState.displayedProducts,
                            key = { index, product -> "${product.safeId()}_$index" }
                        ) { _, product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                onWishlistToggle = { onToggleWishlist(it) },
                                onViewDealClick = { p -> onOpenMerchantLink(p.buyUrl) }
                            )
                        }
                    }
                }

                // Filter yielded zero results
                uiState.rawProducts.isNotEmpty() && uiState.displayedProducts.isEmpty() -> {
                    ClayEmptyState(
                        icon = Icons.Default.FilterList,
                        title = "No matches for active filters",
                        description = "Try adjusting your price range, merchant, or minimum rating filters.",
                        actionText = "Reset Filters",
                        onActionClick = { onResetFilters() }
                    )
                }

                // Visual search yielded zero results
                uiState.isFromVisualSearch && uiState.rawProducts.isEmpty() -> {
                    ClayEmptyState(
                        icon = Icons.Default.PhotoCamera,
                        title = "No store listings found",
                        description = "We couldn't find exact shopping results for this photo. Try snapping from a clearer angle or searching by text.",
                        actionText = "Take New Photo",
                        onActionClick = onCameraClick
                    )
                }

                // Initial empty state before search
                else -> {
                    ClayEmptyState(
                        useLogo = true,
                        title = "Find products across top stores",
                        description = "Type any product or brand name to compare real-time prices across Amazon, Flipkart, and more.",
                        actionText = "Try Searching 'Wireless Earbuds'",
                        onActionClick = { onSearch("Wireless Earbuds") }
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterSortSheet(
            sheetState = sheetState,
            appliedFilters = AppliedFilters(
                minRating = uiState.minRatingFilter,
                merchant = uiState.selectedMerchant,
                priceRange = uiState.priceRangeFilter
            ),
            selectedSort = uiState.selectedSort,
            availableMerchants = uiState.availableMerchants,
            onSortSelected = { onSetSort(it) },
            onApplyFilters = { onApplyFilters(it) },
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showFilterSheet = false
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductSearchScreenPreview() {
    val sampleProducts = listOf(
        Product(
            id = "1",
            title = "Sony WH-1000XM5 Wireless Headphones",
            price = "$398.00",
            source = "Amazon",
            imageUrl = null,
            buyUrl = null,
            rating = 4.7,
            reviewsCount = 1250,
            isWishlisted = false
        ),
        Product(
            id = "2",
            title = "Bose QuietComfort 45 Bluetooth Headphones",
            price = "$329.00",
            source = "Best Buy",
            imageUrl = null,
            buyUrl = null,
            rating = 4.6,
            reviewsCount = 890,
            isWishlisted = true
        ),
        Product(
            id = "3",
            title = "Apple AirPods Max Wireless Headphones",
            price = "$549.00",
            source = "Apple",
            imageUrl = null,
            buyUrl = null,
            rating = 4.8,
            reviewsCount = 2100,
            isWishlisted = false
        ),
        Product(
            id = "4",
            title = "Sennheiser Momentum 4 Wireless Headphones",
            price = "$299.95",
            source = "Target",
            imageUrl = null,
            buyUrl = null,
            rating = 4.4,
            reviewsCount = 430,
            isWishlisted = false
        )
    )

    SnapShopTheme {
        ProductSearchContent(
            uiState = ProductSearchUiState(
                query = "Wireless Headphones",
                rawProducts = sampleProducts,
                displayedProducts = sampleProducts,
                isLoading = false,
                availableMerchants = listOf("Amazon", "Best Buy", "Apple", "Target")
            )
        )
    }
}

