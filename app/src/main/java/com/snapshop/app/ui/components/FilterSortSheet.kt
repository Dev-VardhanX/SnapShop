package com.snapshop.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapshop.app.ui.search.AppliedFilters
import com.snapshop.app.ui.theme.ClayButton
import com.snapshop.app.ui.theme.ClayButtonVariant
import com.snapshop.app.ui.theme.ClayChip
import com.snapshop.app.ui.theme.PrimaryIndigo
import com.snapshop.app.ui.theme.SurfaceLight
import com.snapshop.app.ui.theme.SurfaceVariantLight
import com.snapshop.app.ui.theme.TextPrimaryLight
import com.snapshop.app.ui.theme.TextSecondaryLight

enum class SortOption(val displayName: String) {
    RECOMMENDED("Recommended"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    RATING("Rating: High to Low")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSortSheet(
    sheetState: SheetState,
    appliedFilters: AppliedFilters,
    selectedSort: SortOption,
    availableMerchants: List<String>,
    onSortSelected: (SortOption) -> Unit,
    onApplyFilters: (AppliedFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var stagedSort by remember(selectedSort) { mutableStateOf(selectedSort) }
    var stagedMinRating by remember(appliedFilters) { mutableStateOf(appliedFilters.minRating) }
    var stagedMerchant by remember(appliedFilters) { mutableStateOf(appliedFilters.merchant) }
    var stagedPriceRange by remember(appliedFilters) { mutableStateOf(appliedFilters.priceRange) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceVariantLight)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sort & Filter",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "Customize your product search results",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }

                TextButton(
                    onClick = {
                        stagedSort = SortOption.RECOMMENDED
                        stagedMinRating = 0.0
                        stagedMerchant = null
                        stagedPriceRange = PriceRangeFilter.ALL
                    }
                ) {
                    Text(
                        text = "Reset All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sort Section
            SectionHeader(title = "Sort Products By")
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.entries.forEach { option ->
                    ClayChip(
                        text = option.displayName,
                        selected = stagedSort == option,
                        onClick = { stagedSort = option }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Range Section
            SectionHeader(title = "Price Range")
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriceRangeFilter.entries.forEach { range ->
                    ClayChip(
                        text = range.displayName,
                        selected = stagedPriceRange == range,
                        onClick = { stagedPriceRange = range }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rating Section
            SectionHeader(title = "Minimum Rating")
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ratingOptions = listOf(
                    0.0 to "All Ratings",
                    2.0 to "2★ & above",
                    3.0 to "3★ & above",
                    4.0 to "4★ & above"
                )
                ratingOptions.forEach { (ratingVal, label) ->
                    ClayChip(
                        text = label,
                        selected = stagedMinRating == ratingVal,
                        onClick = { stagedMinRating = ratingVal }
                    )
                }
            }

            // Store Selection
            if (availableMerchants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Stores & Merchants")
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClayChip(
                        text = "All Stores",
                        selected = stagedMerchant == null,
                        onClick = { stagedMerchant = null }
                    )

                    availableMerchants.take(8).forEach { merchant ->
                        ClayChip(
                            text = merchant,
                            selected = stagedMerchant == merchant,
                            onClick = {
                                stagedMerchant = if (stagedMerchant == merchant) null else merchant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayButton(
                    onClick = onDismiss,
                    text = "Cancel",
                    variant = ClayButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )

                ClayButton(
                    onClick = {
                        onSortSelected(stagedSort)
                        onApplyFilters(
                            AppliedFilters(
                                minRating = stagedMinRating,
                                merchant = stagedMerchant,
                                priceRange = stagedPriceRange
                            )
                        )
                        onDismiss()
                    },
                    text = "Apply Filters",
                    icon = Icons.Default.Check,
                    variant = ClayButtonVariant.Primary,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = TextPrimaryLight
    )
}
