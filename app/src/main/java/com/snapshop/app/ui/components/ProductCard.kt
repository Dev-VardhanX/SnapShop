package com.snapshop.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.snapshop.app.domain.Product
import com.snapshop.app.ui.theme.AccentMintDark
import com.snapshop.app.ui.theme.AmberStar
import com.snapshop.app.ui.theme.ClayBadge
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.RedHeart
import com.snapshop.app.ui.theme.SoftRed
import com.snapshop.app.ui.theme.SoftYellow
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.SurfaceVariantLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight

import androidx.compose.ui.res.painterResource
import com.snapshop.app.R

/**
 * Premium 2-Column Claymorphic Product Card.
 * Soft elevated surface, prominent product visual, tactile wishlist action,
 * clean typography with bold price emphasis and minimal clutter.
 */
@Composable
fun ProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (Product) -> Unit,
    modifier: Modifier = Modifier,
    onViewDealClick: ((Product) -> Unit)? = null
) {
    val imageBoxBg = SurfaceVariantLight
    val titleColor = TextPrimaryLight
    val subtitleColor = TextSecondaryLight

    ClayCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = SurfaceLight,
        elevation = 5.dp,
        onClick = { onProductClick(product) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Product Image Box with Floating Tactile Wishlist Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(imageBoxBg),
                contentAlignment = Alignment.Center
            ) {
                val fallbackPainter = painterResource(id = R.drawable.ic_snapshop_logo)
                val imageUrl = product.imageUrl?.trim()

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = fallbackPainter,
                        error = fallbackPainter
                    )
                } else {
                    SnapShopLogo(
                        size = 48.dp,
                        contentDescription = "SnapShop Product"
                    )
                }

                // Tactile Wishlist Floating Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    WishlistHeartButton(
                        isWishlisted = product.isWishlisted,
                        onClick = { onWishlistToggle(product) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Store Name & Rating Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val source = product.source?.trim()
                if (!source.isNullOrBlank()) {
                    ClayBadge(
                        text = source.uppercase(),
                        backgroundColor = imageBoxBg,
                        textColor = subtitleColor
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (product.rating != null && product.rating > 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SoftYellow)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberStar,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", product.rating),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Product Title
            Text(
                text = product.title ?: "Product",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp,
                modifier = Modifier.height(34.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val priceText = product.price?.trim()
                if (!priceText.isNullOrBlank()) {
                    Text(
                        text = priceText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentMintDark
                    )
                } else {
                    Text(
                        text = "Check Deal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtitleColor
                    )
                }

                if (product.reviewsCount != null && product.reviewsCount > 0) {
                    Text(
                        text = "(${product.reviewsCount})",
                        fontSize = 11.sp,
                        color = subtitleColor
                    )
                }
            }
        }

    }
}

/**
 * Tactile bouncing Heart button for wishlist saving.
 */
@Composable
fun WishlistHeartButton(
    isWishlisted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 32
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else if (isWishlisted) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heartScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isWishlisted) RedHeart else TextSecondaryLight,
        label = "heartColor"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isWishlisted) SoftRed else SurfaceLight.copy(alpha = 0.92f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
            tint = iconColor,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}