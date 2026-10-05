package com.snapshop.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapshop.app.ui.components.SnapShopLogo
import com.snapshop.app.ui.theme.BackgroundLight
import com.snapshop.app.ui.theme.ClayBadge
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight
import kotlinx.coroutines.delay

/**
 * Official SnapShop Splash Screen.
 * Prominently presents the claymorphism 3D shopping-bag + camera logo
 * centered on the clean #F6F7FB background with soft subtle shadow
 * and clean minimal presentation.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1300L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Centered Claymorphic Logo Card with soft subtle shadow
            ClayCard(
                modifier = Modifier.size(130.dp),
                shape = RoundedCornerShape(36.dp),
                color = SurfaceLight,
                elevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SnapShopLogo(
                        size = 100.dp,
                        contentDescription = "SnapShop Brand Identity"
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name
            Text(
                text = "SnapShop",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = TextPrimaryLight,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Brand Tagline
            Text(
                text = "Find anything. Shop smarter.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Clay Pill Badge
            ClayBadge(
                text = "SMART PRICE DISCOVERY",
                backgroundColor = PrimaryIndigoContainer,
                textColor = PrimaryIndigo
            )
        }
    }
}
