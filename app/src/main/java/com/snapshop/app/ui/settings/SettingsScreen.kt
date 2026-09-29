package com.snapshop.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapshop.app.BuildConfig
import com.snapshop.app.ui.theme.BackgroundLight
import com.snapshop.app.ui.theme.ClayButton
import com.snapshop.app.ui.theme.ClayButtonVariant
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.ClayIconButton
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearRecentDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Clear All Search History?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            },
            text = {
                Text(
                    text = "This will permanently remove your recorded text and photo searches.",
                    color = TextSecondaryLight
                )
            },
            confirmButton = {
                ClayButton(
                    onClick = {
                        viewModel.clearSearchHistory()
                        showClearHistoryDialog = false
                    },
                    text = "Clear",
                    variant = ClayButtonVariant.Destructive,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = TextSecondaryLight, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showClearRecentDialog) {
        AlertDialog(
            onDismissRequest = { showClearRecentDialog = false },
            title = {
                Text(
                    text = "Clear Recently Viewed?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            },
            text = {
                Text(
                    text = "This will remove all recently viewed products from your history.",
                    color = TextSecondaryLight
                )
            },
            confirmButton = {
                ClayButton(
                    onClick = {
                        viewModel.clearRecentlyViewed()
                        showClearRecentDialog = false
                    },
                    text = "Clear",
                    variant = ClayButtonVariant.Destructive,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showClearRecentDialog = false }) {
                    Text("Cancel", color = TextSecondaryLight, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
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
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.size(42.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // App Information Section
            SettingsSectionHeader("Application")

            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceLight,
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem(
                        icon = Icons.Default.ShoppingBag,
                        title = "About SnapShop",
                        subtitle = "Find anything. Shop smarter. Compare live prices."
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "${BuildConfig.VERSION_NAME} (Claymorphism Edition)"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data Management Section
            SettingsSectionHeader("Data & Storage")

            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceLight,
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem(
                        icon = Icons.Default.History,
                        title = "Clear Search History",
                        subtitle = "Remove all past text and camera searches",
                        onClick = { showClearHistoryDialog = true }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "Clear Recently Viewed",
                        subtitle = "Remove all tracked recently browsed products",
                        onClick = { showClearRecentDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = PrimaryIndigo,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryIndigoContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimaryLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondaryLight
            )
        }
    }
}
