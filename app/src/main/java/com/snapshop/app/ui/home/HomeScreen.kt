package com.snapshop.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.snapshop.app.data.local.entity.SearchHistoryEntity
import com.snapshop.app.data.repository.ProductRepository
import com.snapshop.app.domain.Product
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import com.snapshop.app.ui.components.SnapShopLogo
import com.snapshop.app.ui.components.SnapShopLogoBadge
import com.snapshop.app.ui.theme.AccentMintDark
import com.snapshop.app.ui.theme.BackgroundLight
import com.snapshop.app.ui.theme.ClayButton
import com.snapshop.app.ui.theme.ClayButtonVariant
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.ClayChip
import com.snapshop.app.ui.theme.ClayIconButton
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.RedHeart
import com.snapshop.app.ui.theme.SnapShopTheme
import com.snapshop.app.ui.theme.SoftRed
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.SurfaceVariantLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight
import com.snapshop.app.ui.theme.TextTertiaryLight

data class CategoryItem(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HomeScreen(
    onSearchQuerySubmit: (String) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onHistoryItemClick: (String) -> Unit,
    onWishlistClick: (() -> Unit)? = null,
    onRecentlyViewedClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val repository = remember { ProductRepository(context) }
    val recentSearches by repository.getRecentSearches().collectAsState(initial = emptyList())
    val wishlistProducts by repository.getWishlistProducts().collectAsState(initial = emptyList())
    val recentlyViewedProducts by repository.getRecentlyViewedProducts().collectAsState(initial = emptyList())

    HomeScreenContent(
        recentSearches = recentSearches,
        wishlistProducts = wishlistProducts,
        recentlyViewedProducts = recentlyViewedProducts,
        onSearchQuerySubmit = onSearchQuerySubmit,
        onCameraClick = onCameraClick,
        onGalleryClick = onGalleryClick,
        onHistoryItemClick = onHistoryItemClick,
        onWishlistClick = onWishlistClick,
        onRecentlyViewedClick = onRecentlyViewedClick,
        onSettingsClick = onSettingsClick
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreenContent(
    recentSearches: List<SearchHistoryEntity>,
    wishlistProducts: List<Product>,
    recentlyViewedProducts: List<Product>,
    onSearchQuerySubmit: (String) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onHistoryItemClick: (String) -> Unit,
    onWishlistClick: (() -> Unit)? = null,
    onRecentlyViewedClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null
) {

    var searchText by remember { mutableStateOf("") }

    val categories = listOf(
        CategoryItem("Electronics", Icons.Default.Devices, Color(0xFFE0E7FF)),
        CategoryItem("Fashion", Icons.Default.ShoppingBag, Color(0xFFFFEDD5)),
        CategoryItem("Shoes & Watches", Icons.Default.Watch, Color(0xFFD1FAE5)),
        CategoryItem("Beauty", Icons.Default.Spa, Color(0xFFFCE7F3)),
        CategoryItem("Home", Icons.Default.Home, Color(0xFFE0F2FE)),
        CategoryItem("Fitness", Icons.Default.FitnessCenter, Color(0xFFFEF3C7)),
        CategoryItem("Gaming", Icons.Default.SportsEsports, Color(0xFFEDE9FE))
    )

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            HomeTopBar(
                wishlistCount = wishlistProducts.size,
                onWishlistClick = onWishlistClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Hero Greeting Headline
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HomeHeroSection()
            }

            // Clay Molded Search Bar
            item {
                ClaySearchBar(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    onSearch = {
                        if (searchText.isNotBlank()) {
                            onSearchQuerySubmit(searchText.trim())
                        }
                    },
                    onCameraShortcut = onCameraClick
                )
            }

            // Signature Visual Search Clay Showcase Card
            item {
                VisualSearchClayCard(
                    onCameraClick = onCameraClick,
                    onGalleryClick = onGalleryClick
                )
            }

            // Recent Searches Chips
            if (recentSearches.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recent Searches",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimaryLight
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentSearches.take(6).forEach { entity ->
                                ClayChip(
                                    text = entity.query,
                                    selected = false,
                                    onClick = { onHistoryItemClick(entity.query) }
                                )
                            }
                        }
                    }
                }
            }

            // Recently Viewed Horizontal Carousel
            if (recentlyViewedProducts.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recently Viewed",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimaryLight
                                )
                            }

                            if (onRecentlyViewedClick != null) {
                                Text(
                                    text = "See All",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo,
                                    modifier = Modifier.clickable { onRecentlyViewedClick() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(
                                items = recentlyViewedProducts.take(8),
                                key = { it.safeId() }
                            ) { product ->
                                MiniatureProductCard(
                                    product = product,
                                    onClick = { onSearchQuerySubmit(product.title ?: "") }
                                )
                            }
                        }
                    }
                }
            }

            // Explore Categories Section
            item {
                Column {
                    Text(
                        text = "Explore Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimaryLight,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categories.forEach { category ->
                            ClayCard(
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceLight,
                                elevation = 4.dp,
                                onClick = { onSearchQuerySubmit(category.title) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(category.color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = null,
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = category.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimaryLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Top App Bar with SnapShop 3D logo emblem, Wishlist quick counter, and Settings button.
 */
@Composable
private fun HomeTopBar(
    wishlistCount: Int,
    onWishlistClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Official SnapShop Logo & Brand Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            SnapShopLogoBadge(
                size = 44.dp,
                logoSize = 34.dp,
                elevation = 4.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "SnapShop",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = TextPrimaryLight,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "Smart Price Discovery",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryIndigo
                )
            }
        }

        // Actions: Wishlist Quick Pill & Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onWishlistClick != null) {
                ClayCard(
                    shape = RoundedCornerShape(14.dp),
                    color = if (wishlistCount > 0) SoftRed else SurfaceLight,
                    elevation = 4.dp,
                    onClick = onWishlistClick
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Wishlist",
                            tint = RedHeart,
                            modifier = Modifier.size(16.dp)
                        )
                        if (wishlistCount > 0) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$wishlistCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RedHeart
                            )
                        }
                    }
                }
            }

            if (onSettingsClick != null) {
                ClayIconButton(
                    onClick = onSettingsClick,
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    size = 40.dp,
                    iconSize = 18.dp
                )
            }
        }
    }
}

/**
 * Large Friendly Hero Section: "Find anything. Shop smarter."
 */
@Composable
private fun HomeHeroSection() {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = SurfaceLight,
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryIndigoContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "AI PRICE RADAR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryIndigo,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Find anything.\nShop smarter.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryLight,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Compare live prices across top stores instantly with text or camera.",
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3D Molded Floating SnapShop Emblem
            ClayCard(
                modifier = Modifier.size(68.dp),
                shape = RoundedCornerShape(20.dp),
                color = PrimaryIndigoContainer,
                elevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SnapShopLogo(
                        size = 50.dp,
                        contentDescription = "SnapShop Brand Emblem"
                    )
                }
            }
        }
    }
}

/**
 * Large Clay Molded Search Bar.
 */
@Composable
private fun ClaySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCameraShortcut: () -> Unit
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = SurfaceLight,
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(22.dp)
            )

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    color = TextPrimaryLight,
                    fontSize = 14.sp
                ),
                placeholder = {
                    Text(
                        text = "Search products, brands, models...",
                        fontSize = 14.sp,
                        color = TextTertiaryLight
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
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

            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondaryLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Camera shortcut in search bar
            ClayIconButton(
                onClick = onCameraShortcut,
                icon = Icons.Default.CameraAlt,
                contentDescription = "Visual Search",
                size = 38.dp,
                iconSize = 18.dp,
                containerColor = PrimaryIndigoContainer,
                contentColor = PrimaryIndigo
            )
        }
    }
}


/**
 * Signature Visual Search Clay Showcase Card.
 * Makes photo search feel like a signature SnapShop feature.
 */
@Composable
private fun VisualSearchClayCard(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = SurfaceLight,
        elevation = 7.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayCard(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryIndigoContainer,
                    elevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Search with an Image",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "Snap a real item to find exact store listings",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two Prominent Tactile Clay Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayButton(
                    onClick = onCameraClick,
                    text = "Photo",
                    icon = Icons.Default.CameraAlt,
                    variant = ClayButtonVariant.Primary,
                    modifier = Modifier.weight(1f)
                )

                ClayButton(
                    onClick = onGalleryClick,
                    text = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    variant = ClayButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Miniature Product Card for the Recently Viewed carousel.
 */
@Composable
private fun MiniatureProductCard(
    product: Product,
    onClick: () -> Unit
) {
    ClayCard(
        modifier = Modifier.width(148.dp),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceLight,
        elevation = 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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
                            .padding(6.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = TextSecondaryLight.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title ?: "Product",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            product.price?.let { price ->
                Text(
                    text = price,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentMintDark
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SnapShopTheme {
        HomeScreenContent(
            recentSearches = listOf(
                SearchHistoryEntity(query = "Wireless Headphones"),
                SearchHistoryEntity(query = "Smart Watch"),
                SearchHistoryEntity(query = "Running Shoes")
            ),
            wishlistProducts = listOf(
                Product(
                    id = "1",
                    title = "Wireless Headphones",
                    price = "$99.99",
                    source = "Store",
                    imageUrl = null,
                    buyUrl = null,
                    rating = 4.5,
                    reviewsCount = 100
                )
            ),
            recentlyViewedProducts = listOf(
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
            onSearchQuerySubmit = {},
            onCameraClick = {},
            onGalleryClick = {},
            onHistoryItemClick = {},
            onWishlistClick = {},
            onRecentlyViewedClick = {},
            onSettingsClick = {}
        )
    }
}