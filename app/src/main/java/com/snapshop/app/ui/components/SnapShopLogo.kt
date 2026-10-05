package com.snapshop.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapshop.app.R
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.TextPrimaryLight

/**
 * Official SnapShop Brand Identity Composable.
 * Renders the claymorphism 3D shopping bag + camera/scanner logo
 * with consistent styling, safe scaling, and optional brand lettering.
 */
@Composable
fun SnapShopLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    contentDescription: String = "SnapShop Logo"
) {
    Image(
        painter = painterResource(id = R.drawable.ic_snapshop_logo),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

/**
 * Tactile Claymorphic Logo Badge.
 * Wraps the SnapShop logo in a molded squircle surface with soft 3D elevation.
 */
@Composable
fun SnapShopLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    logoSize: Dp = 34.dp,
    shape: Shape = RoundedCornerShape(14.dp),
    elevation: Dp = 4.dp,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val effectiveBg = backgroundColor ?: SurfaceLight

    ClayCard(
        modifier = modifier.size(size),
        shape = shape,
        color = effectiveBg,
        elevation = elevation,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SnapShopLogo(
                size = logoSize,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}

/**
 * SnapShop Brand Header with Logo Mark + Wordmark.
 */
@Composable
fun SnapShopBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 40.dp,
    titleSize: Int = 20,
    subtitle: String? = "Smart Price Discovery"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SnapShopLogoBadge(
            size = logoSize,
            logoSize = (logoSize.value * 0.8f).dp,
            elevation = 3.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        androidx.compose.foundation.layout.Column {
            Text(
                text = "SnapShop",
                fontWeight = FontWeight.ExtraBold,
                fontSize = titleSize.sp,
                color = TextPrimaryLight,
                letterSpacing = (-0.3).sp
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryIndigo
                )
            }
        }
    }
}
