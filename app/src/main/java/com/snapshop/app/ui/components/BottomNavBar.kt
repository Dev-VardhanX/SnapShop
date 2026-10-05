package com.snapshop.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.snapshop.app.navigation.Screen
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.SnapShopTheme
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.TextSecondaryLight
import com.snapshop.app.ui.theme.clayDepth

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home)
    data object Search : BottomNavItem(Screen.SearchResults.route, "Search", Icons.Default.Search)
    data object Wishlist : BottomNavItem(Screen.Wishlist.route, "Wishlist", Icons.Default.Favorite)
    data object History : BottomNavItem(Screen.SearchHistory.route, "History", Icons.Default.History)
}

/**
 * Floating clay navigation bar.
 * The outer Box is transparent, so the app background shows around/below the pill
 * (no black band) as long as the system nav bar is transparent (see AppNavigation).
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Wishlist,
        BottomNavItem.History
    )
    val barShape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clayDepth(shape = barShape, color = SurfaceLight, elevation = 7.dp)
                .clip(barShape)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->

                    val currentBaseRoute = currentRoute?.substringBefore("?")
                    val itemBaseRoute = item.route.substringBefore("?")

                    val isSelected = currentBaseRoute == itemBaseRoute

                    ClayNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                onNavigate(item.route)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClayNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val itemShape = RoundedCornerShape(22.dp)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navScale"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryIndigo else TextSecondaryLight,
        label = "navIconColor"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .then(
                if (isSelected) {
                    // selected tab = pressed-in clay dent
                    Modifier
                        .clayDepth(
                            shape = itemShape,
                            color = PrimaryIndigoContainer,
                            elevation = 4.dp,
                            inset = true,
                            dropShadow = PrimaryIndigo.copy(alpha = 0.25f)
                        )
                        .clip(itemShape)
                } else {
                    Modifier.clip(itemShape)
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconColor,
                modifier = Modifier.size(21.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = item.title,
                    color = PrimaryIndigo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavBarPreview() {
    SnapShopTheme {
        BottomNavBar(
            currentRoute = Screen.Home.route,
            onNavigate = {}
        )
    }
}
