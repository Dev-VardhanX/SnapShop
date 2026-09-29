package com.snapshop.app.ui.wishlist

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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.snapshop.app.ui.theme.RedHeart
import com.snapshop.app.ui.theme.SoftRed
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onProductClick: (Product) -> Unit,
    onOpenMerchantLink: (String?) -> Unit = {},
    viewModel: WishlistViewModel = viewModel()
) {
    val products by viewModel.wishlistProducts.collectAsState()

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
                        color = SoftRed,
                        elevation = 3.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = RedHeart,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "My Wishlist",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Tracked favorite deals",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                if (products.isNotEmpty()) {
                    ClayBadge(
                        text = "${products.size} SAVED",
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
                    icon = Icons.Default.FavoriteBorder,
                    title = "Your wishlist is waiting",
                    description = "Save products you love to track their prices across top stores and find them here later."
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
                            product = product.copy(isWishlisted = true),
                            onProductClick = onProductClick,
                            onWishlistToggle = { viewModel.removeFromWishlist(it.safeId()) },
                            onViewDealClick = { p -> onOpenMerchantLink(p.buyUrl) }
                        )
                    }
                }
            }
        }
    }
}
