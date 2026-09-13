package com.snapshop.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    selectedSort: SortOption,
    minRatingFilter: Double,
    availableMerchants: List<String>,
    selectedMerchant: String?,
    onSortSelected: (SortOption) -> Unit,
    onMinRatingSelected: (Double) -> Unit,
    onMerchantSelected: (String?) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort & Filter",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset All", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sort Section
            Text(
                text = "Sort By",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = selectedSort == option,
                        onClick = { onSortSelected(option) },
                        label = { Text(option.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rating Filter Section
            Text(
                text = "Minimum Rating",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ratingOptions = listOf(0.0 to "All", 3.5 to "3.5★ & up", 4.0 to "4.0★ & up", 4.5 to "4.5★ & up")
                ratingOptions.forEach { (ratingVal, label) ->
                    FilterChip(
                        selected = minRatingFilter == ratingVal,
                        onClick = { onMinRatingSelected(ratingVal) },
                        label = { Text(label) }
                    )
                }
            }

            if (availableMerchants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Merchant / Source Store",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMerchant == null,
                        onClick = { onMerchantSelected(null) },
                        label = { Text("All Stores") }
                    )

                    availableMerchants.take(8).forEach { merchant ->
                        FilterChip(
                            selected = selectedMerchant == merchant,
                            onClick = { onMerchantSelected(if (selectedMerchant == merchant) null else merchant) },
                            label = { Text(merchant) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Apply & Close", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
