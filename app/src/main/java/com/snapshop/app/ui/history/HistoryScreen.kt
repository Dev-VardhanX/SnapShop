package com.snapshop.app.ui.history

import android.text.format.DateUtils
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapshop.app.data.local.entity.SearchHistoryEntity
import com.snapshop.app.ui.theme.BackgroundLight
import com.snapshop.app.ui.theme.ClayButton
import com.snapshop.app.ui.theme.ClayButtonVariant
import com.snapshop.app.ui.theme.ClayCard
import com.snapshop.app.ui.theme.ClayEmptyState
import com.snapshop.app.ui.theme.ClayIconButton
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.PrimaryIndigoContainer
import com.snapshop.app.ui.theme.RedHeart
import com.snapshop.app.ui.theme.SoftRed
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.SurfaceVariantLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSearchQueryClick: (String) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val historyList by viewModel.historyItems.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear Search History?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all your past searches from this device.",
                    color = TextSecondaryLight
                )
            },
            confirmButton = {
                ClayButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearConfirmDialog = false
                    },
                    text = "Clear All",
                    variant = ClayButtonVariant.Destructive,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Search History",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Recent lookups and visual scans",
                            fontSize = 11.sp,
                            color = TextSecondaryLight
                        )
                    }
                }

                if (historyList.isNotEmpty()) {
                    ClayIconButton(
                        onClick = { showClearConfirmDialog = true },
                        icon = Icons.Default.DeleteOutline,
                        contentDescription = "Clear All History",
                        containerColor = SoftRed,
                        contentColor = RedHeart,
                        size = 40.dp,
                        iconSize = 18.dp
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
            if (historyList.isEmpty()) {
                ClayEmptyState(
                    useLogo = true,
                    title = "No search history yet",
                    description = "Searches you perform using text or camera will automatically be saved here for quick access."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = historyList,
                        key = { it.id }
                    ) { item ->
                        ClaySearchHistoryCard(
                            item = item,
                            onClick = { onSearchQueryClick(item.query) },
                            onDelete = { viewModel.deleteItem(item.query) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tactile clay card for an individual search history entry.
 */
@Composable
private fun ClaySearchHistoryCard(
    item: SearchHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isVisual = item.searchType == "IMAGE"

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceLight,
        elevation = 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isVisual) PrimaryIndigoContainer else SurfaceVariantLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVisual) Icons.Default.CameraAlt else Icons.Default.Search,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.query,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(3.dp))

                val timeAgo = DateUtils.getRelativeTimeSpanString(
                    item.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )

                Text(
                    text = "$timeAgo • ${if (isVisual) "Visual Search" else "Text Search"}",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
            }

            ClayIconButton(
                onClick = onDelete,
                icon = Icons.Default.Clear,
                contentDescription = "Delete",
                size = 32.dp,
                iconSize = 14.dp,
                containerColor = SurfaceVariantLight,
                contentColor = TextSecondaryLight,
                elevation = 0.dp
            )
        }
    }
}
