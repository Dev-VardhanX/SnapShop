package com.snapshop.app.ui.details

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.snapshop.app.domain.Product
import com.snapshop.app.ui.components.isValidMerchantUrl
import com.snapshop.app.ui.theme.AccentMintDark
import com.snapshop.app.ui.theme.AmberStar
import androidx.compose.ui.res.painterResource
import com.snapshop.app.R
import com.snapshop.app.ui.components.SnapShopLogo
import com.snapshop.app.ui.theme.BackgroundLight
import com.snapshop.app.ui.theme.ClayBadge
import com.snapshop.app.ui.theme.ClayButton
import com.snapshop.app.ui.theme.ClayButtonVariant
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.ClayIconButton
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.RedHeart
import com.snapshop.app.ui.theme.SoftRed
import com.snapshop.app.ui.theme.SoftYellow
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.SurfaceVariantLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    product: Product,
    onBackClick: () -> Unit,
    onRelatedProductClick: (Product) -> Unit,
    onOpenMerchantLink: (String?) -> Unit = {},
    viewModel: ProductDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(product) {
        viewModel.setProduct(product)
    }

    val currentProduct = uiState.product ?: product
    val isDealValid = isValidMerchantUrl(currentProduct.buyUrl)

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            DetailsTopBar(
                title = currentProduct.title ?: "Product Details",
                isWishlisted = uiState.isWishlisted,
                onBackClick = onBackClick,
                onWishlistToggle = { viewModel.toggleWishlist() },
                onShareClick = {
                    val shareText = "Compare prices for ${currentProduct.title}: ${currentProduct.buyUrl ?: "via SnapShop"}"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Product Deal"))
                }
            )
        },
        bottomBar = {
            // Floating Clay CTA Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLight)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Secondary: Wishlist toggle
                    ClayIconButton(
                        onClick = { viewModel.toggleWishlist() },
                        icon = if (uiState.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save to Wishlist",
                        size = 54.dp,
                        iconSize = 24.dp,
                        shape = RoundedCornerShape(18.dp),
                        containerColor = if (uiState.isWishlisted) SoftRed else SurfaceLight,
                        contentColor = if (uiState.isWishlisted) RedHeart else TextSecondaryLight
                    )

                    // Primary CTA: View Deal
                    ClayButton(
                        onClick = { onOpenMerchantLink(currentProduct.buyUrl) },
                        text = if (isDealValid) "View Deal" else "Deal Unavailable",
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        variant = if (isDealValid) ClayButtonVariant.Primary else ClayButtonVariant.Secondary,
                        enabled = isDealValid,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Hero Product Image Showcase
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = SurfaceLight,
                    elevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val fallback = painterResource(id = R.drawable.ic_snapshop_logo)
                        if (!currentProduct.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = currentProduct.imageUrl,
                                contentDescription = currentProduct.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                placeholder = fallback,
                                error = fallback
                            )
                        } else {
                            SnapShopLogo(
                                size = 72.dp,
                                contentDescription = "SnapShop Product"
                            )
                        }
                    }
                }
            }

            // 2. Main Title, Price & Rating Card
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceLight,
                    elevation = 5.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Store / Merchant Tag Row
                        currentProduct.source?.takeIf { it.isNotBlank() }?.let { source ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ClayBadge(
                                    text = source.uppercase(),
                                    backgroundColor = PrimaryIndigoContainer,
                                    textColor = PrimaryIndigo,
                                    icon = Icons.Default.Store
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = AccentMintDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Verified Listing",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AccentMintDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Product Title
                        Text(
                            text = currentProduct.title ?: "Product Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            lineHeight = 25.sp,
                            color = TextPrimaryLight
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Price and Rating Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                currentProduct.price?.takeIf { it.isNotBlank() }?.let { price ->
                                    Text(
                                        text = price,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AccentMintDark
                                    )
                                }
                                Text(
                                    text = "Best store offer detected",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                            }

                            currentProduct.rating?.let { rating ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SoftYellow)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberStar,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = String.format("%.1f", rating),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = TextPrimaryLight
                                    )
                                    currentProduct.reviewsCount?.let { count ->
                                        Text(
                                            text = " ($count)",
                                            fontSize = 12.sp,
                                            color = TextSecondaryLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Store Listing & Price Protection Guarantee Card
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceLight,
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Merchant & Listing Info",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimaryLight
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrimaryIndigoContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = currentProduct.source ?: "Online Partner Store",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimaryLight
                                    )
                                    Text(
                                        text = "Official Store Partner",
                                        fontSize = 11.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            currentProduct.price?.let { price ->
                                Text(
                                    text = price,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = TextPrimaryLight
                                )
                            }
                        }
                    }
                }
            }

            // 4. Compare Similar Listings Section
            if (uiState.relatedProducts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Compare Similar Offers & Prices",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(
                    items = uiState.relatedProducts.take(6),
                    key = { it.safeId() }
                ) { related ->
                    ClayOfferCard(
                        product = related,
                        onClick = { onRelatedProductClick(related) },
                        onViewDeal = { onOpenMerchantLink(related.buyUrl) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Top App Bar for Product Details with back, wishlist, and share buttons.
 */
@Composable
private fun DetailsTopBar(
    title: String,
    isWishlisted: Boolean,
    onBackClick: () -> Unit,
    onWishlistToggle: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClayIconButton(
            onClick = onBackClick,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            size = 42.dp,
            iconSize = 20.dp
        )

        Text(
            text = "Product Details",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = TextPrimaryLight
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClayIconButton(
                onClick = onWishlistToggle,
                icon = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Save to Wishlist",
                size = 42.dp,
                iconSize = 20.dp,
                containerColor = if (isWishlisted) SoftRed else SurfaceLight,
                contentColor = if (isWishlisted) RedHeart else TextSecondaryLight
            )

            ClayIconButton(
                onClick = onShareClick,
                icon = Icons.Default.Share,
                contentDescription = "Share",
                size = 42.dp,
                iconSize = 20.dp
            )
        }
    }
}

/**
 * Clean Clay Offer Card for comparing merchant offers.
 */
@Composable
private fun ClayOfferCard(
    product: Product,
    onClick: () -> Unit,
    onViewDeal: () -> Unit
) {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceLight,
        elevation = 3.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantLight),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    SnapShopLogo(
                        size = 32.dp,
                        contentDescription = "SnapShop Product"
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                product.source?.let { store ->
                    ClayBadge(
                        text = store.uppercase(),
                        backgroundColor = SurfaceVariantLight,
                        textColor = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }

                Text(
                    text = product.title ?: "Similar Product",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryLight,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                product.price?.let { price ->
                    Text(
                        text = price,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentMintDark
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            ClayButton(
                onClick = onViewDeal,
                text = "Deal",
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                variant = ClayButtonVariant.Secondary,
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
