package com.snapshop.app.ui.recentlyviewed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapshop.app.domain.Product
import com.snapshop.app.ui.components.ProductCard
import com.snapshop.app.ui.theme.BackgroundLight
import com.snapshop.app.ui.theme.ClayBadge
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.ClayEmptyState
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.SnapShopTheme
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight

@Composable
fun RecentlyViewedScreen(
    onProductClick: (Product) -> Unit,
    onOpenMerchantLink: (String?) -> Unit = {},
    viewModel: RecentlyViewedViewModel = viewModel()
) {
    val products by viewModel.recentlyViewedProducts.collectAsState()

    RecentlyViewedContent(
        products = products,
        onProductClick = onProductClick,
        onOpenMerchantLink = onOpenMerchantLink,
        onWishlistToggle = { viewModel.toggleWishlist(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyViewedContent(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onOpenMerchantLink: (String?) -> Unit = {},
    onWishlistToggle: (Product) -> Unit = {}
) {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClayCard(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryIndigoContainer,
                        elevation = 3.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Recently Viewed",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Products you browsed recently",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                if (products.isNotEmpty()) {
                    ClayBadge(
                        text = "${products.size} ITEMS",
                        backgroundColor = PrimaryIndigoContainer,
                        textColor = PrimaryIndigo
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            if (products.isEmpty()) {
                ClayEmptyState(
                    useLogo = true,
                    title = "No recently viewed products",
                    description = "Products you inspect or explore will automatically appear here for convenient reference."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = products,
                        key = { it.safeId() }
                    ) { product ->
                        ProductCard(
                            product = product,
                            onProductClick = onProductClick,
                            onWishlistToggle = onWishlistToggle,
                            onViewDealClick = { p -> onOpenMerchantLink(p.buyUrl) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecentlyViewedScreenPreview() {
    SnapShopTheme {
        RecentlyViewedContent(
            products = listOf(
                Product(
                    id = "1",
                    title = "Wireless Headphones",
                    price = "$99.99",
                    source = "Store",
                    imageUrl = null,
                    buyUrl = null,
                    rating = 4.5,
                    reviewsCount = 100
                ),
                Product(
                    id = "2",
                    title = "Smart Watch",
                    price = "$199.99",
                    source = "Store",
                    imageUrl = null,
                    buyUrl = null,
                    rating = 4.8,
                    reviewsCount = 250
                )
            ),
            onProductClick = {},
            onOpenMerchantLink = {},
            onWishlistToggle = {}
        )
    }
}
